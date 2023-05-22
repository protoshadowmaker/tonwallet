package ton.coin.wallet.common.ui.custom.popuphint

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.github.landarskiy.reuse.ReuseViewHolder
import io.github.landarskiy.reuse.ViewHolderFactory
import io.github.landarskiy.reuse.annotation.ReuseFactory
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.dp

@ReuseFactory
class PopupHintViewHolderFactory : ViewHolderFactory<PopupHintEntry>() {
    override val typeId: Int = View.generateViewId()

    override fun createView(context: Context, parent: ViewGroup?): View {
        return bodyText(context).apply {
            setPadding(16.dp(), 12.dp(), 16.dp(), 12.dp())
        }
    }

    override fun createViewHolder(view: View): ReuseViewHolder<PopupHintEntry> {
        return PopupHintViewHolder(view)
    }
}