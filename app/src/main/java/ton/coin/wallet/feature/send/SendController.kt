package ton.coin.wallet.feature.send

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.feature.send.recipient.SendRecipientController

class SendController : ViewModelController(), ControllerChangeHandler.ControllerChangeListener {

    private val viewModel: SendViewModel by lazy {
        SendViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: LinearLayout? = null
    private var card: LinearLayout? = null
    private var content: FrameLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?
    ): View {
        val context = container.context
        val contentLayout = LinearLayout(context).apply {
            card = this
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_rect_round_top_12dp)
            addView(ChangeHandlerFrameLayout(context).apply {
                id = View.generateViewId()
                content = this
                val childRouter =
                    getChildRouter(this).setPopRootControllerMode(Router.PopRootControllerMode.POP_ROOT_CONTROLLER_AND_VIEW)
                if (!childRouter.hasRootController()) {
                    childRouter.setRoot(RouterTransaction.with(SendRecipientController()))
                }
                childRouter.addChangeListener(this@SendController)
            }, LinearLayoutLpBuilder().wMatch().hMatch().build())
        }
        val root = LinearLayout(inflater.context).apply {
            setBackgroundResource(R.color.black)
            setOnClickListener { router.popCurrentController() }
            addView(contentLayout, FrameLayoutLpBuilder().wMatch().hMatch().build().apply {
                gravity = Gravity.BOTTOM
            })
        }
        this.root = root
        return root
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        root?.updatePadding(
            systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, 0
        )
    }

    override fun onChangeCompleted(
        to: Controller?,
        from: Controller?,
        isPush: Boolean,
        container: ViewGroup,
        handler: ControllerChangeHandler
    ) {
    }

    override fun onChangeStarted(
        to: Controller?,
        from: Controller?,
        isPush: Boolean,
        container: ViewGroup,
        handler: ControllerChangeHandler
    ) {
        val childRouter = getChildRouter(container)
        if (!childRouter.hasRootController() && !isPush) {
            router.popCurrentController()
        }
    }

    override fun onDestroy() {
        viewModel.cleanup()
        super.onDestroy()
    }
}