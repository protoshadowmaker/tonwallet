package ton.coin.wallet.feature.settings.mnemonic

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.darkToolbar
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.numericWordsText
import ton.coin.wallet.common.ui.setNumericWords

class ViewMnemonicController : ViewModelController() {

    private val viewModel: ViewMnemonicViewModel by lazy {
        ViewMnemonicViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: LinearLayout? = null
    private var scrollView: View? = null
    private var leftNumericTextView: TextView? = null
    private var rightNumericTextView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val context = inflater.context
        val content = LinearLayout(context).apply {
            setPadding(40.dp(), 0, 40.dp(), 0)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(
                LottieAnimationView(context).apply {
                    setAnimation(R.raw.recovery_phrase)
                    playAnimation()
                }, LinearLayoutLpBuilder().wDp(100).hDp(100).build().apply {
                    setMargins(0, 12.dp(), 0, 40.dp())
                }
            )

            val columnLinearLayout = LinearLayout(context).apply {
                weightSum = 2f
                addView(
                    FrameLayout(context).apply {
                        addView(
                            numericWordsText(context).apply {
                                leftNumericTextView = this
                            },
                            FrameLayoutLpBuilder().wWrap().hWrap().build().apply {
                                gravity = Gravity.CENTER
                            })
                    },
                    LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                        weight = 1f
                    })
                addView(
                    FrameLayout(context).apply {
                        addView(
                            numericWordsText(context).apply {
                                rightNumericTextView = this
                            },
                            FrameLayoutLpBuilder().wWrap().hWrap().build().apply {
                                gravity = Gravity.CENTER
                            })
                    },
                    LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                        weight = 1f
                    })
            }

            addView(columnLinearLayout, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 0, 0, 56.dp())
            })
        }
        val contentContainer = ScrollView(context).apply {
            scrollView = this
            isVerticalScrollBarEnabled = false
            clipToPadding = false
            addView(content)
            setBackgroundResource(R.drawable.bg_rect_round_top_12dp)
        }
        val root = LinearLayout(inflater.context).apply {
            setBackgroundResource(R.color.black)
            orientation = LinearLayout.VERTICAL
            addView(darkToolbar(context).apply {
                navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_back_24).apply {
                    this?.setTint(ContextCompat.getColor(context, R.color.white))
                }
                setTitle(R.string.recovery_title)
                setNavigationOnClickListener { onNavigationPressed() }
                elevation = 0f
            }, LinearLayoutLpBuilder().wMatch().build())
            addView(
                contentContainer,
                LinearLayoutLpBuilder().wMatch().hMatch().build()
            )
        }
        this.root = root
        return root
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        lifecycleScope.launch {
            viewModel.state.collect {
                onStateChanged(it)
            }
        }
    }

    private fun onStateChanged(state: ScreenState) {
        if (state.mnemonic.mnemonic.size == 24) {
            leftNumericTextView?.setNumericWords(state.mnemonic.mnemonic.subList(0, 12))
            rightNumericTextView?.setNumericWords(
                state.mnemonic.mnemonic.subList(
                    12,
                    state.mnemonic.mnemonic.size
                ), 12
            )
        }
    }

    private fun onNavigationPressed() {
        router.popCurrentController()
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        root?.updatePadding(
            systemBarInsets.left,
            systemBarInsets.top,
            systemBarInsets.right,
            0
        )
        scrollView?.setPadding(0, 0, 0, systemBarInsets.bottom)
    }
}