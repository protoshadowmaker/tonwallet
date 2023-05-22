package ton.coin.wallet.feature.send.amount

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.data.DraftTransaction
import ton.coin.wallet.data.TonCoins
import ton.coin.wallet.repository.TonRepository
import java.math.BigDecimal

class SendAmountViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _stateFlow: MutableStateFlow<ScreenData> = MutableStateFlow(ScreenData())
    val stateFlow: StateFlow<ScreenData> = _stateFlow.asStateFlow()
    val state: ScreenData get() = stateFlow.value

    init {
        loadData()
    }

    fun loadData() {
        if (state.status == ScreenState.Loading) {
            return
        }
        onScreenDataChanged(state.copy(status = ScreenState.Loading))
        lifecycleScope.launch {
            var transaction = tonRepository.getDraftTransaction().getOrNull()
            if (transaction == null) {
                transaction = DraftTransaction()
                tonRepository.saveDraftTransaction(transaction)
            }
            val cachedBalance = tonRepository.getCachedBalance().getOrNull() ?: TonCoins()
            onScreenDataChanged(
                state.copy(
                    status = ScreenState.Successful,
                    draftTransaction = transaction,
                    availableBalance = cachedBalance
                )
            )
            val availableBalance = tonRepository.getBalance().getOrNull() ?: cachedBalance
            onScreenDataChanged(
                state.copy(
                    status = ScreenState.Successful,
                    draftTransaction = transaction,
                    availableBalance = availableBalance
                )
            )
        }
    }

    fun onAmountChanged(amount: String) {
        val value = try {
            TonCoins(BigDecimal(amount).multiply(BigDecimal.valueOf(1_000_000_000)).toBigInteger())
        } catch (e: Throwable) {
            null
        }
        val transaction = state.draftTransaction.copy(amount = value)
        lifecycleScope.launch {
            tonRepository.saveDraftTransaction(transaction)
        }
        onScreenDataChanged(state.copy(draftTransaction = transaction))
    }

    fun onContinuePressed() {
        if (state.status == ScreenState.Loading) {
            return
        }
        onScreenDataChanged(state.copy(status = ScreenState.Loading))
        lifecycleScope.launch {
            onScreenDataChanged(state.copy(status = ScreenState.Redirection("comment")))
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
    val availableBalance: TonCoins = TonCoins(),
    val status: ScreenState = ScreenState.Pure
)

sealed class ScreenState {
    object Pure : ScreenState()

    object Loading : ScreenState()

    object Successful : ScreenState()

    data class Error(val error: Throwable) : ScreenState()

    data class Redirection(val target: String) : ScreenState()
}