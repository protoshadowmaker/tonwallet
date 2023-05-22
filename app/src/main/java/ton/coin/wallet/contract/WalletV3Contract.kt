package ton.coin.wallet.contract

import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bitstring.BitString
import org.ton.block.AccountActive
import org.ton.block.AccountInfo
import org.ton.block.AccountState
import org.ton.block.AccountUninit
import org.ton.block.AddrNone
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.block.CommonMsgInfoRelaxed
import org.ton.block.Either
import org.ton.block.ExtInMsgInfo
import org.ton.block.Maybe
import org.ton.block.Message
import org.ton.block.MessageRelaxed
import org.ton.block.MsgAddressInt
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.contract.SmartContract
import org.ton.contract.wallet.WalletContract
import org.ton.contract.wallet.WalletTransfer
import org.ton.lite.api.LiteApi
import org.ton.lite.client.LiteClient
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef
import kotlin.time.Duration.Companion.seconds

open class WalletV3Contract private constructor(
    override val address: MsgAddressInt,
    override val state: AccountState
) : WalletContract<Cell> {
    constructor(
        workchain: Int,
        init: StateInit
    ) : this(SmartContract.address(workchain, init), AccountUninit)

    constructor(
        workchain: Int,
        publicKey: PublicKeyEd25519,
        code: Cell,
    ) : this(workchain, createStateInit(publicKey, WalletContract.DEFAULT_WALLET_ID + workchain, code))

    constructor(
        accountInfo: AccountInfo
    ) : this(accountInfo.addr, accountInfo.storage.state)

    override fun loadData(): Cell? = data

    fun getSeqno(): Int = requireNotNull(data).beginParse().run {
        preloadInt(32).toInt()
    }

    fun getSubWalletId(): Int = requireNotNull(data).beginParse().run {
        skipBits(32)
        preloadInt(32).toInt()
    }

    fun getPublicKey(): PublicKeyEd25519 = requireNotNull(data).beginParse().run {
        skipBits(64)
        PublicKeyEd25519(loadBits(256).toByteArray())
    }

    suspend fun transfer(
        liteApi: LiteApi,
        privateKey: PrivateKeyEd25519,
        code: Cell,
        vararg transfers: WalletTransfer,
    ): Unit = transfer(liteApi, privateKey, Clock.System.now() + 60.seconds, code, *transfers)

    suspend fun transfer(
        liteApi: LiteApi,
        privateKey: PrivateKeyEd25519,
        validUntil: Instant,
        code: Cell,
        vararg transfers: WalletTransfer,
    ): Unit = coroutineScope {
        val seqno = if (state !is AccountActive) 0 else getSeqno()
        val walletId = if (state !is AccountActive) WalletContract.DEFAULT_WALLET_ID else getSubWalletId()
        val message = createTransferMessage(
            address = address,
            stateInit = if (state !is AccountActive) createStateInit(privateKey.publicKey(), walletId, code) else null,
            privateKey = privateKey,
            validUntil = validUntil.epochSeconds.toInt(),
            walletId = walletId,
            seqno = seqno,
            transfers = transfers
        )
        sendExternalMessage(liteApi, AnyTlbConstructor, message)
    }

    private companion object {

        @JvmField
        val OP_TRANSFER = 0

        @JvmStatic
        suspend fun <T : WalletContract<Cell>> loadContract(
            liteClient: LiteClient,
            address: AddrStd,
            contractFactory: (AccountInfo) -> T,
        ): T? {
            val blockId = liteClient.getLastBlockId()
            return loadContract(liteClient, blockId, address, contractFactory)
        }

        @JvmStatic
        suspend fun <T : WalletContract<Cell>> loadContract(
            liteClient: LiteClient,
            blockId: TonNodeBlockIdExt,
            address: AddrStd,
            contractFactory: (AccountInfo) -> T
        ): T? {
            val accountInfo = liteClient.getAccountState(address, blockId).account.value
            return if(accountInfo is AccountInfo) {
                contractFactory(accountInfo)
            } else {
                null
            }
        }

        @JvmStatic
        fun createTransferMessage(
            address: MsgAddressInt,
            stateInit: StateInit?,
            privateKey: PrivateKeyEd25519,
            walletId: Int,
            validUntil: Int,
            seqno: Int,
            vararg transfers: WalletTransfer
        ): Message<Cell> {
            val info = ExtInMsgInfo(
                src = AddrNone,
                dest = address,
                importFee = Coins()
            )
            val maybeStateInit =
                Maybe.of(stateInit?.let { Either.of<StateInit, CellRef<StateInit>>(null, CellRef(it)) })
            val transferBody = createTransferMessageBody(
                privateKey,
                walletId,
                validUntil,
                seqno,
                *transfers
            )
            val body = Either.of<Cell, CellRef<Cell>>(null, CellRef(transferBody))
            return Message(
                info = info,
                init = maybeStateInit,
                body = body
            )
        }

        @JvmStatic
        fun createStateInit(
            publicKey: PublicKeyEd25519,
            walletId: Int,
            code: Cell,
        ): StateInit {
            val data = CellBuilder.createCell {
                storeUInt(0, 32) // seqno
                storeUInt(walletId, 32)
                storeBytes(publicKey.key.toByteArray())
            }
            return StateInit(
                code = code,
                data = data
            )
        }

        private fun createTransferMessageBody(
            privateKey: PrivateKeyEd25519,
            walletId: Int,
            validUntil: Int,
            seqno: Int,
            vararg gifts: WalletTransfer
        ): Cell {
            val unsignedBody = CellBuilder.createCell {
                storeUInt(walletId, 32)
                storeUInt(validUntil, 32)
                storeUInt(seqno, 32)
                storeUInt(OP_TRANSFER, 8) // op
                for (gift in gifts) {
                    var sendMode = 3
                    if (gift.sendMode > -1) {
                        sendMode = gift.sendMode
                    }
                    val intMsg = CellRef(createIntMsg(gift))

                    storeUInt(sendMode, 8)
                    storeRef(MessageRelaxed.tlbCodec(AnyTlbConstructor), intMsg)
                }
            }
            val signature = BitString(privateKey.sign(unsignedBody.hash().toByteArray()))

            return CellBuilder.createCell {
                storeBits(signature)
                storeBits(unsignedBody.bits)
                storeRefs(unsignedBody.refs)
            }
        }

        private fun createIntMsg(gift: WalletTransfer): MessageRelaxed<Cell> {
            val info = CommonMsgInfoRelaxed.IntMsgInfoRelaxed(
                ihrDisabled = true,
                bounce = gift.bounceable,
                bounced = false,
                src = AddrNone,
                dest = gift.destination,
                value = gift.coins,
                ihrFee = Coins(),
                fwdFee = Coins(),
                createdLt = 0u,
                createdAt = 0u
            )
            val init = Maybe.of(gift.stateInit?.let {
                Either.of<StateInit, CellRef<StateInit>>(null, CellRef(it))
            })
            val giftBody = gift.body
            val body = if (giftBody == null) {
                Either.of<Cell, CellRef<Cell>>(Cell.empty(), null)
            } else {
                Either.of<Cell, CellRef<Cell>>(null, CellRef(giftBody))
            }

            return MessageRelaxed(
                info = info,
                init = init,
                body = body,
            )
        }
    }
}