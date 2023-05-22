package ton.coin.wallet.feature.settings.mnemonic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.data.Wallet
import ton.coin.wallet.data.WalletMnemonic
import ton.coin.wallet.repository.TonRepository

class ViewMnemonicViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _state: MutableStateFlow<ScreenState> = MutableStateFlow(
        ScreenState()
    )
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    init {
        lifecycleScope.launch {
            val wallet = tonRepository.repositoryStateFlow.value.wallet
            if (wallet is Wallet.UserWallet) {
                onStateChanged(state.value.copy(mnemonic = wallet.mnemonic))
            }
        }
    }

    private fun onStateChanged(newState: ScreenState) {
        _state.tryEmit(newState)
    }
}

data class ScreenState(
    val mnemonic: WalletMnemonic = WalletMnemonic()
)