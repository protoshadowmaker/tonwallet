package ton.coin.wallet.common.ui

import android.app.Activity
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.WindowInsetsControllerCompat


fun TextView.setTextSizeDp(sizeDp: Number) {
    setTextSize(TypedValue.COMPLEX_UNIT_DIP, sizeDp.toFloat())
}

abstract class LayoutLpBuilder<T : ViewGroup.LayoutParams>(
    var width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    var height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
) {
    fun wMatch() = apply {
        width = ViewGroup.LayoutParams.MATCH_PARENT
    }

    fun wWrap() = apply {
        width = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    fun wPx(sizePx: Int) = apply {
        width = sizePx
    }

    fun wDp(sizeDp: Number) = apply {
        width = sizeDp.dp()
    }

    fun hMatch() = apply {
        height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    fun hWrap() = apply {
        height = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    fun hPx(sizePx: Int) = apply {
        height = sizePx
    }

    fun hDp(sizeDp: Number) = apply {
        height = sizeDp.dp()
    }

    abstract fun build(): T
}

class FrameLayoutLpBuilder(
    width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
) : LayoutLpBuilder<FrameLayout.LayoutParams>(width = width, height = height) {
    override fun build(): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(width, height)
    }
}

class LinearLayoutLpBuilder(
    width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
) : LayoutLpBuilder<LinearLayout.LayoutParams>(width = width, height = height) {
    override fun build(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(width, height)
    }
}

fun Activity.lightStatusBar() {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
}

fun Activity.darkStatusBar() {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
}