package ton.coin.wallet.feature.home.adapter

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import io.github.landarskiy.reuse.ReuseViewHolder
import io.github.landarskiy.reuse.ViewHolderFactory
import io.github.landarskiy.reuse.annotation.ReuseFactory
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.dp

@ReuseFactory
class LoadingViewHolderFactory : ViewHolderFactory<LoadingEntry>() {
    override val typeId: Int = View.generateViewId()
    override fun createView(context: Context, parent: ViewGroup?): View {
        return FrameLayout(context).apply {
            addView(CircularProgressIndicator(context).apply {
                indicatorSize = 32.dp()
                isIndeterminate = true

            }, FrameLayoutLpBuilder().build().apply {
                gravity = Gravity.CENTER
            }).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    56.dp()
                )
            }
        }
    }

    override fun createViewHolder(view: View): ReuseViewHolder<LoadingEntry> {
        return LoadingViewHolder(view)
    }
}