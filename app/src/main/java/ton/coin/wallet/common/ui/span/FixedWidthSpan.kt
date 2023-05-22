package ton.coin.wallet.common.ui.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.style.ReplacementSpan
import androidx.core.text.inSpans
import kotlin.math.roundToInt

class FixedWidthSpan : ReplacementSpan() {

    private var actualSize = 0f
    private var fixSize = 0f

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        fixSize = paint.measureText("000")
        actualSize = paint.measureText(text, start, end)
        return fixSize.roundToInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        canvas.drawText(text, start, end, x + fixSize - actualSize, y.toFloat(), paint)
    }
}

inline fun SpannableStringBuilder.fixedWidth(
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = inSpans(FixedWidthSpan(), builderAction = builderAction)