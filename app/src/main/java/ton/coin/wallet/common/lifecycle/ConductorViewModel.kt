package ton.coin.wallet.common.lifecycle

import androidx.annotation.CallSuper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ton.coin.wallet.common.async.AppDispatchers

open class ConductorViewModel : KoinComponent {

    val dispatchers: AppDispatchers by inject()
    private val lifecycleJob = SupervisorJob()
    private val cleanupJob = SupervisorJob()
    val lifecycleScope = CoroutineScope(lifecycleJob + dispatchers.main)
    val cleanupScope = CoroutineScope(cleanupJob + dispatchers.main)

    @CallSuper
    open fun clear() {
        lifecycleJob.cancelChildren()
    }
}