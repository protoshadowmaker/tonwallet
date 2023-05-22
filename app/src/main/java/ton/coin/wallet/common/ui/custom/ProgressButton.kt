package ton.coin.wallet.common.ui.custom

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.appcompat.widget.AppCompatButton
import androidx.core.animation.addListener
import androidx.core.animation.doOnCancel
import ton.coin.wallet.common.ui.dp

class ProgressButton(context: Context) : AppCompatButton(context) {

    private val paint = Paint()
    private val radius = 8.dp()
    private val drawPadding = 16.dp()
    private val valuePadding = 8.dp()
    private val totalProgressPadding = radius * 2 + drawPadding + valuePadding

    private var bounds: RectF = RectF()
    private var angle: Float = 0f
    private var angleSpeed: Float = 3f
    private var sweep: Float = 0f
    private var sweepSpeed: Float = 5f
    private var sweepDirection: Float = 1f
    private var drawScale: Float = 0f
    private var animationEnabled: Boolean = false
    private var internalChangePadding: Boolean = true
    private val originalPadding: Rect = Rect()
    private var transitionAnimator: AnimatorSet? = null
    private var attached: Boolean = false

    init {
        paint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 2.dp().toFloat()
        }
        updateOriginalPadding()
    }

    private fun updateOriginalPadding() {
        originalPadding.set(paddingLeft, paddingTop, paddingBottom, paddingRight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = (w - drawPadding - radius).toFloat()
        val cy = h / 2f
        bounds.set(cx - radius, cy - radius, cx + radius, cy + radius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!animationEnabled && transitionAnimator?.isRunning != true) {
            return
        }
        canvas.apply {
            save()
            scale(drawScale, drawScale, bounds.right, bounds.centerY())
            canvas.drawArc(bounds, angle, sweep, false, paint)
            restore()
        }
        if (sweep > 360 || sweep < 0) {
            sweepDirection = -sweepDirection
        }
        sweep += sweepSpeed * sweepDirection
        angle += angleSpeed
        if (sweepDirection < 0) {
            angle += sweepSpeed
        }
        angle %= 360f
        invalidate()
    }

    private fun setPaddingInternal(left: Int, top: Int, right: Int, bottom: Int) {
        internalChangePadding = true
        setPadding(left, top, right, bottom)
        internalChangePadding = false
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        if (!internalChangePadding) {
            updateOriginalPadding()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attached = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        attached = false
        transitionAnimator?.cancel()
    }

    fun setAnimationEnabled(enabled: Boolean, animated: Boolean) {
        if (enabled == animationEnabled) {
            return
        }
        animationEnabled = enabled
        val startLeft = paddingLeft
        val startRight = paddingRight
        val targetLeft: Int
        val targetRight: Int
        val targetScale: Float
        if (enabled) {
            targetLeft = totalProgressPadding
            targetRight = totalProgressPadding
            targetScale = 1f
        } else {
            targetLeft = originalPadding.left
            targetRight = originalPadding.right
            targetScale = 0f
        }
        transitionAnimator?.cancel()
        if (!animated) {
            setPaddingInternal(
                targetLeft,
                originalPadding.top,
                targetRight,
                originalPadding.bottom
            )
            drawScale = targetScale
        } else {
            AnimatorSet().apply {
                playTogether(
                    ValueAnimator.ofInt(startLeft, targetLeft).apply {
                        addUpdateListener {
                            val left = it.animatedValue as Int
                            setPaddingInternal(left, paddingTop, paddingRight, paddingBottom)
                        }
                    },
                    ValueAnimator.ofInt(startRight, targetRight).apply {
                        addUpdateListener {
                            val right = it.animatedValue as Int
                            setPaddingInternal(paddingLeft, paddingTop, right, paddingBottom)
                        }
                    },
                    ValueAnimator.ofFloat(drawScale, targetScale).apply {
                        addUpdateListener {
                            drawScale = it.animatedValue as Float
                        }
                    },
                )
                duration = 200
                addListener {
                    doOnCancel {
                        if (!attached) {
                            setPaddingInternal(
                                targetLeft,
                                originalPadding.top,
                                targetRight,
                                originalPadding.bottom
                            )
                            drawScale = targetScale
                        }
                    }
                }
                transitionAnimator = this
            }.start()
        }
    }
}