package ton.coin.wallet.common.ui.custom

import android.content.Context
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.dp

class SecureInputView(context: Context) : LinearLayout(context) {

    private val modeViews = mutableListOf<AnimatedDotView>()
    var mode: Int = 4
        set(value) {
            if (field == value) {
                return
            }
            field = value
            val target = if (value == 4) {
                0f
            } else {
                1f
            }
            modeViews.first().animate()
                .alpha(target)
                .setDuration(200)
                .withEndAction {
                    modeViews.first().alpha = target
                }.start()
            modeViews.last().animate()
                .alpha(target)
                .setDuration(200)
                .withEndAction {
                    modeViews.last().alpha = target
                }.start()
        }

    var checkedCount: Int = 0
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (mode == 4) {
                modeViews.forEachIndexed { index, animatedDotView ->
                    animatedDotView.setChecked(index <= value, true)
                }
            } else {
                modeViews.forEachIndexed { index, animatedDotView ->
                    animatedDotView.setChecked(index < value, true)
                }
            }
        }

    init {
        orientation = HORIZONTAL
        addView(AnimatedDotView(context).apply {
            modeViews.add(this)
            alpha = 0f
        }, LinearLayoutLpBuilder().wDp(16).hDp(16).build().apply {
            setMargins(8.dp(), 0, 8.dp(), 0)
        })
        addView(AnimatedDotView(context).apply {
            modeViews.add(this)
        }, LinearLayoutLpBuilder().wDp(16).hDp(16).build().apply {
            setMargins(8.dp(), 0, 8.dp(), 0)
        })
        addView(AnimatedDotView(context).apply {
            modeViews.add(this)
        }, LinearLayoutLpBuilder().wDp(16).hDp(16).build().apply {
            setMargins(8.dp(), 0, 8.dp(), 0)
        })
        addView(AnimatedDotView(context).apply {
            modeViews.add(this)
        }, LinearLayoutLpBuilder().wDp(16).hDp(16).build().apply {
            setMargins(8.dp(), 0, 8.dp(), 0)
        })
        addView(AnimatedDotView(context).apply {
            modeViews.add(this)
        }, LinearLayoutLpBuilder().wDp(16).hDp(16).build().apply {
            setMargins(8.dp(), 0, 8.dp(), 0)
        })
        addView(AnimatedDotView(context).apply {
            modeViews.add(this)
            alpha = 0f
        }, LinearLayoutLpBuilder().wDp(16).hDp(16).build().apply {
            setMargins(8.dp(), 0, 8.dp(), 0)
        })
    }

    fun setColorScheme(@ColorInt borderColor: Int, @ColorInt solidColor: Int) {
        modeViews.forEach { it.setColorScheme(borderColor = borderColor, solidColor = solidColor) }
    }
}