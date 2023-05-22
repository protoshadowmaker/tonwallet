package ton.coin.wallet.feature.send.amount

import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.core.view.updatePadding
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.Theme
import ton.coin.wallet.common.ui.amountInput
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.bodyTextLight
import ton.coin.wallet.common.ui.coloredButton
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.custom.NumericKeyboard
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.formatter.TonCoinsFormatter
import ton.coin.wallet.common.ui.formatter.WalletAddressFormatter
import ton.coin.wallet.common.ui.lightToolbar
import ton.coin.wallet.common.ui.span.drawable
import ton.coin.wallet.common.ui.span.size
import ton.coin.wallet.common.ui.switch
import ton.coin.wallet.common.ui.textButton
import ton.coin.wallet.data.TonCoins
import ton.coin.wallet.feature.send.comment.SendCommentController
import kotlin.math.max

class SendAmountController : ViewModelController() {

    private val viewModel: SendAmountViewModel by lazy {
        SendAmountViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: FrameLayout? = null
    private var card: LinearLayout? = null
    private var addressTextView: TextView? = null
    private var allAmountTextView: TextView? = null
    private var amountEditText: EditText? = null
    private var sendAllSwitch: SwitchCompat? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?
    ): View {
        val context = container.context
        val contentLayout = LinearLayout(context).apply {
            card = this
            orientation = LinearLayout.VERTICAL

            addView(lightToolbar(context).apply {
                background = null
                navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_back_24).apply {
                    this?.setTint(ContextCompat.getColor(context, R.color.black))
                }
                setNavigationOnClickListener { onNavigationPressed() }
                setTitle(R.string.send_title)
                elevation = 0f
            }, LinearLayoutLpBuilder().wMatch().build().apply {
                weight = 0f
            })

            addView(FrameLayout(context).apply {
                setPadding(20.dp(), 0, 0, 0)
                addView(bodyTextLight(context).apply {
                    addressTextView = this
                }, FrameLayoutLpBuilder().wWrap().hWrap().build().apply {
                    gravity = Gravity.CENTER_VERTICAL
                })
                addView(textButton(context).apply {
                    minWidth = 0
                    setPadding(20.dp(), 0, 20.dp(), 0)
                    setOnClickListener { onNavigationPressed() }
                    setText(R.string.send_recipient_edit)
                }, FrameLayoutLpBuilder().wWrap().hMatch().build().apply {
                    gravity = Gravity.CENTER_VERTICAL or Gravity.END
                })
            }, LinearLayoutLpBuilder().wMatch().hDp(48).build().apply {
                weight = 0f
            })

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(LottieAnimationView(context).apply {
                    repeatCount = LottieDrawable.INFINITE
                    setAnimation(R.raw.main)
                    playAnimation()
                }, LinearLayoutLpBuilder().wDp(44).hDp(56).build().apply {
                    gravity = Gravity.CENTER_VERTICAL
                })
                addView(amountInput(context).apply {
                    amountEditText = this
                    hint = "0"
                    showSoftInputOnFocus = false
                    inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                    requestFocus()
                }, LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                    gravity = Gravity.CENTER_VERTICAL
                })
            }, LinearLayoutLpBuilder().wWrap().hMatch().build().apply {
                setMargins(20.dp(), 0, 20.dp(), 0)
                gravity = Gravity.CENTER
                weight = 1f
            })

            addView(FrameLayout(context).apply {
                setPadding(20.dp(), 0, 0, 0)
                addView(bodyText(context).apply {
                    allAmountTextView = this
                    text = SpannableStringBuilder().append(context.getString(R.string.send_all))
                        .append(" ").drawable(context, R.drawable.ic_diamond_small) { append("*") }
                }, FrameLayoutLpBuilder().wWrap().hWrap().build().apply {
                    gravity = Gravity.CENTER_VERTICAL
                })
                addView(switch(context).apply {
                    sendAllSwitch = this
                    setPadding(20.dp(), 0, 20.dp(), 0)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            val availableBalance = viewModel.state.availableBalance
                            val formattedBalance = fullFormatBalance(availableBalance)
                            val balanceToSet = if (formattedBalance == "0") {
                                ""
                            } else {
                                formattedBalance
                            }
                            amountEditText?.apply {
                                setText(balanceToSet)
                            }
                            viewModel.onAmountChanged(formattedBalance)
                            updateAmountSpannable()
                            amountEditText?.setSelection(balanceToSet.length)
                        }
                    }
                }, FrameLayoutLpBuilder().wWrap().hMatch().build().apply {
                    gravity = Gravity.CENTER_VERTICAL or Gravity.END
                })
            }, LinearLayoutLpBuilder().wMatch().hDp(48).build().apply {
                weight = 0f
            })

            addView(coloredButton(context).apply {
                setText(R.string.send_continue)
                setOnClickListener { onContinueClicked() }
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(16.dp(), 0, 16.dp(), 16.dp())
                weight = 0f
            })

            addView(NumericKeyboard(context).apply {
                keyPressListener = { key ->
                    onKeyPressed(key)
                }
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(10.dp(), 0, 10.dp(), 16.dp())
                weight = 0f
            })
        }
        val root = FrameLayout(inflater.context).apply {
            setBackgroundResource(R.drawable.bg_rect_padding_top_12dp)
            setOnClickListener { }
            addView(contentLayout, FrameLayoutLpBuilder().wMatch().hMatch().build().apply {
                gravity = Gravity.BOTTOM
            })
        }
        this.root = root
        return root
    }

    private fun shortFormatBalance(coins: TonCoins): String {
        return TonCoinsFormatter.format(coins, scale = 4)
    }

    private fun fullFormatBalance(coins: TonCoins): String {
        return TonCoinsFormatter.format(coins)
    }

    private fun onKeyPressed(key: String) {
        val edit = amountEditText ?: return
        val currentText = edit.text
        if (key == "." && currentText.contains(".")) {
            return
        }
        val startIndex = edit.selectionStart
        val endIndex = edit.selectionEnd
        val range = endIndex - startIndex
        val keyToAdd = when {
            startIndex == 0 && key == "0" -> "e"
            startIndex == 0 && key == "." -> "0."
            else -> key
        }
        val newIndex: Int
        edit.text = if (keyToAdd == "e") {
            if (range > 0) {
                newIndex = startIndex
                currentText.replace(startIndex, endIndex, "", 0, 0)
            } else {
                newIndex = max(0, startIndex - 1)
                currentText.replace(max(0, startIndex - 1), max(0, startIndex), "", 0, 0)
            }
        } else {
            newIndex = startIndex + keyToAdd.length
            currentText.replace(startIndex, endIndex, keyToAdd, 0, keyToAdd.length)
        }
        viewModel.onAmountChanged(edit.text.toString())
        updateAmountSpannable()
        edit.setSelection(newIndex, newIndex)
        sendAllSwitch?.isChecked = false
    }

    private fun updateAmountSpannable() {
        val edit = amountEditText ?: return
        val rawText = edit.text.toString()
        if (rawText.contains(".")) {
            val parts = rawText.split(".")
            edit.text = SpannableStringBuilder().append(parts[0]).append(".")
                .size(32.dp()) { append(parts[1]) }
        } else {
            edit.setText(rawText)
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        lifecycleScope.launch {
            viewModel.stateFlow.collect {
                onStateChanged(it)
            }
        }
        viewModel.loadData()
    }

    private fun onStateChanged(state: ScreenData) {
        val displayBalance = fullFormatBalance(state.draftTransaction.amount ?: TonCoins())
        if (amountEditText?.text.toString() != displayBalance && displayBalance != "0") {
            amountEditText?.setText(displayBalance)
            updateAmountSpannable()
            amountEditText?.setSelection(displayBalance.length)
        }

        when (state.status) {
            is ScreenState.Loading -> {
                updateUi(state)
            }

            is ScreenState.Successful -> {
                updateUi(state)
            }

            is ScreenState.Redirection -> {
                viewModel.consumeRedirection()
                onContinue()
            }

            else -> {}
        }
    }

    private fun updateUi(state: ScreenData) {
        val context = activity ?: return
        addressTextView?.text =
            SpannableStringBuilder().append(context.getString(R.string.send_recipient)).append(" ")
                .color(Theme.DEFAULT.lightColors.bodyText) {
                    append(
                        WalletAddressFormatter.format(
                            state.draftTransaction.address ?: ""
                        )
                    )
                }
        allAmountTextView?.text =
            SpannableStringBuilder().append(context.getString(R.string.send_all)).append(" ")
                .drawable(context, R.drawable.ic_diamond_small) { append("*") }.append(" ")
                .append(shortFormatBalance(state.availableBalance))
    }

    private fun onContinueClicked() {
        viewModel.onContinuePressed()
    }

    private fun onContinue() {
        router.pushController(
            RouterTransaction.with(SendCommentController())
                .pushChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                .popChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
        )
    }

    private fun onNavigationPressed() {
        router.popCurrentController()
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        card?.updatePadding(0, 0, 0, maxOf(systemBarInsets.bottom, imeInsets.bottom))
    }
}