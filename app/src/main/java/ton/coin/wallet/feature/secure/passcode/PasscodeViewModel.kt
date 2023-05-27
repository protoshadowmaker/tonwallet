package ton.coin.wallet.feature.secure.passcode

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.data.Wallet
import ton.coin.wallet.repository.TonRepository
import ton.coin.wallet.util.removeLast

class PasscodeViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _stateFlow: MutableStateFlow<ScreenData> = MutableStateFlow(ScreenData())
    val stateFlow: StateFlow<ScreenData> = _stateFlow.asStateFlow()
    val state: ScreenData get() = stateFlow.value
    private var processJob: Job? = null

    fun onKeyPressed(key: String) {
        if (isProcessing()) {
            return
        }
        processJob = lifecycleScope.launch {
            processKeyPressed(key)
        }
    }

    private suspend fun processKeyPressed(key: String) {
        if (state.state is ScreenState.Error) {
            val passcode: String
            val checkPasscode: String
            val newState = if (key == "e") {
                passcode = ""
                checkPasscode = ""
                ScreenState.Passcode
            } else {
                passcode = state.passcode
                checkPasscode = key
                ScreenState.CheckPasscode
            }
            onScreenDataChanged(
                state.copy(
                    state = newState, passcode = passcode, checkPasscode = checkPasscode
                )
            )
            return
        }
        if (state.state is ScreenState.Passcode) {
            when {
                key == "e" -> {
                    onScreenDataChanged(state.copy(passcode = state.passcode.removeLast()))
                }

                state.passcode.length < state.length - 1 -> {
                    onScreenDataChanged(state.copy(passcode = state.passcode + key))
                }

                state.passcode.length == state.length - 1 -> {
                    onScreenDataChanged(state.copy(passcode = state.passcode + key))
                    delay(300)
                    onScreenDataChanged(state.copy(state = ScreenState.CheckPasscode))
                }
            }
        } else if (state.state is ScreenState.CheckPasscode) {
            when {
                key == "e" && state.checkPasscode.isEmpty() -> {
                    onScreenDataChanged(state.copy(state = ScreenState.Passcode, passcode = ""))
                }

                key == "e" -> {
                    onScreenDataChanged(state.copy(checkPasscode = state.checkPasscode.removeLast()))
                }

                state.checkPasscode.length < state.length - 1 -> {
                    onScreenDataChanged(state.copy(checkPasscode = state.checkPasscode + key))
                }

                state.checkPasscode.length == state.length - 1 -> {
                    var checkPasscode = state.checkPasscode + key
                    onScreenDataChanged(state.copy(checkPasscode = checkPasscode))
                    delay(300)
                    val newState = if (checkPasscode == state.passcode) {
                        ScreenState.Successful
                    } else {
                        checkPasscode = ""
                        ScreenState.Error(Throwable())
                    }
                    onScreenDataChanged(state.copy(state = newState, checkPasscode = checkPasscode))
                    if (newState is ScreenState.Successful) {
                        finish()
                    }
                }
            }
        }
    }

    private fun isProcessing(): Boolean {
        return processJob?.isActive == true
    }

    fun onModeChange(length: Int) {
        if (state.length == length || isProcessing()) {
            return
        }
        onScreenDataChanged(
            state.copy(
                state = ScreenState.Passcode, length = length, passcode = "", checkPasscode = ""
            )
        )
    }

    private fun finish() {
        processJob = cleanupScope.launch {
            saveWallet()
        }
    }

    private suspend fun saveWallet() {
        val wallet = tonRepository.repositoryState.wallet
        if (wallet !is Wallet.DraftWallet) {
            return
        }
        tonRepository.saveUserWallet(Wallet.UserWallet(wallet.mnemonic))
        tonRepository.saveSecureSettings(state.passcode)
        onScreenDataChanged(
            state.copy(state = ScreenState.Redirection("success"))
        )
    }

    private fun onScreenDataChanged(newData: ScreenData) {
        _stateFlow.tryEmit(newData)
    }
}

data class ScreenData(
    val state: ScreenState = ScreenState.Passcode,
    val length: Int = 4,
    val passcode: String = "",
    val checkPasscode: String = ""
)

sealed class ScreenState {
    object Passcode : ScreenState()

    object CheckPasscode : ScreenState()

    object Successful : ScreenState()

    data class Error(val error: Throwable) : ScreenState()

    data class Redirection(val target: String) : ScreenState()
}