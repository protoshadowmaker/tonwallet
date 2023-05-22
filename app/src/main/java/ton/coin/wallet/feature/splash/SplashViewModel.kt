package ton.coin.wallet.feature.splash

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.data.Wallet
import ton.coin.wallet.repository.LoadingState
import ton.coin.wallet.repository.TonRepository

class SplashViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()

    private val _state: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Loading)
    val state: Flow<ScreenState> = _state.asStateFlow()

    init {
        lifecycleScope.launch {
            tonRepository.repositoryStateFlow.collect { repository ->
                val walletState = repository.walletState
                if (walletState is LoadingState.Completed) {
                    when (repository.wallet) {
                        is Wallet.UserWallet -> onStateChanged(ScreenState.Home)
                        is Wallet.Empty, is Wallet.DraftWallet -> onStateChanged(ScreenState.Welcome)
                    }
                }
            }
        }
    }

    private suspend fun onStateChanged(newState: ScreenState) = withContext(dispatchers.main) {
        _state.tryEmit(newState)
    }
}

sealed class ScreenState {
    object Loading : ScreenState()

    object Welcome : ScreenState()

    object Home : ScreenState()
}