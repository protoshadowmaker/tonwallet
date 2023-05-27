package ton.coin.wallet.common.ui.custom

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.iconKeyboardButton
import ton.coin.wallet.common.ui.numericKeyboardButton

class NumericKeyboard(context: Context) : LinearLayout(context) {

    var keyPressListener: ((key: String) -> Unit)? = null
    var decimalEnabled: Boolean = true
        set(value) {
            decimalButton?.visibility = if (value) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            field = value
        }
    private var decimalButton: Button? = null
    private val textButtons = mutableListOf<Button>()
    private val iconButtons = mutableListOf<ImageButton>()

    init {
        val margin = 3.dp()
        orientation = VERTICAL
        addView(LinearLayout(context).apply {
            orientation = HORIZONTAL
            weightSum = 3f
            addButton(
                "1",
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
            addButton(
                "2",
                marginLeft = margin,
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
            addButton(
                "3",
                marginLeft = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
        })
        addView(LinearLayout(context).apply {
            orientation = HORIZONTAL
            weightSum = 3f
            addButton(
                "4",
                marginTop = margin,
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
            addButton(
                "5",
                marginLeft = margin,
                marginTop = margin,
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
            addButton(
                "6",
                marginLeft = margin,
                marginTop = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
        })
        addView(LinearLayout(context).apply {
            orientation = HORIZONTAL
            weightSum = 3f
            addButton(
                "7",
                marginTop = margin,
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
            addButton(
                "8",
                marginLeft = margin,
                marginTop = margin,
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
            addButton(
                "9",
                marginLeft = margin,
                marginTop = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
        })
        addView(LinearLayout(context).apply {
            orientation = HORIZONTAL
            weightSum = 3f
            decimalButton = addButton(
                ".",
                marginTop = margin,
                marginRight = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
            addButton(
                "0",
                marginLeft = margin,
                marginTop = margin,
                marginRight = margin
            ) { key -> keyPressListener?.invoke(key) }.apply {
                textButtons.add(this)
            }
            addIconButton(
                "e",
                R.drawable.ic_delete,
                marginLeft = margin,
                marginTop = margin,
            ) { key -> keyPressListener?.invoke(key) }.apply {
                iconButtons.add(this)
            }
        })
    }

    fun setStyle(@DrawableRes backgroundDrawableRes: Int, @ColorInt contentColor: Int) {
        textButtons.forEach {
            it.apply {
                setBackgroundResource(backgroundDrawableRes)
                setTextColor(contentColor)
            }
        }
        iconButtons.forEach {
            it.apply {
                setBackgroundResource(backgroundDrawableRes)
                setImageDrawable(drawable.mutate().apply { setTint(contentColor) })
            }
        }
    }
}

private fun LinearLayout.addButton(
    key: String,
    marginLeft: Int = 0,
    marginTop: Int = 0,
    marginRight: Int = 0,
    marginBottom: Int = 0,
    pressListener: ((key: String) -> Unit)
): Button {
    val button = numericKeyboardButton(context).apply {
        text = key
        setOnClickListener {
            pressListener(key)
        }
    }
    addView(button, LinearLayoutLpBuilder().wMatch().hDp(48).build().apply {
        weight = 1f
        setMargins(marginLeft, marginTop, marginRight, marginBottom)
    })
    return button
}

private fun LinearLayout.addIconButton(
    key: String,
    @DrawableRes icon: Int,
    marginLeft: Int = 0,
    marginTop: Int = 0,
    marginRight: Int = 0,
    marginBottom: Int = 0,
    pressListener: ((key: String) -> Unit)
): ImageButton {
    val button = iconKeyboardButton(context).apply {
        setImageResource(icon)
        setOnClickListener {
            pressListener(key)
        }
    }
    addView(button, LinearLayoutLpBuilder().wMatch().hDp(48).build().apply {
        weight = 1f
        setMargins(marginLeft, marginTop, marginRight, marginBottom)
    })
    return button
}