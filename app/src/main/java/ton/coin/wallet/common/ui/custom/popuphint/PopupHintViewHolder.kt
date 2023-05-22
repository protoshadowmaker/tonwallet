package ton.coin.wallet.common.ui.custom.popuphint

import android.view.View
import android.widget.TextView
import io.github.landarskiy.reuse.ReuseViewHolder

class PopupHintViewHolder(view: View) : ReuseViewHolder<PopupHintEntry>(view) {

    private val textView: TextView = view as TextView

    override fun bind(data: PopupHintEntry) {
        textView.text = data.text
        textView.setOnClickListener {
            data.clickListener(data.text)
        }
    }
}