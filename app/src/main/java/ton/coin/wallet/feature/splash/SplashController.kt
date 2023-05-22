package ton.coin.wallet.feature.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.launch
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.feature.home.HomeController
import ton.coin.wallet.feature.welcome.WelcomeController

class SplashController : ViewModelController() {

    private val viewModel: SplashViewModel by lazy {
        SplashViewModel().apply {
            attachViewModel(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        return FrameLayout(inflater.context)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        lifecycleScope.launch {
            viewModel.state.collect {
                onStateChanged(it)
            }
        }
    }

    private fun onStateChanged(state: ScreenState) {
        when (state) {
            is ScreenState.Home -> {
                router.replaceTopController(RouterTransaction.with(HomeController()))
            }

            is ScreenState.Welcome -> {
                router.replaceTopController(RouterTransaction.with(WelcomeController()))
            }

            is ScreenState.Loading -> {}
        }
    }
}