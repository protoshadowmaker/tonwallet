package ton.coin.wallet.feature.home.adapter

import io.github.landarskiy.reuse.TypedDiffEntry
import java.time.LocalDateTime

class TransactionDateEntry(val data: LocalDateTime) : TypedDiffEntry<TransactionDateEntry>() {
    override fun isSameContentTyped(other: TransactionDateEntry): Boolean {
        return data == other.data
    }

    override fun isSameEntryTyped(other: TransactionDateEntry): Boolean {
        return data == other.data
    }
}