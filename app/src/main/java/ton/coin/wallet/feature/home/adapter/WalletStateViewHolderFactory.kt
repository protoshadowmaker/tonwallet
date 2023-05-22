package ton.coin.wallet.feature.home.adapter

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import io.github.landarskiy.reuse.ReuseViewHolder
import io.github.landarskiy.reuse.ViewHolderFactory
import io.github.landarskiy.reuse.annotation.ReuseFactory
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.coloredButton
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.span.drawable
import ton.coin.wallet.common.ui.walletBalanceText
import ton.coin.wallet.common.ui.walletTextDark

@ReuseFactory
class WalletStateViewHolderFactory : ViewHolderFactory<WalletStateEntry>() {
    override val typeId: Int = View.generateViewId()

    private val addressId: Int = View.generateViewId()
    private val balanceId: Int = View.generateViewId()
    private val receiveId: Int = View.generateViewId()
    private val sendId: Int = View.generateViewId()

    override fun createView(context: Context, parent: ViewGroup?): View {
        return FrameLayout(context).apply {
            setBackgroundResource(R.color.black)
            setPadding(20.dp(), 20.dp(), 20.dp(), 16.dp())
            val top = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(
                    walletTextDark(context).apply {
                        id = addressId
                    },
                    LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                    })
                addView(LinearLayout(context).apply {
                    addView(
                        LottieAnimationView(context).apply {
                            repeatCount = LottieDrawable.INFINITE
                            setAnimation(R.raw.main)
                            playAnimation()
                        }, LinearLayoutLpBuilder().wDp(44).hDp(56).build()
                    )
                    addView(walletBalanceText(context).apply {
                        id = balanceId
                    })
                }, LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                })
            }
            addView(top, FrameLayoutLpBuilder().wMatch().hWrap().build().apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            })
            val bottom = LinearLayout(context).apply {
                weightSum = 2f
                addView(coloredButton(context).apply {
                    id = receiveId
                    text = SpannableStringBuilder().drawable(context, R.drawable.ic_receive) {
                        append("*")
                    }.append(" ").append(context.getString(R.string.home_receive))
                }, LinearLayoutLpBuilder().wMatch().hMatch().build().apply {
                    marginEnd = 6.dp()
                    weight = 1f
                })
                addView(coloredButton(context).apply {
                    id = sendId
                    text = SpannableStringBuilder().drawable(context, R.drawable.ic_send) {
                        append("*")
                    }.append(" ").append(context.getString(R.string.home_send))
                }, LinearLayoutLpBuilder().wMatch().hMatch().build().apply {
                    marginStart = 6.dp()
                    weight = 1f
                })
            }
            addView(bottom, FrameLayoutLpBuilder().wMatch().hDp(48).build().apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            })
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 216.dp())
        }
    }

    override fun createViewHolder(view: View): ReuseViewHolder<WalletStateEntry> {
        return WalletStateViewHolder(
            view,
            walletAddressTextView = view.findViewById(addressId),
            walletBalanceTextView = view.findViewById(balanceId),
            sendButton = view.findViewById(sendId),
            receiveButton = view.findViewById(receiveId)
        )
    }
}