package ton.coin.wallet.feature.secure.lock

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.repository.TonRepository
import ton.coin.wallet.util.removeLast

class LockViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _stateFlow: MutableStateFlow<ScreenData> = MutableStateFlow(ScreenData())
    val stateFlow: StateFlow<ScreenData> = _stateFlow.asStateFlow()
    val state: ScreenData get() = stateFlow.value
    private var processJob: Job? = null

    init {
        processJob = lifecycleScope.launch {
            val passcode = tonRepository.getPinCode()
            onScreenDataChanged(state.copy(length = passcode.length))
            if (passcode.isEmpty()) {
                onScreenDataChanged(state.copy(state = ScreenState.Successful))
            }
        }
    }

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
            onScreenDataChanged(
                state.copy(state = ScreenState.Passcode)
            )
        }
        when {
            key == "e" -> {
                onScreenDataChanged(state.copy(passcode = state.passcode.removeLast()))
            }

            state.passcode.length < state.length - 1 -> {
                onScreenDataChanged(state.copy(passcode = state.passcode + key))
            }

            state.passcode.length == state.length - 1 -> {
                var passcode = state.passcode + key
                onScreenDataChanged(state.copy(passcode = passcode))
                delay(300)
                val newState = if (passcode == tonRepository.getPinCode()) {
                    ScreenState.Successful
                } else {
                    passcode = ""
                    ScreenState.Error
                }
                onScreenDataChanged(state.copy(state = newState, passcode = passcode))
            }
        }
    }

    private fun isProcessing(): Boolean {
        return processJob?.isActive == true || state.state == ScreenState.Successful
    }

    private fun onScreenDataChanged(newData: ScreenData) {
        _stateFlow.tryEmit(newData)
    }
}

data class ScreenData(
    val state: ScreenState = ScreenState.Passcode,
    val length: Int = 4,
    val passcode: String = "",
)

sealed class ScreenState {
    object Passcode : ScreenState()

    object Successful : ScreenState()

    object Error : ScreenState()
}