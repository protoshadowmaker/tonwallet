package ton.coin.wallet.feature.walletsetup.mnemonic.test

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieAnimationView
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.alignCenter
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.coloredButton
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.dialog
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.titleText
import ton.coin.wallet.feature.secure.start.SecureStartController
import ton.coin.wallet.feature.walletsetup.mnemonic.MnemonicInputController

class StepTestPhraseController : MnemonicInputController<StepTestPhraseViewModel>() {

    private var descriptionTextView: TextView? = null
    private var alertDialog: AlertDialog? = null

    override fun createViewModel(): StepTestPhraseViewModel {
        return StepTestPhraseViewModel()
    }

    override fun createContent(context: Context): View {
        return LinearLayout(context).apply {
            setPadding(40.dp(), 0, 40.dp(), 0)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(
                LottieAnimationView(context).apply {
                    setAnimation(R.raw.test_time)
                    playAnimation()
                }, LinearLayoutLpBuilder().wDp(100).hDp(100).build()
            )
            addView(titleText(context).apply {
                setText(R.string.test_title)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 12.dp(), 0, 12.dp())
            })
            addView(bodyText(context).apply {
                setText(R.string.test_description)
                alignCenter()
                descriptionTextView = this
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 0, 0, 40.dp())
            })

            (0 until 3).map {
                val editText = createNumericInput(context).apply {
                    number = 1
                }
                numericInputs.add(editText)
                addView(editText, LinearLayoutLpBuilder().wDp(200).hWrap().build())
            }

            addView(
                coloredButton(context).apply {
                    setText(R.string.test_continue)
                    setOnClickListener { onContinuePressed() }
                },
                LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                    setMargins(0, 44.dp(), 0, 56.dp())
                }
            )
        }
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
        val context = router.activity ?: return
        if (state.checkIndexes.size == 3) {
            val args: List<Int> = state.checkIndexes
            descriptionTextView?.text =
                context.getString(R.string.test_description, args[0] + 1, args[1] + 1, args[2] + 1)
            numericInputs.forEachIndexed { index, numericInput ->
                numericInput.number = args[index] + 1
            }
        }
        when (state.oneTimeAction) {
            OneTimeAction.WARNING_WORDS -> {
                showWarningDialog()
            }

            OneTimeAction.PERFECT -> {
                router.pushController(
                    RouterTransaction.with(SecureStartController("create"))
                        .pushChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                        .popChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                )
            }

            null -> {}
        }
        viewModel.consumeOneTimeAction()
    }

    private fun showWarningDialog() {
        val context = activity ?: return
        alertDialog?.dismiss()
        val actions = mutableListOf<String>()
        val callbacks = mutableListOf<() -> Unit>()
        actions.add(context.getString(R.string.test_warning_see_wards))
        callbacks.add { router.popCurrentController() }
        actions.add(context.getString(R.string.test_warning_try_again))
        callbacks.add { alertDialog?.dismiss() }
        alertDialog = AlertDialog.Builder(context, R.style.TonAlertDialog).setView(
            dialog(
                context,
                context.getString(R.string.test_warning_title),
                context.getString(R.string.test_warning_description),
                actions,
                callbacks
            )
        ).create()
        alertDialog?.show()
    }

    private fun onContinuePressed() {
        viewModel.onContinue(numericInputs.map { it.text.trim() })
    }
}