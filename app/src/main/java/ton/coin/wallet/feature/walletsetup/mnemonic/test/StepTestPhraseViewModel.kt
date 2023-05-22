package ton.coin.wallet.feature.walletsetup.mnemonic.test

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.inject
import ton.coin.wallet.data.Wallet
import ton.coin.wallet.data.WalletMnemonic
import ton.coin.wallet.feature.walletsetup.mnemonic.MnemonicInputViewModel
import ton.coin.wallet.repository.TonRepository
import kotlin.random.Random

class StepTestPhraseViewModel : MnemonicInputViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _state: MutableStateFlow<ScreenState> = MutableStateFlow(
        ScreenState()
    )
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    init {
        val wallet = tonRepository.repositoryStateFlow.value.wallet
        if (wallet is Wallet.DraftWallet) {
            val indexes = mutableSetOf<Int>()
            val random = Random(System.currentTimeMillis())
            while (indexes.size < 3) {
                indexes.add(random.nextInt(24))
            }
            onStateChanged(
                state.value.copy(
                    mnemonic = wallet.mnemonic, checkIndexes = indexes.toList().sorted()
                )
            )
        } else {
            onStateChanged(state.value.copy(mnemonic = WalletMnemonic()))
        }
    }

    fun onContinue(words: List<String>) {
        val indexes = state.value.checkIndexes
        val mnemonic = state.value.mnemonic.mnemonic
        if (indexes.isEmpty() || mnemonic.isEmpty()) {
            return
        }
        var valid = true
        indexes.forEachIndexed { listIndex, wordIndex ->
            if (words[listIndex].trim() != mnemonic[wordIndex]) {
                valid = false
            }
        }
        onStateChanged(
            state.value.copy(
                oneTimeAction = if (valid) OneTimeAction.PERFECT else OneTimeAction.WARNING_WORDS
            )
        )
    }

    fun consumeOneTimeAction() {
        onStateChanged(state.value.copy(oneTimeAction = null))
    }

    private fun onStateChanged(newState: ScreenState) {
        _state.tryEmit(newState)
    }
}

data class ScreenState(
    val oneTimeAction: OneTimeAction? = null,
    val checkIndexes: List<Int> = emptyList(),
    val mnemonic: WalletMnemonic = WalletMnemonic()
)

enum class OneTimeAction {
    WARNING_WORDS, PERFECT
}