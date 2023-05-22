package ton.coin.wallet.feature.home.adapter

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.core.text.color
import io.github.landarskiy.reuse.ReuseViewHolder
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.Theme
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.formatter.TonCoinsFormatter
import ton.coin.wallet.common.ui.formatter.WalletAddressFormatter
import ton.coin.wallet.common.ui.span.drawable
import ton.coin.wallet.common.ui.span.size
import ton.coin.wallet.data.CompletedTransaction
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionViewHolder(
    view: View,
    private val content: TextView,
    private val comment: TextView,
    private val time: TextView
) : ReuseViewHolder<TransactionEntry>(view) {

    private val dateFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    override fun bind(data: TransactionEntry) {
        val colors = Theme.DEFAULT.lightColors
        time.text = dateFormatter.format(data.data.date)
        val sb = SpannableStringBuilder()
            .drawable(context, R.drawable.ic_diamond_small) { append("*") }
            .append(" ")
        val amount = TonCoinsFormatter.format(data.data.amount).split(".")
        content.text = if (data.data.direction == CompletedTransaction.Direction.IN) {
            sb.color(colors.transactionAmountIn) {
                size(18.dp()) {
                    append(amount[0])
                }
                if (amount.size > 1) {
                    append(".${amount[1]}")
                }
            }.color(colors.hintText) {
                append(" ${context.getString(R.string.transaction_from)}")
            }
        } else {
            sb.color(colors.transactionAmountOut) {
                size(18.dp()) {
                    append(amount[0])
                }
                if (amount.size > 1) {
                    append(".${amount[1]}")
                }
            }.color(colors.hintText) {
                append(" ${context.getString(R.string.transaction_to)}")
            }
        }.append("\n").color(colors.bodyText) {
            append(WalletAddressFormatter.format(data.data.address, startCount = 6, endCount = 7))
        }.append("\n").color(colors.hintText) {
            append(
                context.getString(
                    R.string.transaction_fee, TonCoinsFormatter.format(data.data.fee)
                )
            )
        }
        comment.visibility = if (data.data.message.isBlank()) {
            View.GONE
        } else {
            comment.text = data.data.message
            View.VISIBLE
        }
    }
}