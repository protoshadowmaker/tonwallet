package ton.coin.wallet.feature.receive

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.repository.TonRepository

class ReceiveViewModel : ConductorViewModel() {
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
            val walletAddress = tonRepository.getWalletAddress().getOrNull() ?: state.walletAddress
            onScreenDataChanged(
                state.copy(
                    status = ScreenState.Successful,
                    walletAddress = walletAddress,
                    sharedWalletAddress = "ton://transfer/$walletAddress"
                )
            )
        }
    }

    private fun onScreenDataChanged(newData: ScreenData) {
        _stateFlow.tryEmit(newData)
    }
}

data class ScreenData(
    val walletAddress: String = "",
    val sharedWalletAddress: String = "",
    val status: ScreenState = ScreenState.Pure
)

sealed class ScreenState {
    object Pure : ScreenState()

    object Loading : ScreenState()

    object Successful : ScreenState()

    data class Error(val error: Throwable) : ScreenState()
}