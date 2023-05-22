package ton.coin.wallet.feature.welcome

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.repository.TonRepository

class WelcomeViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _state: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Empty)
    val state: Flow<ScreenState> = _state.asStateFlow()

    fun createWallet() {
        onStateChanged(ScreenState.Loading)
        lifecycleScope.launch {
            val wallet = tonRepository.createNewWallet()
            if (wallet.getOrNull() != null) {
                onStateChanged(ScreenState.New)
            } else {
                resetState()
            }
        }
    }

    fun resetState() {
        onStateChanged(ScreenState.Empty)
    }

    fun importWallet() {
        onStateChanged(ScreenState.Import)
    }

    private fun onStateChanged(newState: ScreenState) {
        _state.tryEmit(newState)
    }
}

sealed class ScreenState {
    object Empty : ScreenState()

    object Import : ScreenState()

    object New : ScreenState()

    object Loading : ScreenState()
}