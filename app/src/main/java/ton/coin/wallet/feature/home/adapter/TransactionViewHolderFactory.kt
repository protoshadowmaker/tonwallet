package ton.coin.wallet.feature.home.adapter

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import io.github.landarskiy.reuse.ReuseViewHolder
import io.github.landarskiy.reuse.ViewHolderFactory
import io.github.landarskiy.reuse.annotation.ReuseFactory
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.hintText
import ton.coin.wallet.common.ui.setTextSizeDp

@ReuseFactory
class TransactionViewHolderFactory : ViewHolderFactory<TransactionEntry>() {
    override val typeId: Int = View.generateViewId()
    private val contentId: Int = View.generateViewId()
    private val commentId: Int = View.generateViewId()
    private val timeId: Int = View.generateViewId()
    override fun createView(context: Context, parent: ViewGroup?): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp(), 12.dp(), 16.dp(), 16.dp())
            addView(FrameLayout(context).apply {
                addView(bodyText(context).apply {
                    id = contentId
                    setTextSizeDp(14)
                    setLineSpacing(6.dp().toFloat(), 1f)
                }, FrameLayoutLpBuilder().wMatch().build())
                addView(hintText(context).apply {
                    id = timeId
                    setTextSizeDp(14)
                }, FrameLayoutLpBuilder().build().apply {
                    gravity = Gravity.TOP or Gravity.END
                })
            }, LinearLayoutLpBuilder().wMatch().build())
            addView(bodyText(context).apply {
                id = commentId
                setBackgroundResource(R.drawable.bg_rect_round_4_10_10_10)
                setPadding(12.dp(), 10.dp(), 12.dp(), 10.dp())
            }, LinearLayoutLpBuilder().build().apply {
                setMargins(0, 10.dp(), 0, 0)
            })
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun createViewHolder(view: View): ReuseViewHolder<TransactionEntry> {
        return TransactionViewHolder(
            view,
            content = view.findViewById(contentId),
            comment = view.findViewById(commentId),
            time = view.findViewById(timeId)
        )
    }
}