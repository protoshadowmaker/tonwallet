package ton.coin.wallet.feature.send.comment

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.bodyText
import ton.coin.wallet.common.ui.coloredProgressButton
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.custom.ProgressButton
import ton.coin.wallet.common.ui.divider
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.formatter.WalletAddressFormatter
import ton.coin.wallet.common.ui.headlineText
import ton.coin.wallet.common.ui.hintText
import ton.coin.wallet.common.ui.input
import ton.coin.wallet.common.ui.lightToolbar
import ton.coin.wallet.common.ui.nameValueTile
import ton.coin.wallet.common.ui.span.drawable
import ton.coin.wallet.feature.send.progress.SendProgressController

class SendCommentController : ViewModelController() {

    private val viewModel: SendCommentViewModel by lazy {
        SendCommentViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: FrameLayout? = null
    private var card: LinearLayout? = null
    private var commentEditText: EditText? = null
    private var recipientTextView: TextView? = null
    private var amountTextView: TextView? = null
    private var progressButton: ProgressButton? = null

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
            addView(ScrollView(context).apply {
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(headlineText(context).apply {
                        setText(R.string.send_comment)
                        setPadding(20.dp(), 0, 20.dp(), 0)
                    }, LinearLayoutLpBuilder().wMatch().hDp(40).build().apply {
                        setMargins(0, 20.dp(), 0, 0)
                    })

                    addView(input(context).apply {
                        commentEditText = this
                        setHint(R.string.send_description)
                        gravity = Gravity.TOP
                        setPadding(0, 0, 0, 8.dp())
                        addTextChangedListener {
                            doAfterTextChanged { addressEditable ->
                                viewModel.onCommentChanged(addressEditable.toString())
                            }
                        }
                    }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                        setMargins(20.dp(), 12.dp(), 20.dp(), 0)
                    })

                    addView(hintText(context).apply {
                        setText(R.string.send_description_hint)
                    }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                        setMargins(20.dp(), 8.dp(), 20.dp(), 0)
                    })

                    addView(headlineText(context).apply {
                        setText(R.string.send_details)
                        setPadding(20.dp(), 0, 20.dp(), 0)
                    }, LinearLayoutLpBuilder().wMatch().hDp(40).build().apply {
                        setMargins(0, 20.dp(), 0, 0)
                    })

                    addView(
                        nameValueTile(context, bodyText(context).apply {
                            setText(R.string.send_details_recipient)
                        }, bodyText(context).apply {
                            recipientTextView = this
                        }),
                        LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                            setMargins(20.dp(), 0, 20.dp(), 0)
                        }
                    )

                    addView(divider(context), LinearLayoutLpBuilder().wMatch().hDp(0.5).build())

                    addView(
                        nameValueTile(context, bodyText(context).apply {
                            setText(R.string.send_details_amount)
                        }, bodyText(context).apply {
                            amountTextView = this
                        }),
                        LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                            setMargins(20.dp(), 0, 20.dp(), 0)
                        }
                    )
                })
            }, LinearLayoutLpBuilder().wMatch().hMatch().build().apply {
                weight = 1f
            })

            addView(coloredProgressButton(context).apply {
                progressButton = this
                setText(R.string.send_confirm_and_send)
                setOnClickListener { onContinueClicked() }
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
        viewModel.loadData()
    }

    private fun onStateChanged(state: ScreenData) {
        val context = activity ?: return
        progressButton?.setAnimationEnabled(enabled = false, animated = true)
        when (state.status) {
            is ScreenState.Successful -> {
                if (commentEditText?.text.toString().isBlank()) {
                    commentEditText?.setText(state.draftTransaction.comment ?: "")
                }
                recipientTextView?.text = WalletAddressFormatter.format(
                    state.draftTransaction.address ?: ""
                )
                amountTextView?.text = SpannableStringBuilder()
                    .drawable(context, R.drawable.ic_diamond_small) { append("*") }
                    .append(" ")
                    .append(state.amountToDisplay)
            }

            is ScreenState.Redirection -> {
                viewModel.consumeRedirection()
                onContinue()
            }

            is ScreenState.Transfering -> {
                progressButton?.setAnimationEnabled(enabled = true, animated = true)
            }

            else -> {}
        }
    }

    private fun onContinueClicked() {
        viewModel.onContinuePressed()
    }

    private fun onContinue() {
        router.setRoot(
            RouterTransaction.with(SendProgressController())
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