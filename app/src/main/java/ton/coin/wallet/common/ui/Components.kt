package ton.coin.wallet.common.ui

import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.text.bold
import androidx.core.text.color
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.custom.ProgressButton
import ton.coin.wallet.common.ui.span.fixedWidth

fun dialogTitleText(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(19)
        setTextColor(lightColors.dialogTitleText)
        typeface = context.resources.getFont(R.font.roboto_medium)
    }
}

fun titleText(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(24)
        setTextColor(lightColors.titleText)
        typeface = context.resources.getFont(R.font.roboto_medium)
    }
}

fun bodyText(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(15)
        setTextColor(lightColors.bodyText)
        typeface = context.resources.getFont(R.font.roboto_regular)
    }
}

fun bodyTextDark(context: Context): TextView {
    return bodyText(context).apply {
        setTextColor(darkColors.bodyText)
    }
}


fun hintText(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(15)
        setTextColor(lightColors.hintText)
        typeface = context.resources.getFont(R.font.roboto_regular)
    }
}

fun bodyTextLight(context: Context): TextView {
    return bodyText(context).apply {
        setTextColor(lightColors.bodyTextLight)
    }
}

fun walletText(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(15)
        typeface = context.resources.getFont(R.font.roboto_regular)
    }
}

fun walletTextDark(context: Context): TextView {
    return walletText(context).apply {
        setTextColor(lightColors.walletDarkText)
    }
}

fun walletTextLight(context: Context): TextView {
    return walletText(context).apply {
        setTextColor(darkColors.walletDarkText)
    }
}

private fun walletTextMono(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(15)
        typeface = context.resources.getFont(R.font.roboto_mono_regular)
    }
}

fun walletTextMonoDark(context: Context): TextView {
    return walletTextMono(context).apply {
        setTextColor(lightColors.walletDarkText)
    }
}

fun walletTextMonoLight(context: Context): TextView {
    return walletTextMono(context).apply {
        setTextColor(darkColors.walletDarkText)
    }
}

fun walletBalanceText(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(44)
        setTextColor(lightColors.walletDarkText)
        typeface = context.resources.getFont(R.font.roboto_medium)
    }
}

fun TextView.alignCenter(): TextView = apply {
    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
}

fun coloredProgressButton(context: Context): ProgressButton {
    return ProgressButton(context).apply {
        applyBaseStyle()
        setBackgroundResource(R.drawable.button_flat)
        setTextColor(lightColors.coloredButtonText)
    }
}

fun coloredButton(context: Context): Button {
    return Button(context).apply {
        applyBaseStyle()
        setBackgroundResource(R.drawable.button_flat)
        setTextColor(lightColors.coloredButtonText)
    }
}

fun numericKeyboardButton(context: Context): Button {
    return Button(context).apply {
        stateListAnimator = null
        isAllCaps = false
        setTextSizeDp(24)
        typeface = context.resources.getFont(R.font.roboto_regular)
        minWidth = 0.dp()
        minHeight = 48.dp()
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.button_keyboard_digital_light)
        setTextColor(lightColors.keypadButtonText)
    }
}

fun iconKeyboardButton(context: Context): ImageButton {
    return ImageButton(context).apply {
        stateListAnimator = null
        minimumWidth = 0.dp()
        minimumHeight = 48.dp()
        setBackgroundResource(R.drawable.button_keyboard_digital_light)
    }
}

fun textButton(context: Context): Button {
    return Button(context).apply {
        applyBaseStyle()
        setBackgroundResource(R.drawable.button_text)
        setTextColor(lightColors.textButtonText)
    }
}

fun popupTextButton(context: Context): Button {
    return Button(context).apply {
        applyBaseStyle()
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        setPadding(20.dp(), 0, 20.dp(), 0)
        setBackgroundResource(R.drawable.button_text)
        setTextColor(Theme.DEFAULT.lightColors.bodyText)
    }
}

fun checkBox(context: Context): CheckBox {
    return AppCompatCheckBox(context).apply {
        setTextSizeDp(14)
        setTextColor(lightColors.bodyText)
        buttonTintList = ColorStateList.valueOf(lightColors.checkBoxChecked)
    }
}

fun switch(context: Context): SwitchCompat {
    return SwitchCompat(context)
}

fun lightToolbar(context: Context): Toolbar {
    return Toolbar(context).apply {
        setBackgroundColor(lightColors.background)
        setTitleTextColor(lightColors.toolbarText)
    }
}

fun darkToolbar(context: Context): Toolbar {
    return Toolbar(context).apply {
        setBackgroundColor(darkColors.background)
        setTitleTextColor(darkColors.toolbarText)
    }
}

fun settingsTitle(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(15)
        setTextColor(lightColors.settingsTitleText)
        typeface = context.resources.getFont(R.font.roboto_medium)
        minHeight = 40.dp()
        setPadding(20.dp(), 0, 20.dp(), 0)
        gravity = Gravity.START or Gravity.BOTTOM
    }
}

fun headlineText(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(15)
        setTextColor(lightColors.settingsTitleText)
        typeface = context.resources.getFont(R.font.roboto_medium)
    }
}

fun transactionDateText(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(15)
        minHeight = 52.dp()
        setPadding(16.dp(), 20.dp(), 16.dp(), 12.dp())
        setTextColor(lightColors.bodyText)
        typeface = context.resources.getFont(R.font.roboto_medium)
    }
}

fun settingsButton(context: Context): Button {
    return Button(context).apply {
        applyBaseStyle()
        setPadding(20.dp(), 0, 20.dp(), 0)
        setTextColor(lightColors.bodyText)
        typeface = context.resources.getFont(R.font.roboto_regular)
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        setBackgroundResource(R.drawable.button_text)
    }
}

fun settingsDangerousButton(context: Context): TextView {
    return settingsButton(context).apply {
        setTextColor(lightColors.textButtonDangerText)
    }
}

fun dialog(
    context: Context,
    title: String,
    description: String,
    actions: List<String>,
    actionListeners: List<() -> Unit>,
    actionColors: Map<String, Int> = emptyMap()
): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        addView(dialogTitleText(context).apply {
            text = title
        }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
            setMargins(24.dp(), 22.dp(), 24.dp(), 0)
        })
        addView(bodyText(context).apply {
            text = description
        }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
            setMargins(24.dp(), 12.dp(), 24.dp(), 0)
        })
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            actions.forEachIndexed { index, action ->
                addView(textButton(context).apply {
                    minWidth = 0.dp()
                    setPadding(16.dp(), 12.dp(), 16.dp(), 12.dp())
                    text = action
                    setOnClickListener { actionListeners[index]() }
                    val actionColor = actionColors[action]
                    if (actionColor != null) {
                        setTextColor(actionColor)
                    }
                }, LinearLayoutLpBuilder().wWrap().hWrap().build())
            }
        }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
            setMargins(8.dp(), 16.dp(), 8.dp(), 12.dp())
        })
    }
}

fun numericWordsText(context: Context): TextView {
    return TextView(context).apply {
        setTextSizeDp(15)
        setTextColor(lightColors.numericItemText)
        typeface = context.resources.getFont(R.font.roboto_regular)
        setLineSpacing(8.dp().toFloat(), 1f)
    }
}

fun input(context: Context): EditText {
    return EditText(context).apply {
        setTextSizeDp(15)
        setTextColor(lightColors.bodyText)
        setTextCursorDrawable(R.drawable.text_cursor_light)
        setHintTextColor(lightColors.hintText)
        background = Theme.createEditTextDrawable(context, lightColors)
    }
}

fun amountInput(context: Context): EditText {
    return EditText(context).apply {
        setTextSizeDp(44)
        setTextColor(lightColors.bodyText)
        setTextCursorDrawable(R.drawable.text_cursor_light)
        setHintTextColor(lightColors.hintText)
        typeface = context.resources.getFont(R.font.roboto_medium)
        background = null
    }
}

fun TextView.setNumericWords(words: List<String>, displayIndexOffset: Int = 0) {
    val sb = SpannableStringBuilder()
    val textColor = lightColors.bodyText
    words.forEachIndexed { index, word ->
        sb.append(if (index > 0) "\n" else "")
            .fixedWidth { append("${index + 1 + displayIndexOffset}.") }
            .append(" ")
            .bold { color(textColor) { append(word) } }
    }
    text = sb
}

fun nameValueTile(context: Context, name: View, value: View): FrameLayout {
    return FrameLayout(context).apply {
        minimumHeight = 48.dp()
        addView(name, FrameLayoutLpBuilder().build().apply {
            gravity = Gravity.CENTER_VERTICAL
        })
        addView(value, FrameLayoutLpBuilder().build().apply {
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
        })
    }
}

fun divider(context: Context): View {
    return View(context).apply {
        setBackgroundColor(lightColors.divider)
    }
}

private fun Button.applyBaseStyle() {
    stateListAnimator = null
    isAllCaps = false
    setTextSizeDp(15)
    typeface = context.resources.getFont(R.font.roboto_medium)
    minWidth = 200.dp()
    minHeight = 48.dp()
    gravity = Gravity.CENTER
}

private val lightColors: Colors get() = Theme.DEFAULT.lightColors
private val darkColors: Colors get() = Theme.DEFAULT.darkColors