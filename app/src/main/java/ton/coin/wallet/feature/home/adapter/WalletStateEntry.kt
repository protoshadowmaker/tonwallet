package ton.coin.wallet.feature.home.adapter

import io.github.landarskiy.reuse.TypedDiffEntry
import ton.coin.wallet.data.TonCoins

class WalletStateEntry(
    val data: WalletStateData,
    val onReceiveCallback: () -> Unit,
    val onSendCallback: () -> Unit
) : TypedDiffEntry<WalletStateEntry>() {
    override fun isSameContentTyped(other: WalletStateEntry): Boolean {
        return data == other.data
    }

    override fun isSameEntryTyped(other: WalletStateEntry): Boolean {
        return true
    }
}

data class WalletStateData(
    val address: String,
    val balance: TonCoins
)