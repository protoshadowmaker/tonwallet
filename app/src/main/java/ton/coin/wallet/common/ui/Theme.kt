package ton.coin.wallet.common.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.util.StateSet
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import ton.coin.wallet.R


data class Theme(val lightColors: Colors, val darkColors: Colors) {
    companion object {
        val DEFAULT = Theme(
            lightColors = Colors(
                background = 0xFFFFFFFF.toInt(),
                coloredButtonBackground = 0xFF339CEC.toInt(),
                coloredButtonText = 0xFFFFFFFF.toInt(),
                keypadButtonText = 0xFF000000.toInt(),
                textButtonBackground = 0x00FFFFFF,
                textButtonText = 0xFF339CEC.toInt(),
                textButtonDangerText = 0xFFFE3C30.toInt(),
                titleText = 0xFF222222.toInt(),
                bodyText = 0xFF000000.toInt(),
                bodyTextLight = 0xFF757575.toInt(),
                hintText = 0x80000000.toInt(),
                dialogTitleText = 0xFF000000.toInt(),
                numericItemText = 0XFF757575.toInt(),
                walletDarkText = 0xFFFFFFFF.toInt(),
                icon = 0xFF000000.toInt(),
                textCursorActive = 0xFF339CEC.toInt(),
                textCursorInactive = 0xFFDBDBDB.toInt(),
                checkBoxChecked = 0xFF339CEC.toInt(),
                toolbarText = 0xFF000000.toInt(),
                settingsTitleText = 0xFF339CEC.toInt(),
                divider = 0xFFDBDBDB.toInt(),
                transactionAmountIn = 0xFF37A818.toInt(),
                transactionAmountOut = 0xFFFE3C30.toInt(),
            ),
            darkColors = Colors(
                background = 0xFF000000.toInt(),
                coloredButtonBackground = 0xFF339CEC.toInt(),
                coloredButtonText = 0xFFFFFFFF.toInt(),
                keypadButtonText = 0xFF000000.toInt(),
                textButtonBackground = 0x00FFFFFF,
                textButtonText = 0xFF339CEC.toInt(),
                textButtonDangerText = 0xFFFE3C30.toInt(),
                titleText = 0xFF222222.toInt(),
                bodyText = 0xFFFFFFFF.toInt(),
                bodyTextLight = 0xFF757575.toInt(),
                hintText = 0x80000000.toInt(),
                dialogTitleText = 0xFF000000.toInt(),
                numericItemText = 0XFF757575.toInt(),
                walletDarkText = 0xFF000000.toInt(),
                icon = 0xFF000000.toInt(),
                textCursorActive = 0xFF339CEC.toInt(),
                textCursorInactive = 0xFFDBDBDB.toInt(),
                checkBoxChecked = 0xFF339CEC.toInt(),
                toolbarText = 0xFFFFFFFF.toInt(),
                settingsTitleText = 0xFF339CEC.toInt(),
                divider = 0xFFDBDBDB.toInt(),
                transactionAmountIn = 0xFF37A818.toInt(),
                transactionAmountOut = 0xFFFE3C30.toInt(),
            )
        )

        fun createEditTextDrawable(context: Context, colors: Colors): Drawable {
            val defaultDrawable =
                ContextCompat.getDrawable(context, R.drawable.search_dark)!!.mutate()
            defaultDrawable.colorFilter = PorterDuffColorFilter(
                colors.textCursorInactive,
                PorterDuff.Mode.MULTIPLY
            )
            val pressedDrawable =
                ContextCompat.getDrawable(context, R.drawable.search_dark_activated)!!.mutate()
            pressedDrawable.colorFilter = PorterDuffColorFilter(
                colors.textCursorActive,
                PorterDuff.Mode.MULTIPLY
            )
            return StateListDrawable().apply {
                addState(
                    intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused),
                    pressedDrawable
                )
                addState(intArrayOf(android.R.attr.state_focused), pressedDrawable)
                addState(StateSet.WILD_CARD, defaultDrawable)
            }
        }
    }
}

data class Colors(
    @ColorInt val background: Int,
    @ColorInt val coloredButtonBackground: Int,
    @ColorInt val coloredButtonText: Int,
    @ColorInt val keypadButtonText: Int,
    @ColorInt val textButtonBackground: Int,
    @ColorInt val textButtonText: Int,
    @ColorInt val textButtonDangerText: Int,
    @ColorInt val titleText: Int,
    @ColorInt val bodyText: Int,
    @ColorInt val bodyTextLight: Int,
    @ColorInt val hintText: Int,
    @ColorInt val dialogTitleText: Int,
    @ColorInt val numericItemText: Int,
    @ColorInt val walletDarkText: Int,
    @ColorInt val icon: Int,
    @ColorInt val textCursorActive: Int,
    @ColorInt val textCursorInactive: Int,
    @ColorInt val checkBoxChecked: Int,
    @ColorInt val toolbarText: Int,
    @ColorInt val settingsTitleText: Int,
    @ColorInt val divider: Int,
    @ColorInt val transactionAmountIn: Int,
    @ColorInt val transactionAmountOut: Int,
)