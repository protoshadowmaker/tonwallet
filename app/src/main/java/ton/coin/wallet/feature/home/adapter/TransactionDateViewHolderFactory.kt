package ton.coin.wallet.feature.home.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.github.landarskiy.reuse.ReuseViewHolder
import io.github.landarskiy.reuse.ViewHolderFactory
import io.github.landarskiy.reuse.annotation.ReuseFactory
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.transactionDateText

@ReuseFactory
class TransactionDateViewHolderFactory : ViewHolderFactory<TransactionDateEntry>() {
    override val typeId: Int = View.generateViewId()
    override fun createView(context: Context, parent: ViewGroup?): View {
        return transactionDateText(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun createViewHolder(view: View): ReuseViewHolder<TransactionDateEntry> {
        return TransactionDateViewHolder(view)
    }
}