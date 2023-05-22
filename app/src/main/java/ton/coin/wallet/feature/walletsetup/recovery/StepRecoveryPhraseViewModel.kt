package ton.coin.wallet.feature.walletsetup.recovery

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.data.Wallet
import ton.coin.wallet.data.WalletMnemonic
import ton.coin.wallet.repository.TonRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class StepRecoveryPhraseViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _state: MutableStateFlow<ScreenState> = MutableStateFlow(
        ScreenState()
    )
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    init {
        lifecycleScope.launch {
            val wallet = tonRepository.repositoryStateFlow.value.wallet
            if (wallet is Wallet.DraftWallet) {
                onStateChanged(state.value.copy(mnemonic = wallet.mnemonic))
            } else {
                onStateChanged(state.value.copy(mnemonic = WalletMnemonic()))
            }
        }
    }

    fun onDone() {
        val diff = state.value.displayDate.until(LocalDateTime.now(), ChronoUnit.SECONDS)
        if (diff < 60) {
            val skipCount = state.value.skipAttemptCount + 1
            val action = when (skipCount) {
                1 -> OneTimeAction.WARNING
                else -> OneTimeAction.SECOND_WARNING
            }
            onStateChanged(state.value.copy(skipAttemptCount = skipCount, oneTimeAction = action))
        } else {
            onStateChanged(state.value.copy(oneTimeAction = OneTimeAction.CHECKING))
        }
    }

    fun onSkip() {
        onStateChanged(state.value.copy(oneTimeAction = OneTimeAction.CHECKING))
    }

    fun consumeOneTimeAction() {
        onStateChanged(state.value.copy(oneTimeAction = null))
    }

    private fun onStateChanged(newState: ScreenState) {
        _state.tryEmit(newState)
    }
}

data class ScreenState(
    val skipAttemptCount: Int = 0,
    val oneTimeAction: OneTimeAction? = null,
    val displayDate: LocalDateTime = LocalDateTime.now(),
    val mnemonic: WalletMnemonic = WalletMnemonic()
)

enum class OneTimeAction {
    WARNING, SECOND_WARNING, CHECKING
}