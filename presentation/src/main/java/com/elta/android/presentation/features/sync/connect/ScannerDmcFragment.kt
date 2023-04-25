package com.elta.android.presentation.features.sync.connect

import android.content.Context
import android.graphics.Rect
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectMainEvent
import com.elta.android.presentation.features.sync.connect.model.ScannerState
import com.elta.android.presentation.features.sync.connect.viewmodel.ScannerDmcViewModel
import com.elta.android.presentation.features.sync.connect.widgets.AppTopBar
import com.elta.android.presentation.features.sync.connect.widgets.HelpBottomSheet
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val NUMBER_SUFFIX = "D"
private const val PIN_MUL_COEFFICIENT = 599681139
private const val PIN_PLUS_COEFFICIENT = 123
private const val PIN_DIV_COEFFICIENT = 1000
private const val NUMBERS_COUNT_FOR_PIN = 6
private const val GLUCOMETER_NAME_SUFFIX = "SatelliteOnline"
private const val NUMBERS_COUNT_FOR_NAME = 4

private enum class CropCornerType(val degrees: Float, val align: Alignment) {
    TopLeft(degrees = 0f, align = Alignment.TopStart),
    TopRight(degrees = 90f, align = Alignment.TopEnd),
    BottomRight(degrees = 180f, align = Alignment.BottomEnd),
    BottomLeft(degrees = 270f, align = Alignment.BottomStart),
}

@ExperimentalCameraProviderConfiguration
@ExperimentalGetImage
class ScannerDmcFragment : BaseComposeFragment<ScannerDmcViewModel>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean) = ScannerDmcFragment().apply {
            arguments = bundle(IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding)
        }
    }

    override val viewModel: ScannerDmcViewModel by viewModels { viewModelFactory }

    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_DATA_MATRIX)
        .build()
    private var scanner: BarcodeScanner? = null

    override fun ScannerDmcViewModel.init() {
        connectByPinButton.setText(getString(R.string.sync_connect_by_pin_boton_text))
        appTopBar.setStartIconAction(AppAction.BackPressure)
        appTopBar.setEndIconAction(ConnectAction.NeedHelp)
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content(viewModel: ScannerDmcViewModel) {
        val event = viewModel.event.collectAsState(initial = null).value
        val state = viewModel.state.collectAsState().value
        val cropSize = state.cropRect
        val sheetState =
            rememberBottomSheetScaffoldState()
        LaunchedEffect(key1 = event) {
            if (event is ConnectMainEvent.HideSheet) {
                sheetState.bottomSheetState.collapse()
            } else {
                sheetState.bottomSheetState.expand()
            }
        }
        GetLocalProperties { _, _, colors, shapes, _ ->
            Box(Modifier.fillMaxSize()) {
                BottomSheetScaffold(
                    scaffoldState = sheetState,
                    topBar = {
                        AppTopBar(
                            appTopBarModel = viewModel.appTopBar,
                            backgroundColor = colors.black,
                            iconColor = colors.white,
                            textColor = colors.white
                        )
                    },
                    sheetGesturesEnabled = false,
                    sheetShape = shapes.sheet,
                    sheetContent = { BottomSheet(viewModel, state.scannerState) },
                    modifier = Modifier.systemBarsPadding()
                ) {
                    CameraPreView(viewModel)
                }
                PreviewCropRect(cropSize, state.scannerState)
            }
        }
    }

    @Composable
    private fun BoxScope.PreviewCropRect(
        cropSize: DpSize,
        scannerState: ScannerState
    ) {
        GetLocalProperties { _, _, colors, shapes, _ ->
            val borderColor = if (scannerState == ScannerState.Error) {
                colors.red
            } else {
                colors.shadeGGreenA
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(cropSize)
                    .background(color = Color.Transparent)
                    .clip(shapes.dishCard)
            ) {
                BorderCorner(borderColor, CropCornerType.TopLeft)
                BorderCorner(borderColor, CropCornerType.TopRight)
                BorderCorner(borderColor, CropCornerType.BottomLeft)
                BorderCorner(borderColor, CropCornerType.BottomRight)
            }
        }
    }

    @Composable
    private fun BoxScope.BorderCorner(
        borderColor: Color,
        cornerType: CropCornerType
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_border_corner),
            modifier = Modifier.Companion
                .align(cornerType.align)
                .rotate(cornerType.degrees),
            colorFilter = ColorFilter.tint(color = borderColor),
            contentDescription = null
        )
    }

    @Composable
    private fun CameraPreView(viewModel: ScannerDmcViewModel) {
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
                viewModel.setCropSize(DpSize(width = cropSizeDp, height = cropSizeDp))
                val cropRect = Rect(left, top, left.plus(cropSize), top.plus(cropSize))
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            ContextCompat.getMainExecutor(requireContext()),
                            getImageAnalyzer(cropRect, viewModel)
                        )
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
        viewModel: ScannerDmcViewModel
    ) =
        ImageAnalysis.Analyzer { imageProxy ->
            imageProxy.setCropRect(cropRect)
            imageProxy.image?.let { image ->
                scanner?.process(
                    InputImage.fromMediaImage(
                        image,
                        imageProxy.imageInfo.rotationDegrees
                    )
                )
                    ?.addOnSuccessListener { barcodes ->
                        handleBarcodes(barcodes, viewModel)
                        imageProxy.close()
                    }
                    ?.addOnFailureListener {
                        viewModel sendAction ConnectAction.ScannerError
                    }
                    ?.addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }

    private fun handleBarcodes(
        barcodes: MutableList<Barcode>,
        viewModel: ScannerDmcViewModel
    ) {
        if (barcodes.isNotEmpty()) {
            barcodes.forEach { code ->
                val value = code.rawValue.orEmpty()
                if (value.startsWith(NUMBER_SUFFIX)) {
                    runCatching {
                        val number = value.drop(1)
                        val pin = number
                            .takeLast(NUMBERS_COUNT_FOR_PIN)
                            .toInt()
                            .times(PIN_MUL_COEFFICIENT)
                            .plus(PIN_PLUS_COEFFICIENT)
                            .mod(PIN_DIV_COEFFICIENT)
                        val name =
                            "$GLUCOMETER_NAME_SUFFIX${number.takeLast(NUMBERS_COUNT_FOR_NAME)}"
                        viewModel sendAction ConnectAction.StartConnecting(pin, name)
                        scanner?.close()
                    }
                        .onFailure {
                            viewModel sendAction ConnectAction.ScannerError
                        }
                }
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

    @Composable
    private fun BottomSheet(viewModel: ScannerDmcViewModel, sheetType: ScannerState) {
        when (sheetType) {
            ScannerState.Info -> InfoSheet()
            ScannerState.Error -> ErrorSheet()
            ScannerState.Help -> HelpBottomSheet(
                downButtonModel = viewModel.connectByPinButton,
                closeOnClick = {
                    viewModel sendAction ConnectAction.CloseHelp
                }
            )
        }
    }

    @Composable
    private fun InfoSheet() {
        GetLocalProperties { dimens, _, _, _, _ ->
            Row(
                modifier = Modifier.padding(dimens.scannerInfoSheetPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_qr_code),
                    contentDescription = null
                )
                HSpacerMedium()
                Text(text = stringResource(id = R.string.sync_connection_scanner_sheet_info_text))
            }
        }
    }

    @Composable
    private fun ErrorSheet() {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.sync_connection_scanner_sheet_error_title),
                    style = types.h3
                )
                VSpacerVerySmall()
                Text(
                    text = stringResource(id = R.string.sync_connection_scanner_sheet_error_text),
                    color = colors.shadeBlack0
                )
            }
        }
    }
}
