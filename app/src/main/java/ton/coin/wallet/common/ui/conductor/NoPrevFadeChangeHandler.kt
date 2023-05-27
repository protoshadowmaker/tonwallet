package ton.coin.wallet.common.ui.conductor

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler

class NoPrevFadeChangeHandler : AnimatorChangeHandler {
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
        val animator = AnimatorSet()
        if (from != null && !isPush) {
            animator.play(ObjectAnimator.ofFloat(from, View.ALPHA, 1f, 0f))
        }
        if (from != null && isPush) {
            from.alpha = 0f
        }
        if (to != null && isPush) {
            animator.play(ObjectAnimator.ofFloat(to, View.ALPHA, 0f, 1f))
        }
        if (to != null && !isPush) {
            to.alpha = 1f
        }
        return animator
    }

    override fun resetFromView(from: View) {
        from.alpha = 1f
    }

    override fun copy(): ControllerChangeHandler =
        NoPrevFadeChangeHandler(animationDuration, removesFromViewOnPush)
}