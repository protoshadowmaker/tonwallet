package ton.coin.wallet.common.ui.custom

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.Theme
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.dp

class KeyValueButton(context: Context) : FrameLayout(context) {

    val titleTextView: TextView
    val valueTextView: TextView

    init {
        setBackgroundResource(R.drawable.button_text)
        minimumHeight = 48.dp()
        setPadding(20.dp(), 8.dp(), 20.dp(), 8.dp())
        addView(bodyText(context).apply {
            titleTextView = this
        }, FrameLayoutLpBuilder().build().apply {
            gravity = Gravity.CENTER_VERTICAL
        })
        addView(bodyText(context).apply {
            valueTextView = this
            setTextColor(Theme.DEFAULT.lightColors.textButtonText)
        }, FrameLayoutLpBuilder().build().apply {
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
        })
    }
}