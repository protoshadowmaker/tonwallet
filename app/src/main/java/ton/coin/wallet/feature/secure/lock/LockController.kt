package ton.coin.wallet.feature.secure.lock

import android.animation.LayoutTransition
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.view.updatePadding
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.alignCenter
import ton.coin.wallet.common.ui.bodyTextDark
import ton.coin.wallet.common.ui.custom.NumericKeyboard
import ton.coin.wallet.common.ui.custom.SecureInputView
import ton.coin.wallet.common.ui.dp

class LockController : ViewModelController() {

    private val viewModel: LockViewModel by lazy {
        LockViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: LinearLayout? = null
    private var animationView: LottieAnimationView? = null
    private var secureInput: SecureInputView? = null
    private var description: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        onBackPressedDispatcher?.addCallback(owner = lifecycleOwner) {
            activity?.moveTaskToBack(true)
        }
        val context = inflater.context
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutTransition = LayoutTransition().apply {
                setDuration(200)
                enableTransitionType(LayoutTransition.CHANGING)
            }
            addView(
                LottieAnimationView(context).apply {
                    animationView = this
                    setAnimation(R.raw.password)
                    playAnimation()
                }, LinearLayoutLpBuilder().wDp(100).hDp(100).build()
            )
            addView(bodyTextDark(context).apply {
                description = this
                text = context.getString(R.string.passcode_set_mode, viewModel.state.length)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 20.dp(), 0, 0)
            })
            addView(SecureInputView(context).apply {
                setColorScheme(
                    borderColor = Color.argb(0.32f, 1f, 1f, 1f),
                    solidColor = Color.WHITE
                )
                secureInput = this
            }, LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                gravity = Gravity.CENTER
                setMargins(40.dp(), 40.dp(), 40.dp(), 0)
            })

            addView(NumericKeyboard(context).apply {
                decimalEnabled = false
                setStyle(R.drawable.button_keyboard_digital_dark, Color.WHITE)
                keyPressListener = { key ->
                    viewModel.onKeyPressed(key)
                    refreshAnimation()
                }
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(10.dp(), 107.dp(), 10.dp(), 16.dp())
                weight = 0f
            })
        }
        val contentContainer = FrameLayout(context).apply {
            addView(
                content,
                FrameLayoutLpBuilder().wMatch().hWrap().build().apply {
                    gravity = Gravity.BOTTOM
                }
            )
        }
        val root = LinearLayout(inflater.context).apply {
            setOnClickListener { }
            setBackgroundResource(R.color.black)
            orientation = LinearLayout.VERTICAL
            addView(
                contentContainer,
                LinearLayoutLpBuilder().wMatch().hMatch().build()
            )
        }
        this.root = root
        return root
    }

    private fun refreshAnimation() {
        val view = animationView ?: return
        if (view.isAnimating) {
            return
        }
        view.playAnimation()
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        lifecycleScope.launch {
            viewModel.stateFlow.collect {
                onStateChanged(it)
            }
        }
        hideKeyboard()
    }

    private fun onStateChanged(state: ScreenData) {
        val context = activity ?: return
        secureInput?.mode = state.length
        description?.text = context.getString(R.string.passcode_set_mode, viewModel.state.length)
        when (state.state) {
            is ScreenState.Passcode -> {
                secureInput?.checkedCount = state.passcode.length
            }

            is ScreenState.Error -> {
                secureInput?.checkedCount = state.passcode.length
            }

            is ScreenState.Successful -> {
                onDone()
            }
        }
    }

    private fun onDone() {
        router.popCurrentController()
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        root?.updatePadding(
            systemBarInsets.left,
            systemBarInsets.top,
            systemBarInsets.right,
            systemBarInsets.bottom
        )
    }
}