package ton.coin.wallet.data

data class DraftTransaction(
    val address: String? = null,
    val amount: TonCoins? = null,
    val comment: String? = null
)