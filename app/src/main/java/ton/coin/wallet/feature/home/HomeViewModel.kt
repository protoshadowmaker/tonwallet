package ton.coin.wallet.feature.home

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.data.CompletedTransaction
import ton.coin.wallet.data.PendingTransaction
import ton.coin.wallet.data.TonCoins
import ton.coin.wallet.data.Wallet
import ton.coin.wallet.repository.LoadingState
import ton.coin.wallet.repository.RepositoryState
import ton.coin.wallet.repository.TonRepository

class HomeViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _stateFlow: MutableStateFlow<ScreenData> = MutableStateFlow(ScreenData())
    val stateFlow: StateFlow<ScreenData> = _stateFlow.asStateFlow()
    val state: ScreenData get() = stateFlow.value

    init {
        reloadData()
        lifecycleScope.launch {
            tonRepository.repositoryStateFlow.collect {
                onRepositoryStateChanged(it)
            }
        }
    }

    fun reloadData() {
        if (state.status == ScreenState.Loading) {
            return
        }
        onScreenDataChanged(state.copy(status = ScreenState.Loading))
        lifecycleScope.launch {
            val cachedBalance = tonRepository.getCachedBalance().getOrNull()
            val walletAddress = tonRepository.getWalletAddress().getOrNull()
            onScreenDataChanged(
                state.copy(
                    status = ScreenState.Loading,
                    balance = cachedBalance ?: state.balance,
                    walletAddress = walletAddress ?: state.walletAddress
                )
            )
            val balance = tonRepository.getBalance().getOrNull()
            onScreenDataChanged(
                state.copy(
                    status = ScreenState.Successful,
                    balance = balance ?: state.balance,
                )
            )
        }
        reloadTransactions()
    }

    fun loadNextTransactions() {
        tonRepository.loadNextTransactions()
    }

    fun reloadTransactions() {
        tonRepository.reloadTransactions()
    }

    private suspend fun onRepositoryStateChanged(state: RepositoryState) {
        val wallet = state.wallet
        if (wallet is Wallet.UserWallet) {
            val cachedBalance = tonRepository.getCachedBalance().getOrNull()
            val walletAddress = tonRepository.getWalletAddress().getOrNull()
            onScreenDataChanged(
                this.state.copy(
                    balance = cachedBalance ?: this.state.balance,
                    walletAddress = walletAddress ?: this.state.walletAddress,
                    pendingTransactions = wallet.pendingTransactions,
                    completedTransactions = wallet.completedTransactions,
                    transactionsStatus = if (state.transactionsState is LoadingState.Completed) {
                        ScreenState.Successful
                    } else {
                        ScreenState.Loading
                    }
                )
            )
        }
    }

    private fun onScreenDataChanged(newData: ScreenData) {
        _stateFlow.tryEmit(newData)
    }
}

data class ScreenData(
    val balance: TonCoins = TonCoins(),
    val walletAddress: String = "",
    val pendingTransactions: List<PendingTransaction> = emptyList(),
    val completedTransactions: List<CompletedTransaction> = emptyList(),
    val status: ScreenState = ScreenState.Pure,
    val transactionsStatus: ScreenState = ScreenState.Pure
) {
    val isLoadingState: Boolean get() = status == ScreenState.Loading || transactionsStatus == ScreenState.Loading
}

sealed class ScreenState {
    object Pure : ScreenState()

    object Loading : ScreenState()

    object Successful : ScreenState()

    data class Error(val error: Throwable) : ScreenState()

    data class Redirection(val target: String) : ScreenState()
}