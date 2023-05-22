package ton.coin.wallet.common.ui.span

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.text.inSpans


inline fun SpannableStringBuilder.drawable(
    context: Context,
    @DrawableRes drawableRes: Int,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder {
    val drawable = ContextCompat.getDrawable(context, drawableRes)?.apply {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    }
    return if (drawable == null) {
        this
    } else {
        inSpans(ImageSpan(drawable, ImageSpan.ALIGN_CENTER), builderAction = builderAction)
    }
}