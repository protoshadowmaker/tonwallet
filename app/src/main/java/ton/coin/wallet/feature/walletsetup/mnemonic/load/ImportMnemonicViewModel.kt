package ton.coin.wallet.feature.walletsetup.mnemonic.load

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.data.Wallet
import ton.coin.wallet.data.WalletMnemonic
import ton.coin.wallet.feature.walletsetup.mnemonic.MnemonicInputViewModel
import ton.coin.wallet.repository.TonRepository

class ImportMnemonicViewModel : MnemonicInputViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _stateFlow: MutableStateFlow<ScreenState> = MutableStateFlow(
        ScreenState()
    )
    val stateFlow: StateFlow<ScreenState> = _stateFlow.asStateFlow()
    val state: ScreenState get() = stateFlow.value

    fun importAccount(mnemonic: List<String>) {
        if (state.loading) {
            return
        }
        onStateChanged(state.copy(loading = true))
        lifecycleScope.launch {
            val exists = tonRepository.isWalletValid(mnemonic)
            if (exists.getOrNull() == true) {
                tonRepository.saveDraftWallet(Wallet.DraftWallet(WalletMnemonic(mnemonic)))
                onStateChanged(state.copy(loading = false, oneTimeAction = OneTimeAction.PERFECT))
            } else {
                onStateChanged(
                    state.copy(
                        loading = false,
                        oneTimeAction = OneTimeAction.WARNING_WORDS
                    )
                )
            }
        }
    }

    fun consumeOneTimeAction() {
        onStateChanged(stateFlow.value.copy(oneTimeAction = null))
    }

    private fun onStateChanged(newState: ScreenState) {
        _stateFlow.tryEmit(newState)
    }
}

data class ScreenState(
    val loading: Boolean = false,
    val oneTimeAction: OneTimeAction? = null,
    val mnemonic: WalletMnemonic = WalletMnemonic()
)

enum class OneTimeAction {
    WARNING_WORDS, PERFECT
}