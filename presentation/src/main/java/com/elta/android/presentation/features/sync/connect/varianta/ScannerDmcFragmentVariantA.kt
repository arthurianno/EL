package com.elta.android.presentation.features.sync.connect

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.varianta.model.ScannerStateVariantA
import com.elta.android.presentation.features.sync.connect.viewmodel.ScannerDmcViewModelVariantA
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.extractPinCode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val NUMBER_SUFFIX = "D"
private const val NUMBER_SUFFIX_2 = "E"
private const val NUMBERS_COUNT_FOR_NAME = 4
private const val SATELLITE_ONLINE_MODEL = "SatelliteOnline"
private const val SATELLITE_VOICE_MODEL = "SatelliteVoice"

// fixme Variant A : improved_enabling_location

@ExperimentalGetImage
class ScannerDmcFragmentVariantA : BaseComposeFragment<ScannerDmcViewModelVariantA>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean) = ScannerDmcFragmentVariantA().apply {
            arguments = bundle(IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding)
        }
    }

    override val viewModel: ScannerDmcViewModelVariantA by viewModels { viewModelFactory }

    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_DATA_MATRIX)
        .build()
    private var scanner: BarcodeScanner? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @Composable
    override fun Content(viewModel: ScannerDmcViewModelVariantA) {
        val state = viewModel.state.collectAsState().value
        DisposableEffect(Unit) {
            onDispose { scanner?.close() }
        }
        DmcScannerScreen(
            state = when (state.scannerState) {
                ScannerStateVariantA.Info -> DmcScannerUiState.Scanning
                ScannerStateVariantA.Error -> DmcScannerUiState.Error
                ScannerStateVariantA.Help -> DmcScannerUiState.Help
            },
            onBack = { viewModel sendAction AppAction.BackPressure },
            onHelp = { viewModel sendAction ConnectAction.NeedHelp },
            onCloseHelp = { viewModel sendAction ConnectAction.CloseHelp },
            onConnectByPin = { viewModel sendAction ConnectAction.ConnectByPin },
            camera = { CameraPreView(viewModel) }
        )
    }

    @Composable
    private fun CameraPreView(viewModel: ScannerDmcViewModelVariantA) {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current
        val previewView: PreviewView = remember { PreviewView(context) }
        GetLocalProperties { dimens, _, _, _, _ ->
            LaunchedEffect(key1 = true) {
                scanner = BarcodeScanning.getClient(scannerOptions)
                val left = density.run { dimens.scannerPreviewLeftPadding.toPx() }.toInt()
                val top = density.run { dimens.scannerPreviewTopPadding.toPx() }.toInt()
                val cropSizeDp = (
                        configuration.screenWidthDp -
                                dimens.scannerPreviewLeftPadding.value.toInt().times(2)
                        ).dp
                val cropSize = density.run { cropSizeDp.toPx() }.toInt()
                val cropRect = Rect(left, top, left.plus(cropSize), top.plus(cropSize))
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(cameraExecutor, getImageAnalyzer(cropRect, viewModel))
                    }
                val cameraProvider = context.getCameraProvider()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalysis,
                    preview
                )
                preview.setSurfaceProvider(previewView.surfaceProvider)
            }
            AndroidView(
                factory =
                { previewView },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    private fun getImageAnalyzer(
        cropRect: Rect,
        viewModel: ScannerDmcViewModelVariantA
    ) =
        ImageAnalysis.Analyzer { imageProxy ->
            imageProxy.setCropRect(cropRect)
            val barcodeScanner = scanner
            if (barcodeScanner == null) {
                imageProxy.close()
                return@Analyzer
            }
            imageProxy.image?.let { image ->
                barcodeScanner.process(
                    InputImage.fromMediaImage(
                        image,
                        imageProxy.imageInfo.rotationDegrees
                    )
                )
                    .addOnSuccessListener { barcodes ->
                        handleBarcodes(barcodes, viewModel)
                    }
                    .addOnFailureListener {
                        viewModel sendAction ConnectAction.ScannerError
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } ?: imageProxy.close()
        }

    private fun handleBarcodes(
        barcodes: MutableList<Barcode>,
        viewModel: ScannerDmcViewModelVariantA
    ) {
        if (barcodes.isNotEmpty()) {
            barcodes.forEach { code ->
                val value = code.rawValue.orEmpty().trim()
                val isVoice = value.startsWith(NUMBER_SUFFIX_2)
                val isOnline = value.startsWith(NUMBER_SUFFIX)
                if (isVoice || isOnline) {
                    runCatching {
                        val pin = value.extractPinCode()
                        val modelName = if (isVoice) SATELLITE_VOICE_MODEL else SATELLITE_ONLINE_MODEL
                        val name = "$modelName${value.takeLast(NUMBERS_COUNT_FOR_NAME)}"
                        viewModel sendAction ConnectAction.StartConnecting(pin, name)
                        scanner?.close()
                    }
                        .onFailure {
                            viewModel sendAction ConnectAction.ScannerError
                        }
                } else viewModel sendAction ConnectAction.ScannerError
            }
        }
    }

    private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            ProcessCameraProvider.getInstance(this).also { cameraProvider ->
                cameraProvider.addListener({
                    continuation.resume(cameraProvider.get(1, TimeUnit.MINUTES))
                }, ContextCompat.getMainExecutor(this))
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}
