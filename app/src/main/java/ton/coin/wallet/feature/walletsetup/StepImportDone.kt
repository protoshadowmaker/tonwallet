package ton.coin.wallet.feature.walletsetup

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bluelinelabs.conductor.RouterTransaction
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.alignCenter
import ton.coin.wallet.common.ui.coloredButton
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.titleText
import ton.coin.wallet.feature.home.HomeController

class StepImportDone : ViewModelController() {

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
                    setAnimation(R.raw.congratulations)
                    repeatCount = LottieDrawable.INFINITE
                    playAnimation()
                }, LinearLayoutLpBuilder().wDp(100).hDp(100).build()
            )
            addView(titleText(context).apply {
                setText(R.string.import_done_title)
                alignCenter()
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 12.dp(), 0, 128.dp())
            })

            addView(
                coloredButton(context).apply {
                    setText(R.string.import_done_proceed)
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
            addView(
                contentContainer,
                LinearLayoutLpBuilder().wMatch().hMatch().build()
            )
        }
        this.root = root
        return root
    }

    private fun onProceedPressed() {
        router.setRoot(
            RouterTransaction.with(HomeController())
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