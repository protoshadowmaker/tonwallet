package ton.coin.wallet.feature.send.scanqr

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.bluelinelabs.conductor.Controller
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.FrameLayoutLpBuilder
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.darkToolbar
import ton.coin.wallet.data.DraftTransaction
import ton.coin.wallet.data.TonCoins
import java.math.BigInteger
import java.util.concurrent.Executors

class ScanQrController(
    private val modalMode: Boolean = false
) : ViewModelController(), LifecycleOwner {

    var resultCallback: ((transaction: DraftTransaction) -> Unit)? = null

    private val viewModel: ScanQrViewModel by lazy {
        ScanQrViewModel().apply {
            attachViewModel(this)
        }
    }

    private val requestCode: Int = 1000
    private var root: FrameLayout? = null
    private var content: View? = null
    private var preview: PreviewView? = null
    private var found: Boolean = false

    init {
        addLifecycleListener(
            object : LifecycleListener() {
                override fun postAttach(controller: Controller, view: View) {
                    checkPermission()
                }
            },
        )
    }

    override val lifecycle: Lifecycle
        get() = lifecycleOwner.lifecycle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?
    ): View {
        val context = container.context
        val contentLayout = LinearLayout(context).apply {
            content = this
            orientation = LinearLayout.VERTICAL
            addView(darkToolbar(context).apply {
                background = null
                navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_back_24).apply {
                    this?.setTint(ContextCompat.getColor(context, R.color.white))
                }
                setTitle(R.string.san_title)
                setNavigationOnClickListener { onNavigationPressed() }
                elevation = 0f
            }, LinearLayoutLpBuilder().wMatch().build())
        }
        val root = FrameLayout(inflater.context).apply {
            setBackgroundResource(R.color.black)
            setOnClickListener {}
            addView(PreviewView(context).apply {
                preview = this
            }, FrameLayoutLpBuilder().wMatch().hMatch().build())
            addView(contentLayout, FrameLayoutLpBuilder().wMatch().hMatch().build().apply {
                gravity = Gravity.BOTTOM
            })
        }
        this.root = root
        return root
    }

    private fun checkPermission() {
        val activity = this.activity as ComponentActivity? ?: return
        registerForActivityResult(requestCode)
        when {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchPreview()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
            }

            else -> requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != this.requestCode) {
            return
        }
        if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            launchPreview()
        }
    }

    private fun launchPreview() {
        lifecycleScope.launch {
            startCameraPreview()
        }
    }

    private suspend fun startCameraPreview() {
        val context = activity ?: return
        val cameraPreview = preview ?: return
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        val previewUseCase = Preview.Builder().build()
        previewUseCase.setSurfaceProvider(cameraPreview.surfaceProvider)
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE
        ).build()
        val scanner = BarcodeScanning.getClient(options)
        val analysisUseCase = ImageAnalysis.Builder().build()
        analysisUseCase.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
            processImageProxy(scanner, imageProxy)
        }
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                previewUseCase,
                analysisUseCase
            )
        } catch (ignore: Throwable) {
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {
        val image = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(
            image,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodeList ->
                val barcode = barcodeList.getOrNull(0)
                barcode?.rawValue?.let { value ->
                    handleQr(value)
                }
            }
            .addOnFailureListener {
            }.addOnCompleteListener {
                imageProxy.image?.close()
                imageProxy.close()
            }
    }

    private fun handleQr(qr: String) {
        if (!qr.startsWith("ton://transfer/")) {
            return
        }
        try {
            val uri = Uri.parse(qr)
            val walletAddress = uri.lastPathSegment
            val amount = try {
                BigInteger(uri.getQueryParameter("amount") ?: "0")
            } catch (e: Throwable) {
                BigInteger.ZERO
            }
            val comment = uri.getQueryParameter("text")
            onFound(
                DraftTransaction(
                    address = walletAddress,
                    amount = TonCoins(amount),
                    comment = comment
                )
            )
        } catch (ignore: Throwable) {
        }
    }

    private fun onFound(transaction: DraftTransaction) {
        if (found) {
            return
        }
        found = true
        resultCallback?.let {
            it(transaction)
            router.popCurrentController()
        }
    }

    private fun onNavigationPressed() {
        router.popCurrentController()
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        lifecycleScope.launch {

        }
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        content?.updatePadding(
            systemBarInsets.left,
            if (modalMode) 0 else systemBarInsets.top,
            systemBarInsets.right,
            systemBarInsets.bottom,
        )
    }
}