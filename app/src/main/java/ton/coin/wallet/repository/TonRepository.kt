package ton.coin.wallet.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.block.AccountInfo
import org.ton.block.AddrStd
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletContract
import org.ton.contract.wallet.WalletV4R2Contract
import org.ton.lite.client.internal.TransactionId
import org.ton.mnemonic.Mnemonic
import ton.coin.wallet.common.async.AppDispatchers
import ton.coin.wallet.contract.WalletV3R1Contract
import ton.coin.wallet.contract.WalletV3R2Contract
import ton.coin.wallet.data.CompletedTransaction
import ton.coin.wallet.data.DraftTransaction
import ton.coin.wallet.data.PendingTransaction
import ton.coin.wallet.data.TonCoins
import ton.coin.wallet.data.Wallet
import ton.coin.wallet.data.WalletVersion
import kotlin.math.min

class TonRepository(
    private val dispatchers: AppDispatchers,
    private val localDs: TonLocalDataSource,
    private val remoteDs: TonRemoteDataSource
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(dispatchers.io + job)
    private val launchScope = CoroutineScope(dispatchers.main + job)
    private var loadTransactionsJob: Job? = null
    private var autoLoadTransactionsCount = 0

    private val _repositoryState: MutableStateFlow<RepositoryState> =
        MutableStateFlow(RepositoryState())
    val repositoryStateFlow: StateFlow<RepositoryState> get() = _repositoryState

    val repositoryState: RepositoryState get() = repositoryStateFlow.value

    init {
        scope.launch {
            val mnemonic = localDs.getWalletMnemonic().getOrNull()
            if (mnemonic != null) {
                modifyState {
                    repositoryState.copy(
                        wallet = Wallet.UserWallet(mnemonic),
                        walletState = LoadingState.Completed(),
                    )
                }
            } else {
                modifyState {
                    repositoryState.copy(
                        wallet = Wallet.Empty,
                        walletState = LoadingState.Completed(),
                    )
                }
            }
            checkAndReloadTransactionsInternal()
        }
    }

    private suspend fun onStateChanged(newState: RepositoryState) = withContext(dispatchers.main) {
        _repositoryState.tryEmit(newState)
    }

    private suspend inline fun modifyState(
        crossinline modifyAction: (state: RepositoryState) -> RepositoryState
    ) = withContext(dispatchers.main) {
        onStateChanged(modifyAction(repositoryStateFlow.value))
    }

    suspend fun createNewWallet(): Result<Wallet.DraftWallet> = withContext(dispatchers.default) {
        val mnemonicResult = localDs.createWallet()
        val mnemonicValue = mnemonicResult.getOrNull()
        if (mnemonicResult.isFailure || mnemonicValue == null) {
            Result.failure(mnemonicResult.exceptionOrNull() ?: Exception())
        } else {
            val draftWallet = Wallet.DraftWallet(mnemonicValue)
            saveDraftWallet(draftWallet)
            Result.success(draftWallet)
        }
    }

    suspend fun deleteWallet(): Result<Unit> = withContext(dispatchers.default) {
        localDs.cleanData()
        Result.success(Unit)
    }

    suspend fun saveDraftWallet(wallet: Wallet.DraftWallet) = withContext(dispatchers.io) {
        modifyState {
            repositoryState.copy(wallet = wallet)
        }
    }

    suspend fun saveUserWallet(wallet: Wallet.UserWallet) = withContext(dispatchers.io) {
        localDs.saveWalletMnemonic(wallet.mnemonic)
        modifyState {
            repositoryState.copy(wallet = wallet)
        }
        preloadCache()
    }

    private fun preloadCache() {
        scope.launch {
            preloadCacheInternal()
        }
    }

    private suspend fun preloadCacheInternal() {
        val mnemonicResult = localDs.getWalletMnemonic()
        val mnemonic = mnemonicResult.getOrNull() ?: return
        val privateKey = getPrivateKey(mnemonic.mnemonic)
        val publicKey = privateKey.publicKey()
        val v3r1Addr = WalletV3R1Contract(0, publicKey).address as AddrStd
        val v3r2Addr = WalletV3R2Contract(0, publicKey).address as AddrStd
        val v4r2Addr = WalletV4R2Contract(0, publicKey).address as AddrStd
        localDs.saveWalletAddress(v3r1Addr.toString(userFriendly = true), WalletVersion.V3R1)
        localDs.saveWalletAddress(v3r2Addr.toString(userFriendly = true), WalletVersion.V3R2)
        localDs.saveWalletAddress(v4r2Addr.toString(userFriendly = true), WalletVersion.V4R2)
        getAccountInfo(v3r1Addr).getOrNull()?.let {
            localDs.saveWalletBalance(
                TonCoins(it.storage.balance.coins.amount.value), WalletVersion.V3R1
            )
        }
        getAccountInfo(v3r2Addr).getOrNull()?.let {
            localDs.saveWalletBalance(
                TonCoins(it.storage.balance.coins.amount.value), WalletVersion.V3R2
            )
        }
        getAccountInfo(v4r2Addr).getOrNull()?.let {
            localDs.saveWalletBalance(
                TonCoins(it.storage.balance.coins.amount.value), WalletVersion.V4R2
            )
        }
    }

    suspend fun saveSecureSettings(pinCode: String) = withContext(dispatchers.io) {
        localDs.savePinCode(pinCode)
        localDs.savePinCodeSize(pinCode.length)
    }

    suspend fun getPinCode(): String = withContext(dispatchers.io) {
        localDs.getPinCode().getOrNull() ?: ""
    }

    suspend fun isWalletValid(
        mnemonic: List<String>
    ): Result<Boolean> = withContext(dispatchers.default) {
        val trimmed = mnemonic.map { it.trim() }
        if (trimmed.any { it.isBlank() }) {
            return@withContext Result.success(false)
        }
        Result.success(Mnemonic.isValid(mnemonic))
    }

    suspend fun getCachedBalance(): Result<TonCoins> = withContext(dispatchers.io) {
        val walletVersion = localDs.getWalletVersion().getOrThrow()
        getCachedBalance(walletVersion)
    }

    private suspend fun getCachedBalance(
        walletVersion: WalletVersion
    ): Result<TonCoins> = withContext(dispatchers.io) {
        localDs.getWalletBalance(walletVersion)
    }

    suspend fun getBalance(): Result<TonCoins> = withContext(dispatchers.io) {
        val walletVersion = localDs.getWalletVersion().getOrThrow()
        getBalance(walletVersion)
    }

    private suspend fun getBalance(
        walletVersion: WalletVersion
    ): Result<TonCoins> = withContext(dispatchers.io) {
        val mnemonicResult = localDs.getWalletMnemonic()
        val mnemonic = mnemonicResult.getOrNull() ?: return@withContext Result.failure(Exception())
        val accountResult = getAccountInfo(mnemonic.mnemonic, walletVersion)
        val account = accountResult.getOrNull()
        if (account != null) {
            val balance = TonCoins(account.storage.balance.coins.amount.value)
            localDs.saveWalletBalance(balance, walletVersion)
            Result.success(balance)
        } else {
            Result.failure(Exception())
        }
    }

    suspend fun getWalletAddress(): Result<String> = withContext(dispatchers.io) {
        val currentVersion = localDs.getWalletVersion().getOrThrow()
        getWalletAddress(currentVersion)
    }

    suspend fun getWalletAddress(
        version: WalletVersion
    ): Result<String> = withContext(dispatchers.io) {
        val cachedAddress = localDs.getWalletAddress(version).getOrNull()
        if (cachedAddress != null) {
            return@withContext Result.success(cachedAddress)
        }
        val mnemonicResult = localDs.getWalletMnemonic()
        val mnemonic = mnemonicResult.getOrNull() ?: return@withContext Result.failure(Exception())
        val walletAddress = getWalletAddress(version, mnemonic.mnemonic)
        val userFriendlyAddress = walletAddress.toString(userFriendly = true)
        localDs.saveWalletAddress(userFriendlyAddress, version)
        Result.success(userFriendlyAddress)
    }

    suspend fun getWalletVersion(): Result<WalletVersion> = withContext(dispatchers.io) {
        localDs.getWalletVersion()
    }

    private suspend fun getAccountInfo(
        mnemonic: List<String>, version: WalletVersion
    ): Result<AccountInfo> = withContext(dispatchers.io) {
        val walletAddress = getWalletAddress(version, mnemonic)
        getAccountInfo(walletAddress)
    }

    private suspend fun getAccountInfo(
        walletAddress: AddrStd
    ): Result<AccountInfo> = withContext(dispatchers.io) {
        val accountStateResult = remoteDs.getAccountState(walletAddress)
        val accountState =
            accountStateResult.getOrNull() ?: return@withContext Result.failure(Exception())
        val account = accountState.account.value
        if (account !is AccountInfo) {
            Result.failure(Exception())
        } else {
            Result.success(account)
        }
    }

    private suspend fun getPrivateKey(mnemonic: List<String>): PrivateKeyEd25519 =
        withContext(dispatchers.default) {
            val seed = Mnemonic.toSeed(mnemonic, "")
            PrivateKeyEd25519.of(seed)
        }

    private suspend fun getWalletAddress(version: WalletVersion, mnemonic: List<String>): AddrStd {
        val privateKey = getPrivateKey(mnemonic)
        val publicKey = privateKey.publicKey()
        return when (version) {
            WalletVersion.V3R1 -> {
                WalletV3R1Contract(0, publicKey).address as AddrStd
            }

            WalletVersion.V3R2 -> {
                WalletV3R2Contract(0, publicKey).address as AddrStd
            }

            WalletVersion.V4R2 -> {
                WalletV4R2Contract(0, publicKey).address as AddrStd
            }
        }
    }

    private fun <T : WalletContract<Cell>> createWalletContract(
        version: WalletVersion, accountInfo: AccountInfo
    ): T {
        @Suppress("UNCHECKED_CAST") return when (version) {
            WalletVersion.V3R1 -> {
                WalletV3R1Contract(accountInfo)
            }

            WalletVersion.V3R2 -> {
                WalletV3R2Contract(accountInfo)
            }

            WalletVersion.V4R2 -> {
                WalletV4R2Contract(accountInfo)
            }
        } as T
    }

    suspend fun getDraftTransaction(): Result<DraftTransaction?> = withContext(dispatchers.io) {
        localDs.getDraftTransaction()
    }

    suspend fun saveDraftTransaction(draft: DraftTransaction) = withContext(dispatchers.io) {
        localDs.saveDraftTransaction(draft)
    }

    suspend fun cleanupDraftTransaction() = withContext(dispatchers.io) {
        localDs.saveDraftTransaction(null)
    }

    suspend fun processTransaction(
        transaction: DraftTransaction
    ): Result<Boolean> = withContext(dispatchers.io) {
        val address: String = transaction.address ?: return@withContext Result.success(false)
        val amount: TonCoins = transaction.amount ?: return@withContext Result.success(false)
        val comment: String = transaction.comment ?: ""
        val pendingTransaction =
            PendingTransaction(address = address, amount = amount, comment = comment)
        addPendingTransaction(pendingTransaction)
        val mnemonic = localDs.getWalletMnemonic().getOrNull()
        val walletVersion = localDs.getWalletVersion().getOrNull()
        if (mnemonic == null || walletVersion == null) {
            removePendingTransaction(pendingTransaction)
            return@withContext Result.success(false)
        }
        val account = getAccountInfo(mnemonic.mnemonic, walletVersion).getOrNull()
        if (account == null) {
            removePendingTransaction(pendingTransaction)
            return@withContext Result.success(false)
        }
        val result = remoteDs.processTransaction(
            transaction,
            createWalletContract(walletVersion, account),
            getPrivateKey(mnemonic.mnemonic)
        )
        removePendingTransaction(pendingTransaction)
        result
    }

    private suspend fun addPendingTransaction(transaction: PendingTransaction) {
        val wallet = repositoryState.wallet
        if (wallet !is Wallet.UserWallet) {
            return
        }
        modifyState {
            repositoryState.copy(
                wallet = wallet.copy(pendingTransactions = wallet.pendingTransactions.toMutableList()
                    .apply {
                        add(transaction)
                    })
            )
        }
    }

    private suspend fun removePendingTransaction(transaction: PendingTransaction) {
        val wallet = repositoryState.wallet
        if (wallet !is Wallet.UserWallet) {
            return
        }
        modifyState {
            repositoryState.copy(
                wallet = wallet.copy(pendingTransactions = wallet.pendingTransactions.toMutableList()
                    .apply {
                        removeIf { it.uuid == transaction.uuid }
                    })
            )
        }
    }

    fun reloadTransactions() {
        if (!canReloadTransactions()) {
            return
        }
        loadTransactionsJob = launchScope.launch {
            checkAndReloadTransactionsInternal()
        }
    }

    private fun canReloadTransactions(): Boolean {
        return repositoryState.transactionsState != LoadingState.Loading
    }

    private suspend fun checkAndReloadTransactionsInternal() {
        if (!canReloadTransactions()) {
            return
        }
        modifyState {
            repositoryState.copy(
                transactionsState = LoadingState.Loading,
                haveMoreTransactions = true
            )
        }
        withContext(dispatchers.io) {
            reloadTransactionsInternal()
        }
        loadNextTransactions(false)
    }

    private suspend fun reloadTransactionsInternal() {
        val wallet = repositoryState.wallet
        if (wallet !is Wallet.UserWallet) {
            modifyState {
                repositoryState.copy(transactionsState = LoadingState.Completed())
            }
            return
        }
        val finishUpdate: suspend (transactions: List<CompletedTransaction>) -> Unit = {
            val newWallet = wallet.copy(completedTransactions = it)
            if (it.isNotEmpty()) {
                val version = localDs.getWalletVersion().getOrThrow()
                localDs.saveTransactions(it, version)
            }
            modifyState {
                repositoryState.copy(
                    wallet = newWallet,
                    transactionsState = LoadingState.Completed()
                )
            }
        }
        if (wallet.completedTransactions.isEmpty()) {
            val version = localDs.getWalletVersion().getOrThrow()
            val cachedTransactions = localDs.getTransactions(version).getOrNull() ?: emptyList()
            modifyState {
                repositoryState.copy(
                    wallet = wallet.copy(completedTransactions = cachedTransactions)
                )
            }
        }
        val mnemonic = localDs.getWalletMnemonic().getOrNull()
        val walletVersion = localDs.getWalletVersion().getOrNull()
        if (mnemonic == null || walletVersion == null) {
            finishUpdate(emptyList())
            return
        }
        val walletAddress = getWalletAddress(walletVersion, mnemonic.mnemonic)
        val lastTransactionId = getLastTransactionId(walletAddress)
        if (lastTransactionId == null) {
            finishUpdate(emptyList())
            return
        }
        val transactions = loadTransactions(walletAddress, lastTransactionId)
        if (transactions.isSuccess) {
            finishUpdate(transactions.getOrNull() ?: emptyList())
        } else {
            modifyState {
                repositoryState.copy(transactionsState = LoadingState.Completed())
            }
        }
    }

    fun loadNextTransactions(byUser: Boolean = true) {
        if (byUser) {
            autoLoadTransactionsCount = 0
        }
        if (!canLoadNextTransactions()) {
            return
        }
        loadTransactionsJob = launchScope.launch {
            checkAndLoadNextTransactionsInternal()
        }
    }

    private fun canLoadNextTransactions(): Boolean {
        return repositoryState.transactionsState != LoadingState.Loading &&
                repositoryState.haveMoreTransactions
    }

    private suspend fun checkAndLoadNextTransactionsInternal() {
        if (!canLoadNextTransactions()) {
            return
        }
        modifyState {
            repositoryState.copy(transactionsState = LoadingState.Loading)
        }
        withContext(dispatchers.io) {
            loadNextTransactionsInternal()
        }
        if (autoLoadTransactionsCount < 5) {
            autoLoadTransactionsCount++
            loadNextTransactions(false)
        }
    }

    private suspend fun loadNextTransactionsInternal() {
        val finishUpdate: suspend (transactions: List<CompletedTransaction>) -> Unit =
            { transactions ->
                val wallet = repositoryState.wallet
                val newWallet = if (wallet is Wallet.UserWallet) {
                    val newTransactions = wallet.completedTransactions.toMutableList().apply {
                        addAll(transactions)
                    }
                    if (newTransactions.isNotEmpty()) {
                        val version = localDs.getWalletVersion().getOrThrow()
                        localDs.saveTransactions(
                            newTransactions.subList(
                                0,
                                min(newTransactions.size, 10)
                            ), version
                        )
                    }
                    wallet.copy(completedTransactions = newTransactions)
                } else {
                    wallet
                }
                modifyState {
                    repositoryState.copy(
                        wallet = newWallet,
                        transactionsState = LoadingState.Completed(),
                        haveMoreTransactions = transactions.isNotEmpty()
                    )
                }
            }

        val wallet = repositoryState.wallet
        val lastTransactionId = if (wallet is Wallet.UserWallet) {
            wallet.completedTransactions.lastOrNull()?.id
        } else {
            null
        }
        if (lastTransactionId == null) {
            finishUpdate(emptyList())
            return
        }
        val mnemonic = localDs.getWalletMnemonic().getOrNull()
        val walletVersion = localDs.getWalletVersion().getOrNull()
        if (mnemonic == null || walletVersion == null) {
            finishUpdate(emptyList())
            return
        }
        val walletAddress = getWalletAddress(walletVersion, mnemonic.mnemonic)
        val transactions = loadTransactions(walletAddress, lastTransactionId)
        if (transactions.isSuccess) {
            val list = transactions.getOrNull() ?: emptyList()
            finishUpdate(list.subList(1, list.size))
        } else {
            modifyState {
                repositoryState.copy(transactionsState = LoadingState.Completed())
            }
        }
    }

    private suspend fun loadTransactions(
        accountAddress: AddrStd,
        fromTransactionId: TransactionId
    ): Result<List<CompletedTransaction>> {
        var transactions: List<CompletedTransaction> = emptyList()
        var result: Result<List<CompletedTransaction>>
        var attempt = 0
        var successful = false
        while (attempt < 4 && transactions.size < 2) {
            result = remoteDs.getTransactions(accountAddress, fromTransactionId)
            val tr = result.getOrNull() ?: emptyList()
            successful = successful or result.isSuccess
            if (tr.size > transactions.size) {
                transactions = tr
            }
            attempt++
        }
        return if (successful) {
            Result.success(transactions)
        } else {
            Result.failure(Exception())
        }
    }

    private suspend fun getLastTransactionId(accountAddress: AddrStd): TransactionId? {
        val accountState = remoteDs.getAccountState(accountAddress).getOrNull()
        return accountState?.lastTransactionId
    }

    suspend fun setWalletVersion(newVersion: WalletVersion) {
        val version = localDs.getWalletVersion().getOrNull()
        if (newVersion == version) {
            return
        }
        localDs.saveWalletVersion(newVersion)
        loadTransactionsJob?.cancel()
        var wallet = repositoryState.wallet
        if (wallet !is Wallet.UserWallet) {
            return
        }
        val cachedBalance = getCachedBalance(newVersion).getOrNull() ?: TonCoins()
        wallet = wallet.copy(
            version = newVersion,
            balance = cachedBalance,
            completedTransactions = emptyList()
        )
        modifyState {
            repositoryState.copy(
                wallet = wallet,
                transactionsState = LoadingState.Completed(),
                haveMoreTransactions = true
            )
        }
        val balance = getBalance().getOrNull() ?: TonCoins()
        modifyState {
            repositoryState.copy(wallet = wallet.copy(balance = balance))
        }
        reloadTransactions()
    }
}

data class RepositoryState(
    val wallet: Wallet = Wallet.Empty,
    val walletState: LoadingState = LoadingState.Loading,
    val transactionsState: LoadingState = LoadingState.Completed(),
    val haveMoreTransactions: Boolean = true
)

sealed class LoadingState {
    object Loading : LoadingState()

    data class Completed(val error: Throwable? = null) : LoadingState() {

        val isSuccessful: Boolean = error == null
    }
}