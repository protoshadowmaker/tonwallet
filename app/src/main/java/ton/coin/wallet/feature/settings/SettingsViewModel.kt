package ton.coin.wallet.feature.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.data.WalletVersion
import ton.coin.wallet.repository.TonRepository

class SettingsViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _stateFlow: MutableStateFlow<ScreenData> = MutableStateFlow(ScreenData())
    val stateFlow: StateFlow<ScreenData> = _stateFlow.asStateFlow()
    val state: ScreenData get() = stateFlow.value

    init {
        lifecycleScope.launch {
            onScreenDataChanged(
                state.copy(
                    walletVersion = tonRepository.getWalletVersion().getOrThrow()
                )
            )
        }
    }

    fun logOut() {
        if (state.status == ScreenState.Loading) {
            return
        }
        onScreenDataChanged(state.copy(status = ScreenState.Loading))
        lifecycleScope.launch {
            tonRepository.deleteWallet()
            onScreenDataChanged(state.copy(status = ScreenState.Redirection("welcome")))
        }
    }

    fun onWalletVersionSelected(walletVersion: WalletVersion) {
        onScreenDataChanged(state.copy(walletVersion = walletVersion))
        cleanupScope.launch {
            tonRepository.setWalletVersion(walletVersion)
        }
    }

    private fun onScreenDataChanged(newData: ScreenData) {
        _stateFlow.tryEmit(newData)
    }
}

data class ScreenData(
    val walletVersion: WalletVersion = WalletVersion.V4R2,
    val status: ScreenState = ScreenState.Pure
)

sealed class ScreenState {
    object Pure : ScreenState()

    object Loading : ScreenState()

    object Successful : ScreenState()

    data class Error(val error: Throwable) : ScreenState()

    data class Redirection(val target: String) : ScreenState()
}