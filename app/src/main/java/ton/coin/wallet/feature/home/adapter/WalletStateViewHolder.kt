package ton.coin.wallet.feature.home.adapter

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.Button
import android.widget.TextView
import io.github.landarskiy.reuse.ReuseViewHolder
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.formatter.TonCoinsFormatter
import ton.coin.wallet.common.ui.formatter.WalletAddressFormatter
import ton.coin.wallet.common.ui.span.size

class WalletStateViewHolder(
    view: View,
    private val walletAddressTextView: TextView,
    private val walletBalanceTextView: TextView,
    private val sendButton: Button,
    private val receiveButton: Button,
) : ReuseViewHolder<WalletStateEntry>(view) {

    override fun bind(data: WalletStateEntry) {
        val address = data.data.address
        walletAddressTextView.text = WalletAddressFormatter.format(address)
        val amountParts = TonCoinsFormatter.format(data.data.balance, scale = 4).split(".")
        walletBalanceTextView.text = SpannableStringBuilder().append(amountParts[0]).apply {
            if (amountParts.size > 1) {
                size(32.dp()) { append(".${amountParts[1]}") }
            }
        }
        sendButton.setOnClickListener {
            data.onSendCallback()
        }
        receiveButton.setOnClickListener {
            data.onReceiveCallback()
        }
    }
}