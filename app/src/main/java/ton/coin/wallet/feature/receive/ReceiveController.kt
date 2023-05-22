package ton.coin.wallet.feature.receive

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.bodyTextLight
import ton.coin.wallet.common.ui.coloredButton
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.lightToolbar
import ton.coin.wallet.common.ui.walletTextMonoLight

class ReceiveController : ViewModelController() {

    private val viewModel: ReceiveViewModel by lazy {
        ReceiveViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: FrameLayout? = null
    private var card: LinearLayout? = null
    private var qrView: ImageView? = null;
    private var addressView: TextView? = null;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?
    ): View {
        val context = container.context
        val contentLayout = LinearLayout(context).apply {
            card = this
            orientation = LinearLayout.VERTICAL
            setOnClickListener { }
            setBackgroundResource(R.drawable.bg_rect_round_top_12dp)
            addView(lightToolbar(context).apply {
                background = null
                setTitle(R.string.receive_title)
            })
            addView(bodyTextLight(context).apply {
                setText(R.string.receive_description)
                lineHeight = 20.dp()
                setPadding(20.dp(), 0, 20.dp(), 16.dp())
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            })
            addView(ImageView(context).apply {
                qrView = this
            }, LinearLayoutLpBuilder().wDp(160).hDp(160).build().apply {
                gravity = Gravity.CENTER
            })
            addView(walletTextMonoLight(context).apply {
                addressView = this
                minLines = 2
                setLines(2)
                setPadding(20.dp(), 28.dp(), 20.dp(), 28.dp())
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            })
            addView(coloredButton(context).apply {
                setText(R.string.receive_share)
                setOnClickListener { onShareClicked() }
            }, LinearLayoutLpBuilder().wMatch().hWrap().build().apply {
                setMargins(16.dp(), 0, 16.dp(), 16.dp())
            })
        }
        val root = FrameLayout(inflater.context).apply {
            setBackgroundResource(R.color.black_10)
            setOnClickListener { router.popCurrentController() }
            addView(contentLayout, FrameLayoutLpBuilder().wMatch().hWrap().build().apply {
                gravity = Gravity.BOTTOM
            })
        }
        this.root = root
        return root
    }

    private fun onShareClicked() {
        val address = viewModel.state.sharedWalletAddress
        if (address.isBlank()) {
            return
        }
        val activity = this.activity ?: return
        activity.startActivity(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, address)
        })
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
        if (state.status != ScreenState.Successful || state.walletAddress.isBlank()) {
            return
        }
        val context = activity ?: return
        val data = QrData.Url(state.sharedWalletAddress)
        val options = createQrVectorOptions {
            padding = .125f
            errorCorrectionLevel = QrErrorCorrectionLevel.Auto
            logo {
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_diamond)
                size = .33f
                padding = QrVectorLogoPadding.Natural(.0f)
                shape = QrVectorLogoShape.RoundCorners(.25f)
            }
            colors {
                dark = QrVectorColor.Solid(ContextCompat.getColor(context, R.color.black))
                ball = QrVectorColor.Solid(ContextCompat.getColor(context, R.color.cyan_blue))
            }
            shapes {
                darkPixel = QrVectorPixelShape.RoundCorners(.5f)
                ball = QrVectorBallShape.RoundCorners(.25f)
                frame = QrVectorFrameShape.RoundCorners(.25f)
            }
        }
        qrView?.setImageDrawable(QrCodeDrawable(data, options))
        val middleIndex = state.walletAddress.length / 2
        val address = state.walletAddress
        val displayText = "${address.substring(0, middleIndex)}\n${address.substring(middleIndex)}"
        addressView?.text = displayText
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        root?.updatePadding(
            systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, 0
        )
        card?.updatePadding(0, 0, 0, systemBarInsets.bottom)
    }
}