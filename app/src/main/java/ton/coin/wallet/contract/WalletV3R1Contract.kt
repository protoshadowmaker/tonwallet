package ton.coin.wallet.contract

import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.block.AccountInfo
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer
import org.ton.lite.api.LiteApi
import kotlin.time.Duration.Companion.seconds

class WalletV3R1Contract : WalletV3Contract {

    constructor(
        workchain: Int,
        init: StateInit
    ) : super(workchain, init)

    constructor(
        workchain: Int,
        publicKey: PublicKeyEd25519
    ) : super(workchain, publicKey, CODE)

    constructor(accountInfo: AccountInfo) : super(accountInfo)

    suspend fun transfer(
        liteApi: LiteApi,
        privateKey: PrivateKeyEd25519,
        vararg transfers: WalletTransfer
    ): Unit = transfer(liteApi, privateKey, Clock.System.now() + 60.seconds, *transfers)

    suspend fun transfer(
        liteApi: LiteApi,
        privateKey: PrivateKeyEd25519,
        validUntil: Instant,
        vararg transfers: WalletTransfer
    ): Unit = coroutineScope {
        super.transfer(liteApi, privateKey, validUntil, CODE, *transfers)
    }

    private companion object {
        @JvmField
        val CODE =
            Cell("FF0020DD2082014C97BA9730ED44D0D70B1FE0A4F2608308D71820D31FD31FD31FF82313BBF263ED44D0D31FD31FD3FFD15132BAF2A15144BAF2A204F901541055F910F2A3F8009320D74A96D307D402FB00E8D101A4C8CB1FCB1FCBFFC9ED54")
    }
}