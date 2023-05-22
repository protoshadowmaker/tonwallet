package ton.coin.wallet.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ton.api.liteclient.config.LiteClientConfigGlobal
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.block.AccountInfo
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.cell.buildCell
import org.ton.contract.wallet.WalletContract
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletV4R2Contract
import org.ton.lite.client.LiteClient
import org.ton.lite.client.internal.FullAccountState
import org.ton.lite.client.internal.TransactionId
import ton.coin.wallet.common.async.AppDispatchers
import ton.coin.wallet.contract.WalletV3R1Contract
import ton.coin.wallet.contract.WalletV3R2Contract
import ton.coin.wallet.controller.TonConfigLoader
import ton.coin.wallet.data.CompletedTransaction
import ton.coin.wallet.data.CompletedTransactionMapUtil
import ton.coin.wallet.data.DraftTransaction

class TonRemoteDataSource(private val dispatchers: AppDispatchers) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(dispatchers.io + job)

    private var clientInitializationJob: Job? = null
    private var _client: LiteClient? = null
    private val client: LiteClient get() = requireNotNull(_client)
    private val isInitializationCompleted: Boolean get() = _client != null

    init {
        scope.launch { initialize() }
    }

    private suspend fun initialize(): Boolean = withContext(dispatchers.main) {
        if (isInitializationCompleted) {
            return@withContext true
        }
        clientInitializationJob?.let {
            if (it.isActive) {
                it.join()
                return@withContext isInitializationCompleted
            }
        }
        clientInitializationJob = scope.launch {
            val configData: String = TonConfigLoader.loadConfig()
            val json = Json {
                ignoreUnknownKeys = true
            }
            val config = withContext(dispatchers.default) {
                json.decodeFromString<LiteClientConfigGlobal>(configData)
            }
            _client = LiteClient(dispatchers.io, config)
        }
        clientInitializationJob?.join()
        isInitializationCompleted
    }

    suspend fun getAccountState(address: AddrStd): Result<FullAccountState> {
        if (!initialize()) {
            return Result.failure(Exception())
        }
        return try {
            val result = client.getAccountState(AddrStd(address.toString(userFriendly = true)))
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun createWallet(
        walletContract: WalletV4R2Contract, privateKey: PrivateKeyEd25519
    ): Result<Boolean> {
        return try {
            walletContract.transfer(client.liteApi, privateKey)
            Result.success(true)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun processTransaction(
        transaction: DraftTransaction,
        contract: WalletContract<out Any>,
        privateKey: PrivateKeyEd25519
    ): Result<Boolean> {
        return try {
            val destinationAccountState =
                client.getAccountState(AddrStd(requireNotNull(transaction.address)))

            val destinationAccountInfo = destinationAccountState.account.value
            if (destinationAccountInfo !is AccountInfo) {
                return Result.success(false)
            }
            val nanoAmount = requireNotNull(transaction.amount).value
            val walletTransfer = WalletTransfer {
                destination = destinationAccountInfo.addr
                coins = Coins.ofNano(nanoAmount)
                bounceable = false
                val comment = transaction.comment ?: ""
                if (comment.isNotBlank()) {
                    body = buildCell {
                        storeUInt(0, 32)
                        storeBytes(comment.toByteArray())
                    }
                }
            }
            when (contract) {
                is WalletV3R1Contract -> contract.transfer(
                    client.liteApi, privateKey, walletTransfer
                )

                is WalletV3R2Contract -> contract.transfer(
                    client.liteApi, privateKey, walletTransfer
                )

                is WalletV4R2Contract -> contract.transfer(
                    client.liteApi, privateKey, walletTransfer
                )

                else -> return Result.success(false)
            }
            Result.success(true)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(
        accountAddress: AddrStd, fromTransactionId: TransactionId
    ): Result<List<CompletedTransaction>> {
        return try {
            val rawTransactions = client.getLastTransactions(accountAddress, fromTransactionId)
            Result.success(rawTransactions.map {
                CompletedTransactionMapUtil.toCompletedTransaction(it)
            })
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}