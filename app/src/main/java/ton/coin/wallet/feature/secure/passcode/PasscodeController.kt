package ton.coin.wallet.feature.secure.passcode

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.airbnb.lottie.LottieAnimationView
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.alignCenter
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.custom.NumericKeyboard
import ton.coin.wallet.common.ui.custom.SecureInputView
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.lightToolbar
import ton.coin.wallet.common.ui.popupTextButton
import ton.coin.wallet.common.ui.textButton
import ton.coin.wallet.common.ui.titleText
import ton.coin.wallet.feature.walletsetup.StepCreateDone
import ton.coin.wallet.feature.walletsetup.StepImportDone

class PasscodeController(private val source: String = "") : ViewModelController() {

    private val viewModel: PasscodeViewModel by lazy {
        PasscodeViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: LinearLayout? = null
    private var animationView: LottieAnimationView? = null
    private var secureInput: SecureInputView? = null
    private var title: TextView? = null
    private var description: TextView? = null
    private var optionsButton: TextView? = null
    private var popup: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
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
            addView(titleText(context).apply {
                title = this
                setText(R.string.passcode_set_title)
                alignCenter()
            }, LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                gravity = Gravity.CENTER
                setMargins(40.dp(), 12.dp(), 40.dp(), 12.dp())
            })
            addView(bodyText(context).apply {
                description = this
                text = context.getString(R.string.passcode_set_mode, viewModel.state.length)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build())
            addView(SecureInputView(context).apply {
                secureInput = this
            }, LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                gravity = Gravity.CENTER
                setMargins(40.dp(), 40.dp(), 40.dp(), 0)
            })
            addView(textButton(context).apply {
                optionsButton = this
                setText(R.string.passcode_set_options_action)
                setOnClickListener { showOptionsPopup() }
            }, LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                gravity = Gravity.CENTER
                setMargins(40.dp(), 43.dp(), 40.dp(), 0)
            })

            addView(NumericKeyboard(context).apply {
                decimalEnabled = false
                keyPressListener = { key ->
                    viewModel.onKeyPressed(key)
                    refreshAnimation()
                }
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(10.dp(), 16.dp(), 10.dp(), 16.dp())
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
            setBackgroundResource(R.color.white)
            orientation = LinearLayout.VERTICAL
            addView(lightToolbar(context).apply {
                navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_back_24).apply {
                    this?.setTint(ContextCompat.getColor(context, R.color.black))
                }
                setNavigationOnClickListener { onNavigationPressed() }
                elevation = 0f
            }, LinearLayoutLpBuilder().wMatch().build())
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

    private fun showOptionsPopup() {
        val context = activity ?: return
        val popupElevation = 2.dp()
        val popupView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(popupTextButton(context).apply {
                text = context.getString(R.string.passcode_set_mode_item, 4)
                setOnClickListener {
                    viewModel.onModeChange(4)
                    popup?.dismiss()
                }
            })
            addView(popupTextButton(context).apply {
                text = context.getString(R.string.passcode_set_mode_item, 6)
                setOnClickListener {
                    viewModel.onModeChange(6)
                    popup?.dismiss()
                }
            })
        }
        val location = intArrayOf(0, 0)
        val target = optionsButton ?: return
        target.getLocationInWindow(location)
        val showAction = { popupToDisplay: PopupWindow ->
            popupToDisplay.showAtLocation(
                target,
                Gravity.START or Gravity.TOP,
                location[0],
                location[1] - (64.dp() + (optionsButton?.measuredHeight ?: 0))
            )
        }
        popup = PopupWindow(popupView, 200.dp(), ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
            animationStyle = R.style.PopupAnimationGravityBottom
            elevation = popupElevation.toFloat()
            setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_rect_round_6dp))
        }.apply {
            showAction(this)
        }
    }

    private fun onNavigationPressed() {
        router.popCurrentController()
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        lifecycleScope.launch {
            viewModel.stateFlow.collect {
                onStateChanged(it)
            }
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        popup?.dismiss()
    }

    private fun onStateChanged(state: ScreenData) {
        val context = activity ?: return
        secureInput?.mode = state.length
        description?.text = context.getString(R.string.passcode_set_mode, viewModel.state.length)
        when (state.state) {
            is ScreenState.Passcode -> {
                secureInput?.checkedCount = state.passcode.length
                title?.setText(R.string.passcode_set_title)
                optionsButton?.visibility = View.VISIBLE
            }

            is ScreenState.CheckPasscode -> {
                secureInput?.checkedCount = state.checkPasscode.length
                title?.setText(R.string.passcode_set_confirm_title)
                optionsButton?.visibility = View.INVISIBLE
            }

            is ScreenState.Error -> {
                secureInput?.checkedCount = state.checkPasscode.length
            }

            is ScreenState.Redirection -> {
                onDone()
            }

            else -> {}
        }
    }

    private fun onDone() {
        val route = if (source == "import") {
            StepImportDone()
        } else {
            StepCreateDone()
        }
        router.setRoot(
            RouterTransaction.with(route)
                .pushChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                .popChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
        )
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