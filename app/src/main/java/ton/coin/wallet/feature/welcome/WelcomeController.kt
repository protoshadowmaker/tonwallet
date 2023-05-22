package ton.coin.wallet.feature.welcome

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.alignCenter
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.coloredProgressButton
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.custom.ProgressButton
import ton.coin.wallet.common.ui.darkStatusBar
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.lightStatusBar
import ton.coin.wallet.common.ui.textButton
import ton.coin.wallet.common.ui.titleText
import ton.coin.wallet.feature.walletsetup.StepCongratulationsController
import ton.coin.wallet.feature.walletsetup.mnemonic.load.ImportMnemonicController

class WelcomeController : ViewModelController() {

    private val viewModel: WelcomeViewModel by lazy {
        WelcomeViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: FrameLayout? = null
    private var createButton: ProgressButton? = null
    private var importButton: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?
    ): View {
        activity?.lightStatusBar()
        val context = inflater.context
        val contentContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(
                LottieAnimationView(context).apply {
                    setAnimation(R.raw.start)
                    repeatCount = LottieDrawable.INFINITE
                    playAnimation()
                }, LinearLayoutLpBuilder().wDp(100).hDp(100).build()
            )
            addView(titleText(context).apply {
                setText(R.string.app_name)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 12.dp(), 0, 12.dp())
            })
            addView(bodyText(context).apply {
                setText(R.string.welcome_description)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 0, 0, 106.dp())
            })

            addView(
                coloredProgressButton(context).apply {
                    setText(R.string.welcome_wallet_new)
                    setOnClickListener { onCreatePressed() }
                    createButton = this
                },
                LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                    setMargins(0, 0, 0, 8.dp())
                }
            )
            addView(
                textButton(context).apply {
                    setText(R.string.welcome_wallet_import)
                    setOnClickListener { onImportPressed() }
                    importButton = this
                },
                LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                    setMargins(0, 0, 0, 44.dp())
                }
            )
        }
        val root = FrameLayout(inflater.context).apply {
            setPadding(40.dp(), 0, 40.dp(), 0)
            setBackgroundResource(R.color.white)
            addView(
                contentContainer,
                FrameLayoutLpBuilder().wMatch().hWrap().build().apply {
                    gravity = Gravity.BOTTOM
                }
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

    private fun onStateChanged(state: ScreenState) {
        when (state) {
            is ScreenState.Empty -> {
                setButtonsEnabled(true)
                createButton?.setAnimationEnabled(enabled = false, animated = true)
            }

            is ScreenState.Import -> {
                setButtonsEnabled(false)
                createButton?.setAnimationEnabled(enabled = false, animated = true)
                viewModel.resetState()
                router.pushController(
                    RouterTransaction.with(ImportMnemonicController())
                        .pushChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                        .popChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                )
            }

            is ScreenState.Loading -> {
                setButtonsEnabled(false)
                createButton?.setAnimationEnabled(enabled = true, animated = true)
            }

            is ScreenState.New -> {
                createButton?.setAnimationEnabled(enabled = false, animated = true)
                viewModel.resetState()
                router.pushController(
                    RouterTransaction.with(StepCongratulationsController())
                        .pushChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                        .popChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                )
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        createButton?.isEnabled = enabled
        importButton?.isEnabled = enabled
    }

    private fun onCreatePressed() {
        viewModel.createWallet()
    }

    private fun onImportPressed() {
        viewModel.importWallet()
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