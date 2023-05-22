package ton.coin.wallet.feature.send.comment

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.common.ui.formatter.TonCoinsFormatter
import ton.coin.wallet.data.DraftTransaction
import ton.coin.wallet.data.TonCoins
import ton.coin.wallet.data.Wallet
import ton.coin.wallet.repository.TonRepository

class SendCommentViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _stateFlow: MutableStateFlow<ScreenData> = MutableStateFlow(ScreenData())
    val stateFlow: StateFlow<ScreenData> = _stateFlow.asStateFlow()
    val state: ScreenData get() = stateFlow.value

    init {
        loadData()
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
        if (wallet.pendingTransactions.isNotEmpty()) {
            onScreenDataChanged(state.copy(status = ScreenState.Redirection("successfull")))
        }
    }

    fun loadData() {
        if (state.status.isLoading()) {
            return
        }
        onScreenDataChanged(state.copy(status = ScreenState.Loading))
        lifecycleScope.launch {
            var transaction = tonRepository.getDraftTransaction().getOrNull()
            if (transaction == null) {
                transaction = DraftTransaction()
                tonRepository.saveDraftTransaction(transaction)
            }
            onScreenDataChanged(
                state.copy(
                    status = ScreenState.Successful,
                    draftTransaction = transaction,
                    amountToDisplay = TonCoinsFormatter.format(
                        transaction.amount ?: TonCoins(), scale = 4
                    )
                )
            )
        }
    }

    fun onCommentChanged(comment: String) {
        val transaction = state.draftTransaction.copy(comment = comment)
        lifecycleScope.launch {
            tonRepository.saveDraftTransaction(transaction)
        }
        onScreenDataChanged(state.copy(draftTransaction = transaction))
    }

    fun onContinuePressed() {
        if (state.status.isLoading()) {
            return
        }
        onScreenDataChanged(state.copy(status = ScreenState.Transfering))
        cleanupScope.launch {
            val transaction = tonRepository.getDraftTransaction().getOrNull()
            if (transaction == null) {
                onScreenDataChanged(state.copy(status = ScreenState.Successful))
                return@launch
            }
            val result = tonRepository.processTransaction(transaction)
            onScreenDataChanged(state.copy(status = ScreenState.Successful))
        }
    }

    fun consumeRedirection() {
        onScreenDataChanged(state.copy(status = ScreenState.Successful))
    }

    private fun onScreenDataChanged(newData: ScreenData) {
        _stateFlow.tryEmit(newData)
    }
}

data class ScreenData(
    val draftTransaction: DraftTransaction = DraftTransaction(),
    val amountToDisplay: String = "",
    val status: ScreenState = ScreenState.Pure
)

sealed class ScreenState {
    object Pure : ScreenState()

    object Loading : ScreenState()

    object Transfering : ScreenState()

    object Successful : ScreenState()

    data class Error(val error: Throwable) : ScreenState()

    data class Redirection(val target: String) : ScreenState()

    fun isLoading(): Boolean {
        return this is Loading || this is Transfering
    }
}