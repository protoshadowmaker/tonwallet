package ton.coin.wallet.feature.home.adapter

import io.github.landarskiy.reuse.TypedDiffEntry

class DividerEntry : TypedDiffEntry<DividerEntry>() {
    override fun isSameContentTyped(other: DividerEntry): Boolean {
        return true
    }

    override fun isSameEntryTyped(other: DividerEntry): Boolean {
        return true
    }
}