package ton.coin.wallet.common.ui.conductor

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler

class HorizontalFadeChangeFromHandler : AnimatorChangeHandler {
    constructor() : super()
    constructor(removesFromViewOnPush: Boolean) : super(removesFromViewOnPush)
    constructor(duration: Long) : super(duration)
    constructor(duration: Long, removesFromViewOnPush: Boolean) : super(
        duration,
        removesFromViewOnPush
    )

    override fun getAnimator(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        toAddedToContainer: Boolean,
    ): Animator {
        val animatorSet = AnimatorSet()
        if (isPush) {
            if (to != null) {
                animatorSet.playTogether(
                    ObjectAnimator.ofFloat(to, View.TRANSLATION_X, to.width.toFloat(), 0f),
                    ObjectAnimator.ofFloat(to, View.ALPHA, 0f, 1f)
                )
            }
        } else {
            if (from != null) {
                animatorSet.playTogether(
                    ObjectAnimator.ofFloat(from, View.TRANSLATION_X, from.width.toFloat()),
                    ObjectAnimator.ofFloat(from, View.ALPHA, 1f, 0f)
                )
            }
        }
        return animatorSet
    }

    override fun resetFromView(from: View) {
        from.translationX = 0f
    }

    override fun copy(): ControllerChangeHandler =
        HorizontalFadeChangeFromHandler(animationDuration, removesFromViewOnPush)
}