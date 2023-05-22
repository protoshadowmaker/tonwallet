package ton.coin.wallet.common.ui.custom.popuphint

import io.github.landarskiy.reuse.TypedDiffEntry

data class PopupHintEntry(val text: String, val clickListener: (mnemonic: String) -> Unit) :
    TypedDiffEntry<PopupHintEntry>() {

    override fun isSameEntryTyped(other: PopupHintEntry): Boolean {
        return text == other.text
    }

    override fun isSameContentTyped(other: PopupHintEntry): Boolean {
        return text == other.text
    }
}