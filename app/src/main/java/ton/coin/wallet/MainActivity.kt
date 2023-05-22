package ton.coin.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import ton.coin.wallet.feature.splash.SplashController

class MainActivity : ComponentActivity() {

    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val container = ChangeHandlerFrameLayout(this)
        setContentView(container);

        router = Conductor.attachRouter(this, container, savedInstanceState)
            .setPopRootControllerMode(Router.PopRootControllerMode.NEVER)
            .setOnBackPressedDispatcherEnabled(true)

        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(SplashController()))
        }
    }
}