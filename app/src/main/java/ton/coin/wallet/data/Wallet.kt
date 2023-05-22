package ton.coin.wallet.data

import org.ton.bigint.BigInt

sealed class Wallet {
    /**
     * Empty state
     */
    object Empty : Wallet()

    /**
     * Draft state
     */
    data class DraftWallet(val mnemonic: WalletMnemonic) : Wallet()

    /**
     * Created state
     */
    data class UserWallet(
        val mnemonic: WalletMnemonic,
        val version: WalletVersion = WalletVersion.V4R2,
        val balance: TonCoins = TonCoins(BigInt.valueOf(0L)),
        val completedTransactions: List<CompletedTransaction> = emptyList(),
        val pendingTransactions: List<PendingTransaction> = emptyList()
    ) : Wallet()
}

enum class WalletVersion {
    V3R1, V3R2, V4R2;

    companion object {
        fun valueOfOrDefault(name: String?): WalletVersion {
            return values().find { it.name == name } ?: V4R2
        }
    }
}