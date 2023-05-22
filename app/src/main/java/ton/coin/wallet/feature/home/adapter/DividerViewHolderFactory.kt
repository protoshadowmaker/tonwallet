package ton.coin.wallet.feature.home.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.github.landarskiy.reuse.ReuseViewHolder
import io.github.landarskiy.reuse.ViewHolderFactory
import io.github.landarskiy.reuse.annotation.ReuseFactory
import ton.coin.wallet.common.ui.Theme
import ton.coin.wallet.common.ui.dp

@ReuseFactory
class DividerViewHolderFactory : ViewHolderFactory<DividerEntry>() {
    override val typeId: Int = View.generateViewId()
    override fun createView(context: Context, parent: ViewGroup?): View {
        return View(context).apply {
            setBackgroundColor(Theme.DEFAULT.lightColors.divider)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0.5.dp()
            )
        }
    }

    override fun createViewHolder(view: View): ReuseViewHolder<DividerEntry> {
        return DividerViewHolder(view)
    }
}