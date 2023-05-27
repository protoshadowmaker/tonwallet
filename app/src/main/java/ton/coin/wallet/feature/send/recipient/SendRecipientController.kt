package ton.coin.wallet.feature.send.recipient

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.coloredButton
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.conductor.VerticalFadeChangeFromHandler
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.headlineText
import ton.coin.wallet.common.ui.hintText
import ton.coin.wallet.common.ui.input
import ton.coin.wallet.common.ui.lightToolbar
import ton.coin.wallet.common.ui.textButton
import ton.coin.wallet.feature.send.amount.SendAmountController
import ton.coin.wallet.feature.send.scanqr.ScanQrController

class SendRecipientController : ViewModelController() {

    private val viewModel: SendRecipientViewModel by lazy {
        SendRecipientViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: FrameLayout? = null
    private var card: LinearLayout? = null
    private var addressEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?
    ): View {
        val context = container.context
        val contentLayout = LinearLayout(context).apply {
            card = this
            orientation = LinearLayout.VERTICAL
            addView(lightToolbar(context).apply {
                background = null
                setTitle(R.string.send_title)
                elevation = 0f
            }, LinearLayoutLpBuilder().wMatch().build())
            addView(headlineText(context).apply {
                setText(R.string.send_address_title)
                setPadding(20.dp(), 0, 20.dp(), 0)
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(0, 8.dp(), 0, 0)
            })
            addView(input(context).apply {
                addressEditText = this
                setHint(R.string.send_address_hint)
                setLines(2)
                minLines = 2
                maxLines = 2
                gravity = Gravity.TOP
                setPadding(0, 0, 0, 8.dp())
                addTextChangedListener {
                    doAfterTextChanged { addressEditable ->
                        viewModel.onAddressChanged(addressEditable.toString())
                    }
                }
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(20.dp(), 12.dp(), 20.dp(), 0)
            })
            addView(hintText(context).apply {
                setText(R.string.send_address_hint_additional)
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(20.dp(), 8.dp(), 20.dp(), 0)
            })
            addView(textButton(context).apply {
                setText(R.string.send_scan)
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_scan_small, 0, 0, 0)
                minWidth = 0.dp()
                setPadding(12.dp(), 0, 12.dp(), 0)
                setOnClickListener { onScan() }
            }, LinearLayoutLpBuilder().wWrap().hWrap().build().apply {
                setMargins(8.dp(), 4.dp(), 8.dp(), 0)
            })
            addView(FrameLayout(context).apply {
                addView(coloredButton(context).apply {
                    setText(R.string.send_continue)
                    setOnClickListener { onContinueClicked() }
                }, FrameLayoutLpBuilder().wMatch().hWrap().build().apply {
                    setMargins(16.dp(), 0, 16.dp(), 16.dp())
                    gravity = Gravity.BOTTOM
                })
            }, LinearLayoutLpBuilder().wMatch().hMatch().build())
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
        when (state.status) {
            is ScreenState.Successful -> {
                if (addressEditText?.text.toString().isBlank()) {
                    val address = state.draftTransaction.address ?: ""
                    addressEditText?.setText(address)
                    addressEditText?.setSelection(address.length)
                }
            }

            is ScreenState.Redirection -> {
                viewModel.consumeRedirection()
                onContinue()
            }

            else -> {}
        }
    }

    private fun onContinueClicked() {
        viewModel.onContinuePressed()
    }

    private fun onContinue() {
        router.pushController(
            RouterTransaction.with(SendAmountController())
                .pushChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                .popChangeHandler(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
        )
    }

    private fun onScan() {
        val router = parentController?.router ?: router
        router.pushController(
            RouterTransaction.with(ScanQrController().apply {
                resultCallback = { transaction ->
                    val address = transaction.address ?: ""
                    viewModel.onAddressChanged(address)
                }
            })
                .pushChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION))
                .popChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION))
        )
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        card?.updatePadding(0, 0, 0, maxOf(systemBarInsets.bottom, imeInsets.bottom))
    }
}