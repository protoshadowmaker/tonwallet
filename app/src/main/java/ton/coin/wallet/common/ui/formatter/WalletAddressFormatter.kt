package ton.coin.wallet.common.ui.formatter

object WalletAddressFormatter {
    fun format(address: String, startCount: Int = 4, endCount: Int = 4): String {
        val trimmed = address.trim()
        return if (trimmed.length <= startCount + endCount) {
            trimmed
        } else {
            "${trimmed.take(startCount)}...${trimmed.takeLast(endCount)}"
        }
    }
}