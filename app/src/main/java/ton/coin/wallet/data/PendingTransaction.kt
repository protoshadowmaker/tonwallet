package ton.coin.wallet.data

import java.util.UUID

data class PendingTransaction(
    val uuid: UUID = UUID.randomUUID(),
    val address: String = "",
    val amount: TonCoins = TonCoins(),
    val comment: String = ""
)