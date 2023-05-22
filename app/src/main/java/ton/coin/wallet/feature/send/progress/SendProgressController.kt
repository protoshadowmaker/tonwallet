package ton.coin.wallet.feature.send.progress

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.coloredButton
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.formatter.TonCoinsFormatter
import ton.coin.wallet.common.ui.lightToolbar
import ton.coin.wallet.common.ui.titleText
import ton.coin.wallet.common.ui.walletTextMonoLight

class SendProgressController : ViewModelController() {

    private val viewModel: SendProgressViewModel by lazy {
        SendProgressViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: FrameLayout? = null
    private var card: LinearLayout? = null
    private var lottieView: LottieAnimationView? = null
    private var title: TextView? = null
    private var description: TextView? = null
    private var address: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?
    ): View {
        val context = container.context
        val contentLayout = LinearLayout(context).apply {
            card = this
            orientation = LinearLayout.VERTICAL
            addView(lightToolbar(context).apply {
                background = null
                navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_close_24).apply {
                    this?.setTint(ContextCompat.getColor(context, R.color.black))
                }
                setNavigationOnClickListener { onNavigationPressed() }
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                weight = 0f
            })
            addView(FrameLayout(context).apply {
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(
                        LottieAnimationView(context).apply {
                            lottieView = this
                            setAnimation(R.raw.waiting_ton)
                            repeatCount = LottieDrawable.INFINITE
                            playAnimation()
                        }, LinearLayoutLpBuilder().wDp(100).hDp(100).build().apply {
                            gravity = Gravity.CENTER
                        }
                    )
                    addView(titleText(context).apply {
                        title = this
                        setText(R.string.send_progress)
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                        setMargins(0, 12.dp(), 0, 0)
                    })
                    addView(bodyText(context).apply {
                        description = this
                        setText(R.string.send_progress_description)
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                        setMargins(0, 12.dp(), 0, 0)
                    })
                    addView(walletTextMonoLight(context).apply {
                        address = this
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                        setMargins(0, 24.dp(), 0, 0)
                    })
                }, FrameLayoutLpBuilder().wMatch().hWrap().build().apply {
                    setMargins(40.dp(), 0, 40.dp(), 0)
                    gravity = Gravity.CENTER
                })
            }, LinearLayoutLpBuilder().wMatch().hMatch().build().apply {
                weight = 1f
            })
            addView(coloredButton(context).apply {
                setText(R.string.send_view_wallet)
                setOnClickListener { onNavigationPressed() }
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                weight = 0f
                setMargins(16.dp(), 0, 16.dp(), 16.dp())
            })
        }
        val root = FrameLayout(inflater.context).apply {
            setBackgroundResource(R.drawable.bg_rect_padding_top_12dp)
            setOnClickListener {}
            addView(contentLayout, FrameLayoutLpBuilder().wMatch().hMatch().build().apply {
                gravity = Gravity.BOTTOM
            })
        }
        this.root = root
        return root
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        lifecycleScope.launch {
            viewModel.stateFlow.collect {
                onStateChanged(it)
            }
        }
    }

    private fun onStateChanged(state: ScreenData) {
        val context = activity ?: return
        when (state.status) {
            is ScreenState.Successful -> {
                lottieView?.apply {
                    setAnimation(R.raw.success)
                    repeatCount = LottieDrawable.INFINITE
                    playAnimation()
                }
                title?.setText(R.string.send_done)
                description?.text = context.getString(
                    R.string.send_success_amount,
                    TonCoinsFormatter.format(state.transaction.amount, 4)
                )
                val addressText = state.transaction.address
                val middleIndex = addressText.length / 2
                val top = addressText.substring(0, middleIndex)
                val bottom = addressText.substring(middleIndex)
                val displayText = "$top\n$bottom"
                address?.text = displayText
            }

            else -> {}
        }
    }

    private fun onNavigationPressed() {
        router.popCurrentController()
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        card?.updatePadding(0, 0, 0, maxOf(systemBarInsets.bottom, imeInsets.bottom))
    }
}