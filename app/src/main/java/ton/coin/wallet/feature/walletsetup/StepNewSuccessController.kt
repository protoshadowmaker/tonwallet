package ton.coin.wallet.feature.walletsetup

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bluelinelabs.conductor.RouterTransaction
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.alignCenter
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.coloredButton
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.titleText
import ton.coin.wallet.common.ui.lightToolbar
import ton.coin.wallet.feature.walletsetup.recovery.StepRecoveryPhraseController

class StepNewSuccessController : ViewModelController() {

    private var root: LinearLayout? = null

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
                    setAnimation(R.raw.success)
                    repeatCount = LottieDrawable.INFINITE
                    playAnimation()
                }, LinearLayoutLpBuilder().wDp(100).hDp(100).build()
            )
            addView(titleText(context).apply {
                setText(R.string.new_wallet_success_title)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 12.dp(), 0, 12.dp())
            })
            addView(bodyText(context).apply {
                setText(R.string.new_wallet_success_description)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 0, 0, 116.dp())
            })

            addView(
                coloredButton(context).apply {
                    setText(R.string.new_wallet_success_passcode)
                    setOnClickListener { onProceedPressed() }
                },
                LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                    setMargins(0, 0, 0, 100.dp())
                }
            )
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

    private fun onNavigationPressed() {
        router.popCurrentController()
    }

    private fun onProceedPressed() {
        router.pushController(
            RouterTransaction.with(StepRecoveryPhraseController())
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