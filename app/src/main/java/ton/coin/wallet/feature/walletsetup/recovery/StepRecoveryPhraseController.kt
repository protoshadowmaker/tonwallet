package ton.coin.wallet.feature.walletsetup.recovery

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.airbnb.lottie.LottieAnimationView
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.*
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.feature.walletsetup.mnemonic.test.StepTestPhraseController

class StepRecoveryPhraseController : ViewModelController() {

    private val viewModel: StepRecoveryPhraseViewModel by lazy {
        StepRecoveryPhraseViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: LinearLayout? = null
    private var leftNumericTextView: TextView? = null
    private var rightNumericTextView: TextView? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val context = inflater.context
        val content = LinearLayout(context).apply {
            setPadding(40.dp(), 0, 40.dp(), 0)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(
                LottieAnimationView(context).apply {
                    setAnimation(R.raw.recovery_phrase)
                    playAnimation()
                }, LinearLayoutLpBuilder().wDp(100).hDp(100).build()
            )
            addView(titleText(context).apply {
                setText(R.string.recovery_title)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 12.dp(), 0, 12.dp())
            })
            addView(bodyText(context).apply {
                setText(R.string.recovery_description)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 0, 0, 40.dp())
            })

            val columnLinearLayout = LinearLayout(context).apply {
                weightSum = 2f
                addView(
                    FrameLayout(context).apply {
                        addView(
                            numericWordsText(context).apply {
                                leftNumericTextView = this
                            },
                            FrameLayoutLpBuilder().wWrap().hWrap().build().apply {
                                gravity = Gravity.CENTER
                            })
                    },
                    LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                        weight = 1f
                    })
                addView(
                    FrameLayout(context).apply {
                        addView(
                            numericWordsText(context).apply {
                                rightNumericTextView = this
                            },
                            FrameLayoutLpBuilder().wWrap().hWrap().build().apply {
                                gravity = Gravity.CENTER
                            })
                    },
                    LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                        weight = 1f
                    })
            }

            addView(columnLinearLayout, LinearLayoutLpBuilder().wMatch().hWrap().build())
            addView(
                coloredButton(context).apply {
                    setText(R.string.recovery_done)
                    setOnClickListener { onDonePressed() }
                },
                LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                    setMargins(0, 44.dp(), 0, 56.dp())
                }
            )
        }
        val contentContainer = ScrollView(context).apply {
            isVerticalScrollBarEnabled = false
            addView(content)
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

    override fun onAttach(view: View) {
        super.onAttach(view)
        lifecycleScope.launch {
            viewModel.state.collect {
                onStateChanged(it)
            }
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        alertDialog?.dismiss()
    }

    private fun onStateChanged(state: ScreenState) {
        if (state.mnemonic.mnemonic.size == 24) {
            leftNumericTextView?.setNumericWords(state.mnemonic.mnemonic.subList(0, 12))
            rightNumericTextView?.setNumericWords(
                state.mnemonic.mnemonic.subList(
                    12,
                    state.mnemonic.mnemonic.size
                ), 12
            )
        }
        when (state.oneTimeAction) {
            OneTimeAction.WARNING -> {
                showWarningDialog()
            }

            OneTimeAction.SECOND_WARNING -> {
                showWarningDialog { viewModel.onSkip() }
            }

            OneTimeAction.CHECKING -> {
                router.pushController(
                    RouterTransaction.with(StepTestPhraseController())
                        .pushChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                        .popChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                )
            }

            null -> {}
        }
        viewModel.consumeOneTimeAction()
    }

    private fun showWarningDialog(skipListener: (() -> Unit)? = null) {
        val context = activity ?: return
        alertDialog?.dismiss()
        val actions = mutableListOf<String>()
        val callbacks = mutableListOf<() -> Unit>()
        if (skipListener != null) {
            actions.add(context.getString(R.string.recovery_warning_skip))
            callbacks.add(skipListener)
        }
        actions.add(context.getString(R.string.recovery_warning_ok))
        callbacks.add { alertDialog?.dismiss() }
        alertDialog = AlertDialog.Builder(context, R.style.TonAlertDialog).setView(
            dialog(
                context,
                context.getString(R.string.recovery_warning_title),
                context.getString(R.string.recovery_warning_description),
                actions,
                callbacks
            )
        ).create()
        alertDialog?.show()
    }

    private fun onNavigationPressed() {
        router.popCurrentController()
    }

    private fun onDonePressed() {
        viewModel.onDone()
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