package ton.coin.wallet.feature.home.adapter

import io.github.landarskiy.reuse.TypedDiffEntry
import ton.coin.wallet.data.CompletedTransaction

class TransactionEntry(val data: CompletedTransaction) : TypedDiffEntry<TransactionEntry>() {
    override fun isSameContentTyped(other: TransactionEntry): Boolean {
        return data == other.data
    }

    override fun isSameEntryTyped(other: TransactionEntry): Boolean {
        return data.id == other.data.id
    }
}