package ton.coin.wallet.common.ui.span

import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import androidx.core.text.inSpans

inline fun SpannableStringBuilder.size(
    size: Int,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = inSpans(AbsoluteSizeSpan(size), builderAction = builderAction)