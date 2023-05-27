package ton.coin.wallet.common.lifecycle

import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bluelinelabs.conductor.Controller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import org.koin.core.component.KoinComponent

abstract class ViewModelController : Controller(), KoinComponent {
    val job = SupervisorJob()
    val lifecycleScope = CoroutineScope(job + Dispatchers.Main)

    private val viewModels = mutableListOf<ConductorViewModel>()

    var systemBarInsets = Insets.NONE
        private set
    var imeInsets = Insets.NONE
        private set

    fun attachViewModel(viewModel: ConductorViewModel) {
        viewModels.add(viewModel)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            onInsetsChanged()
            windowInsets
        }
        ViewCompat.requestApplyInsets(view)
    }

    open fun onInsetsChanged() {}

    override fun onDetach(view: View) {
        super.onDetach(view)
        job.cancelChildren()
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModels.forEach { it.clear() }
    }

    fun hideKeyboard() {
        val activity = this.activity ?: return
        try {
            val imm = activity.getSystemService(InputMethodManager::class.java)
            imm.hideSoftInputFromWindow((activity.currentFocus ?: View(activity)).windowToken, 0)
        } catch (ignored: Throwable) {
        }
    }


    fun disableScreenShot() {
        setFlagSecure(true)
    }

    fun enableScreenShot() {
        setFlagSecure(false)
    }

    private fun setFlagSecure(disabled: Boolean) {
        val activity = this.activity ?: return
        if (disabled) {
            try {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } catch (ignore: Exception) {
            }
        } else {
            try {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } catch (ignore: Exception) {
            }
        }
    }

}