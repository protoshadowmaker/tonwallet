package ton.coin.wallet.common.ui.custom

import android.content.Context
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.iconKeyboardButton
import ton.coin.wallet.common.ui.numericKeyboardButton

class NumericKeyboard(context: Context) : LinearLayout(context) {

    var keyPressListener: ((key: String) -> Unit)? = null
    var decimalEnabled: Boolean = true
        set(value) {
            decimalButton?.isVisible = value
            field = value
        }
    private var decimalButton: Button? = null

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
            ) { key -> keyPressListener?.invoke(key) }
            addButton(
                "2",
                marginLeft = margin,
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }
            addButton(
                "3",
                marginLeft = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }
        })
        addView(LinearLayout(context).apply {
            orientation = HORIZONTAL
            weightSum = 3f
            addButton(
                "4",
                marginTop = margin,
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }
            addButton(
                "5",
                marginLeft = margin,
                marginTop = margin,
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }
            addButton(
                "6",
                marginLeft = margin,
                marginTop = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }
        })
        addView(LinearLayout(context).apply {
            orientation = HORIZONTAL
            weightSum = 3f
            addButton(
                "7",
                marginTop = margin,
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }
            addButton(
                "8",
                marginLeft = margin,
                marginTop = margin,
                marginRight = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }
            addButton(
                "9",
                marginLeft = margin,
                marginTop = margin,
                marginBottom = margin
            ) { key -> keyPressListener?.invoke(key) }
        })
        addView(LinearLayout(context).apply {
            orientation = HORIZONTAL
            weightSum = 3f
            addButton(
                ".",
                marginTop = margin,
                marginRight = margin
            ) { key -> keyPressListener?.invoke(key) }
            addButton(
                "0",
                marginLeft = margin,
                marginTop = margin,
                marginRight = margin
            ) { key -> keyPressListener?.invoke(key) }
            addIconButton(
                "e",
                R.drawable.ic_delete,
                marginLeft = margin,
                marginTop = margin,
            ) { key -> keyPressListener?.invoke(key) }
        })
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