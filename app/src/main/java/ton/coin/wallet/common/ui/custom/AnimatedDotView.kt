package ton.coin.wallet.common.ui.custom

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import androidx.annotation.ColorInt
import ton.coin.wallet.common.ui.dp

class AnimatedDotView(context: Context) : View(context) {
    private var attached: Boolean = false
    private var checked: Boolean = false
    private var scale: Float = 0f
    private val borderPaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 1.dp().toFloat()
        color = Color.parseColor("#DBDBDB")
    }
    private val fillPaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val ovalRect: RectF = RectF()
    private var changeAnimator: Animator? = null

    fun setColorScheme(@ColorInt borderColor: Int, @ColorInt solidColor: Int) {
        borderPaint.color = borderColor
        fillPaint.color = solidColor
    }

    fun setChecked(checked: Boolean, animated: Boolean = true) {
        if (checked == this.checked) {
            return
        }
        changeAnimator?.cancel()
        this.checked = checked
        if (!attached) {
            return
        }
        val target = if (checked) {
            1f
        } else {
            0f
        }
        if (animated) {
            changeAnimator = ValueAnimator.ofFloat(scale, target).apply {
                duration = 100
                addUpdateListener {
                    scale = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            scale = target
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = 1.dp().toFloat()
        ovalRect.set(padding, padding, w.toFloat() - padding, h.toFloat() - padding)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawOval(ovalRect, borderPaint)
        canvas.apply {
            save()
            scale(scale, scale, ovalRect.centerX(), ovalRect.centerY())
            canvas.drawOval(ovalRect, fillPaint)
            restore()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attached = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        changeAnimator?.cancel()
        attached = false
        scale = if (checked) {
            1f
        } else {
            0f
        }
    }
}