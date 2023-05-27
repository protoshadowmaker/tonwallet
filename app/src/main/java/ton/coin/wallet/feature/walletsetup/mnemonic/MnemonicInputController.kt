package ton.coin.wallet.feature.walletsetup.mnemonic

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.landarskiy.reuse.adapter.DiffAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.custom.NumericInput
import ton.coin.wallet.common.ui.custom.popuphint.PopupHintEntry
import ton.coin.wallet.common.ui.custom.reuse.types.ReuseDefaultContentScope
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.lightToolbar
import kotlin.math.max

abstract class MnemonicInputController<T : MnemonicInputViewModel> : ViewModelController() {
    val viewModel: T by lazy {
        createViewModel().apply {
            attachViewModel(this)
        }
    }

    private var mnemonicJob: Job? = null
    private var mnemonicPopup: PopupWindow? = null
    private val defaultScope = ReuseDefaultContentScope()
    private val listAdapter = DiffAdapter(defaultScope.types)

    private var root: LinearLayout? = null
    private var scrollView: ScrollView? = null
    val numericInputs: MutableList<NumericInput> = mutableListOf()

    abstract fun createViewModel(): T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val context = inflater.context
        numericInputs.clear()
        val content = createContent(context)
        val contentContainer = ScrollView(context).apply {
            isVerticalScrollBarEnabled = false
            addView(content)
            setOnScrollChangeListener { _, _, _, _, _ ->
                closeMnemonicPopup()
            }
            @Suppress("ClickableViewAccessibility")
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN && mnemonicPopup != null) {
                    mnemonicPopup?.dismiss()
                }
                false
            }
        }
        val root = LinearLayout(inflater.context).apply {
            setBackgroundResource(R.color.white)
            orientation = LinearLayout.VERTICAL
            addView(lightToolbar(context).apply {
                navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_back_24).apply {
                    this?.setTint(ContextCompat.getColor(context, R.color.black))
                }
                setNavigationOnClickListener { onNavigationPressed() }
                elevation = 0f
            }, LinearLayoutLpBuilder().wMatch().build())
            addView(
                contentContainer,
                LinearLayoutLpBuilder().wMatch().hMatch().build()
            )
        }
        this.root = root
        this.scrollView = contentContainer
        return root
    }

    fun createNumericInput(context: Context): NumericInput {
        val numericInput = NumericInput(context).apply {
            number = 1
        }
        numericInput.editText.addTextChangedListener {
            val text = it?.toString() ?: ""
            val words = text.split("\n", " ", ",", ".", "-")
            if (words.size == numericInputs.size) {
                numericInputs.forEachIndexed { index, numericInput ->
                    numericInput.text = words[index]
                }
            } else {
                searchMnemonic(numericInput)
            }
        }
        numericInput.editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                closeMnemonicPopup()
            }
            if (hasFocus) {
                searchMnemonic(numericInput)
            }
        }
        @Suppress("ClickableViewAccessibility")
        numericInput.editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && mnemonicPopup == null) {
                searchMnemonic(numericInput)
            }
            false
        }
        return numericInput
    }

    open fun onNavigationPressed() {
        router.popCurrentController()
    }

    abstract fun createContent(context: Context): View

    private fun searchMnemonic(target: NumericInput) {
        val context = router.activity ?: return
        val popup = mnemonicPopup
        val location = intArrayOf(0, 0)
        target.getLocationInWindow(location)
        val showAction = { popupToDisplay: PopupWindow ->
            popupToDisplay.showAtLocation(
                target,
                Gravity.START or Gravity.TOP,
                location[0],
                location[1] - 44.dp()
            )
        }
        if (popup != null) {
            searchMnemonics(target.text, showAction) {
                target.text = it
                closeMnemonicPopup()
            }
            return
        }

        val popupElevation = 2.dp()
        val popupView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = listAdapter
        }
        mnemonicPopup = PopupWindow(popupView, 200.dp(), 44.dp(), false).apply {
            animationStyle = R.style.PopupAnimationGravityBottom
            elevation = popupElevation.toFloat()
            setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_rect_round_6dp))
            setOnDismissListener {
                mnemonicJob?.cancel()
                mnemonicPopup = null
            }
        }
        searchMnemonics(target.text, showAction) {
            target.text = it
            closeMnemonicPopup()
        }
    }

    private fun searchMnemonics(
        query: String,
        showAction: (PopupWindow) -> Unit,
        selectCallback: (mnemonic: String) -> Unit
    ) {
        mnemonicJob?.cancel()
        mnemonicJob = lifecycleScope.launch {
            viewModel.searchMnemonic(query).collect { mnemonics ->
                val popup = mnemonicPopup ?: return@collect
                val recycler = (popup.contentView as RecyclerView)
                listAdapter.setItems(
                    defaultScope.newDataBuilder().withPopupHintViewHolderFactory(
                        mnemonics.map {
                            PopupHintEntry(it) { selectedMnemonic ->
                                selectCallback(selectedMnemonic)
                            }
                        }).build()
                )
                if (mnemonics.isEmpty()) {
                    popup.dismiss()
                } else if (!popup.isShowing) {
                    showAction(popup)
                    recycler.scrollToPosition(0)
                } else {
                    recycler.scrollToPosition(0)
                }
            }
        }
    }

    private fun closeMnemonicPopup() {
        mnemonicJob?.cancel()
        mnemonicPopup?.dismiss()
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        closeMnemonicPopup()
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        root?.updatePadding(
            max(systemBarInsets.left, imeInsets.left),
            max(systemBarInsets.top, imeInsets.top),
            max(systemBarInsets.right, imeInsets.right),
            max(systemBarInsets.bottom, imeInsets.bottom)
        )
        if (imeInsets.bottom == 0) {
            closeMnemonicPopup()
        }
    }
}