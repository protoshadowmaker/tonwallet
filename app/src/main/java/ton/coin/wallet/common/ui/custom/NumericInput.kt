package ton.coin.wallet.common.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.InputType
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import ton.coin.wallet.R
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.Theme
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.setTextSizeDp

class NumericInput(context: Context) : FrameLayout(context) {

    var number
        set(value) {
            editText.number = value
            editText.updateNumberPaint()
            invalidate()
        }
        get() = editText.number
    val editText: NumericEditText = NumericEditText(context)
    var text: String
        get() = editText.text?.toString() ?: ""
        set(value) {
            editText.text = Editable.Factory.getInstance().newEditable(value)
            editText.setSelection(value.length)
        }

    init {
        setWillNotDraw(false)
        addView(editText, FrameLayoutLpBuilder().wMatch().hWrap().build())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            save()
            translate(
                editText.maxNumberWidth - editText.currentNumberWidth,
                editText.paddingTop.toFloat()
            )
            editText.textLayout.draw(this)
            restore()
        }
    }
}

class NumericEditText(context: Context) : AppCompatEditText(context) {

    private var numberPaint: TextPaint
    var maxNumberWidth = 0f
        private set
    var currentNumberWidth = 0f
        private set
    private var drawText: String = ""
    var textLayout: StaticLayout
        private set
    var number = 0
        set(value) {
            if (field == value) {
                return
            }
            field = value
            drawText = "$value: "
            measureCurrentNumber()
            updateTextLayout()
        }

    init {
        numberPaint = TextPaint(paint)
        textLayout = StaticLayout.Builder.obtain(
            drawText, 0, drawText.length, numberPaint, 100.dp()
        ).build()
        maxLines = 1
        setLines(1)
        setTextSizeDp(15)
        setTextColor(Theme.DEFAULT.lightColors.bodyText)
        setTextCursorDrawable(R.drawable.text_cursor_light)
        background = Theme.createEditTextDrawable(context, Theme.DEFAULT.lightColors)
        //setImeOptions((if (number != editTexts.size - 1) EditorInfo.IME_ACTION_NEXT else EditorInfo.IME_ACTION_DONE) or EditorInfo.IME_FLAG_NO_EXTRACT_UI)
        inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
    }

    fun updateNumberPaint() {
        numberPaint = TextPaint(paint)
        numberPaint.color = Theme.DEFAULT.lightColors.numericItemText
        maxNumberWidth = numberPaint.measureText("00: ")
        setPadding(maxNumberWidth.toInt(), 12.dp(), paddingRight, 12.dp())
        measureCurrentNumber()
        updateTextLayout()
    }

    private fun updateTextLayout() {
        textLayout = StaticLayout.Builder.obtain(
            drawText,
            0,
            drawText.length,
            numberPaint,
            maxNumberWidth.toInt()
        ).build()
    }

    private fun measureCurrentNumber() {
        currentNumberWidth = numberPaint.measureText(drawText)
    }
}