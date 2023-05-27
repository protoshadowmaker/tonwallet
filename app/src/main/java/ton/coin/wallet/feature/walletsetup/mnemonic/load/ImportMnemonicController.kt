package ton.coin.wallet.feature.walletsetup.mnemonic.load

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieAnimationView
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.alignCenter
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.coloredProgressButton
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.custom.ProgressButton
import ton.coin.wallet.common.ui.dialog
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.textButton
import ton.coin.wallet.common.ui.titleText
import ton.coin.wallet.feature.secure.start.SecureStartController
import ton.coin.wallet.feature.walletsetup.StepTooBad
import ton.coin.wallet.feature.walletsetup.mnemonic.MnemonicInputController

class ImportMnemonicController : MnemonicInputController<ImportMnemonicViewModel>() {

    private var continueButton: ProgressButton? = null
    private var alertDialog: AlertDialog? = null

    override fun createViewModel(): ImportMnemonicViewModel {
        return ImportMnemonicViewModel()
    }

    override fun createContent(context: Context): View {
        return LinearLayout(context).apply {
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
                setText(R.string.import_title)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 12.dp(), 0, 12.dp())
            })
            addView(bodyText(context).apply {
                setText(R.string.import_description)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build())
            addView(textButton(context).apply {
                setText(R.string.import_dont_have)
                setOnClickListener { doNotHave() }
            }, LinearLayoutLpBuilder().wWrap().hWrap().build())

            (0 until 24).map {
                val editText = createNumericInput(context).apply {
                    number = it + 1
                }
                numericInputs.add(editText)
                addView(editText, LinearLayoutLpBuilder().wDp(200).hWrap().build())
            }
            addView(
                coloredProgressButton(context).apply {
                    continueButton = this
                    setText(R.string.import_continue)
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
            viewModel.stateFlow.collect {
                onStateChanged(it)
            }
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        alertDialog?.dismiss()
    }

    private fun onStateChanged(state: ScreenState) {
        continueButton?.setAnimationEnabled(state.loading, true)
        when (state.oneTimeAction) {
            OneTimeAction.WARNING_WORDS -> {
                showWarningDialog()
            }

            OneTimeAction.PERFECT -> {
                router.pushController(
                    RouterTransaction.with(SecureStartController("import"))
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
        actions.add(context.getString(R.string.import_warning_ok))
        callbacks.add { alertDialog?.dismiss() }
        alertDialog = AlertDialog.Builder(context, R.style.TonAlertDialog).setView(
            dialog(
                context,
                context.getString(R.string.import_warning_title),
                context.getString(R.string.import_warning_description),
                actions,
                callbacks
            )
        ).create()
        alertDialog?.show()
    }

    private fun onContinuePressed() {
        viewModel.importAccount(numericInputs.map { it.text.trim() })
    }

    private fun doNotHave() {
        router.pushController(
            RouterTransaction.with(StepTooBad())
                .pushChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                .popChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
        )
    }
}