package ton.coin.wallet.feature.home.adapter

import android.view.View
import android.widget.TextView
import io.github.landarskiy.reuse.ReuseViewHolder
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionDateViewHolder(view: View) : ReuseViewHolder<TransactionDateEntry>(view) {

    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.US)
    private val text: TextView = view as TextView

    override fun bind(data: TransactionDateEntry) {
        text.text = dateFormatter.format(data.data)
    }
}