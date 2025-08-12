package com.elta.android.presentation.features.newsChannel.ui.components


import android.content.Context
import android.net.Uri
import android.provider.CalendarContract.Colors
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.features.newsChannel.model.MessageUiEntity
import com.elta.android.presentation.features.newsChannel.model.NewsViewState
import com.elta.android.presentation.features.newsChannel.ui.components.content.NewsChatContent
import com.elta.android.presentation.features.newsChannel.ui.components.top.NewsTopBar
import com.elta.android.presentation.theme.GetLocalProperties
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewsContent(
    state: NewsViewState,
    sheetState: ModalBottomSheetState,
    listState: LazyListState,
    onFileSelectClick: () -> Unit = {},
    onPictureArrowBackClick: () -> Unit = {},
    onTopBarBackClick: () -> Unit = {},
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onLongMessageClick: (MessageUiEntity) -> Unit = {},
    onDocumentIconClick: (MessageUiEntity) -> Unit = {},
    onCopyClick: (MessageUiEntity) -> Unit = {},
    onDismissClick: () -> Unit = {},
    onSwipeRefresh: () -> Unit = {},
    onDownIconClick: () -> Unit = {},
    onDownloadClick: (String?) -> Unit = { _ -> },
    onShareClick: (String?) -> Unit = { _ -> },
    onScrollToTop: () -> Unit = {},
    onLoadNextPage: () -> Unit = {}
) {
    GetLocalProperties { _, _, colors, shapes, _ ->
        ModalBottomSheetLayout(
            sheetContent = {
                BottomSheetDialog(
                    onFileSelectClick = onFileSelectClick
                )
            },
            sheetState = sheetState,
            sheetShape = shapes.sheet
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        NewsTopBar(
                            connectState = state.connectState,
                            onBackButtonClick = onTopBarBackClick
                        )
                    }
                ) { paddingValues ->
                    NewsChatContent(
                        modifier = Modifier.padding(paddingValues),
                        chat = state.chat,
                        connectState = state.connectState,
                        contentMenuEntity = state.contextMenuEntity,
                        hasNewMessages = state.hasNewMessages,
                        listState = listState,
                        isLoadingNextMessagesPage = state.isLoadingNextMessagesPage,
                        isSwipeRefreshing = state.isSwipeRefreshing, // Передаем isSwipeRefreshing
                        onMessageClick = onMessageClick,
                        onLongMessageClick = onLongMessageClick,
                        onDocumentIconClick = onDocumentIconClick,
                        onCopyClick = onCopyClick,
                        onDismissClick = onDismissClick,
                        onSwipeRefresh = onSwipeRefresh,
                        onDownIconClick = onDownIconClick,
                        onScrollToTop = onScrollToTop,
                        onLoadNextPage = onLoadNextPage
                    )
                }
                AnimatedVisibility(
                    visible = state.previewState.isPhotoPreview,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200)),
                    modifier = Modifier.fillMaxSize()
                ) {
                     FullScreenImagePreview(
                         imageData = state.previewState.imageData,
                         onBackClick = onPictureArrowBackClick,
                         onDownloadClick = { onDownloadClick(state.previewState.imageData) },
                         onShareClick ={ onShareClick(state.previewState.imageData) }
                     )
                }
                AnimatedVisibility(
                    visible = state.contextMenuEntity.isOpenContextMenu,
                    enter = fadeIn(animationSpec = tween(100)),
                    exit = fadeOut(animationSpec = tween(100))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = colors.black.copy(alpha = 0.7f))
                            .clickableWithNoRipple { onDismissClick() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun FullScreenImagePreview(
    imageData: String?,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit
) {
    GetLocalProperties { _, _, colors, _, _ ->
        val context = LocalContext.current
        val processedImageData = remember(imageData) { decodeImageData(imageData) }
        // Запоминаем ImageRequest, чтобы он не пересоздавался при рекомпозиции
        val imageRequest = remember(processedImageData) {
            createImageRequest(context, processedImageData)
        }

        Log.d("FullScreenImagePreview", "Loading image data: imageData=$imageData")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.black)
        ) {
            ImageContent(
                imageRequest = imageRequest,
                processedImageData = processedImageData,
                colors = colors.black
            )

            // Панель управления всегда поверх картинки
            ImagePreviewControlPanel(
                colors = Color.White, // Изменил на белый для лучшей видимости
                onBackClick = onBackClick,
                onDownloadClick = onDownloadClick,
                onShareClick = onShareClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .zIndex(10f) // Убедимся что панель всегда сверху
            )
        }
    }
}


@Composable
private fun ImageContent(
    imageRequest: ImageRequest,
    processedImageData: ByteArray?,
    colors: Color
) {
    if (processedImageData == null) {
        Image(
            painter = ColorPainter(colors),
            contentDescription = stringResource(R.string.content_description_clear_button),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        Log.e("FullScreenImagePreview", "No valid image data provided")
    } else {
        AsyncImage(
            model = imageRequest, // Используем запомненный ImageRequest
            contentDescription = stringResource(R.string.content_description_clear_button),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .zoomable()
        )
    }
}

@OptIn(ExperimentalEncodingApi::class)
private fun decodeImageData(imageData: String?): ByteArray? {
    return when {
        imageData != null -> {
            try {
                Base64.decode(imageData)
            } catch (e: Exception) {
                Log.e("ImageDecoder", "Failed to decode Base64: ${e.message}")
                null
            }
        }
        else -> null
    }
}

private fun createImageRequest(
    context: Context,
    imageData: ByteArray?
): ImageRequest {
    return ImageRequest.Builder(context)
        .data(imageData)
        .crossfade(true)
        .allowHardware(true)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .listener(
            onSuccess = { _, _ ->
                Log.d("FullScreenImagePreview", "Image loaded successfully")
            },
            onError = { _, throwable ->
                Log.e("FullScreenImagePreview", "Failed to load image: ${throwable.throwable.message}")
            }
        )
        .error(com.google.firebase.inappmessaging.display.R.drawable.image_placeholder)
        .placeholder(com.google.firebase.inappmessaging.display.R.drawable.image_placeholder)
        .build()
}

@Composable
private fun ImagePreviewControlPanel(
    colors: Color,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(8.dp)
    ) {
        ControlButton(
            iconRes = R.drawable.ic_back,
            contentDescriptionRes = R.string.content_description_back_button,
            colors = colors,
            onClick = onBackClick
        )

        ControlButton(
            iconRes = R.drawable.ic_arrow_download,
            contentDescriptionRes = R.string.firmware_title_downloading,
            colors = colors,
            onClick = onDownloadClick,
            modifier = Modifier.padding(top = 16.dp)
        )

        ControlButton(
            iconRes = R.drawable.ic_img_share_bulb,
            contentDescriptionRes = R.string.event_share_dialog_title,
            colors = colors,
            onClick = onShareClick,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


@Composable
private fun ControlButton(
    @DrawableRes iconRes: Int,
    @StringRes contentDescriptionRes: Int,
    colors: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = stringResource(contentDescriptionRes),
        colorFilter = ColorFilter.tint(colors),
        modifier = modifier
            .size(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { onClick() }
    )
}


@Composable
fun Modifier.zoomable(): Modifier {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    return this.then(
        Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 4f)

                    // Сброс offset при возврате к минимальному масштабу
                    if (newScale == 1f) {
                        offsetX = 0f
                        offsetY = 0f
                    } else {
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                    scale = newScale
                }
            }
    )
}

@Composable
fun BottomSheetDialog(
    onFileSelectClick: () -> Unit,
) {
    BottomSheetMenuItem(
        image = R.drawable.img_file,
        text = R.string.consultant_bottom_sheet_item_select_file,
        onItemClick = onFileSelectClick
    )
}

@Composable
private fun BottomSheetMenuItem(
    @DrawableRes image: Int,
    @StringRes text: Int,
    onItemClick: () -> Unit
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        Row(
            modifier = Modifier
                .clickable(onClick = onItemClick)
                .fillMaxWidth()
                .padding(dimens.consultantBottomSheetItemPadding)
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = null
            )
            HSpacerMedium()
            Text(text = stringResource(id = text))
        }
    }
}

