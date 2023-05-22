package ton.coin.wallet.feature.home.adapter

import io.github.landarskiy.reuse.TypedDiffEntry

class LoadingEntry : TypedDiffEntry<LoadingEntry>() {
    override fun isSameContentTyped(other: LoadingEntry): Boolean {
        return true
    }

    override fun isSameEntryTyped(other: LoadingEntry): Boolean {
        return true
    }
}