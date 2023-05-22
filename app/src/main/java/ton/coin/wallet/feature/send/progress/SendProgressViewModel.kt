package ton.coin.wallet.feature.send.progress

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.data.PendingTransaction
import ton.coin.wallet.data.Wallet
import ton.coin.wallet.repository.TonRepository

class SendProgressViewModel : ConductorViewModel() {
    private val tonRepository: TonRepository by inject()

    private val _stateFlow: MutableStateFlow<ScreenData> = MutableStateFlow(ScreenData())
    val stateFlow: StateFlow<ScreenData> = _stateFlow.asStateFlow()
    val state: ScreenData get() = stateFlow.value

    init {
        listenTransaction()
    }

    private fun listenTransaction() {
        lifecycleScope.launch {
            tonRepository.repositoryStateFlow.collect {
                val wallet = it.wallet
                if (wallet is Wallet.UserWallet) {
                    onWalletStateChanged(wallet)
                }
            }
        }
    }

    private fun onWalletStateChanged(wallet: Wallet.UserWallet) {
        if (wallet.pendingTransactions.isEmpty()) {
            onScreenDataChanged(state.copy(status = ScreenState.Successful))
        } else {
            onScreenDataChanged(state.copy(transaction = wallet.pendingTransactions.first()))
        }
    }

    private fun onScreenDataChanged(newData: ScreenData) {
        _stateFlow.tryEmit(newData)
    }
}

data class ScreenData(
    val transaction: PendingTransaction = PendingTransaction(),
    val status: ScreenState = ScreenState.Loading
)

sealed class ScreenState {
    object Pure : ScreenState()

    object Loading : ScreenState()

    object Successful : ScreenState()

    data class Error(val error: Throwable) : ScreenState()
}