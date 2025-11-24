@file:OptIn(ExperimentalEncodingApi::class)

package com.elta.android.presentation.features.newsChannel.ui.components.content

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.features.consultant.model.CachingState
import com.elta.android.presentation.features.newsChannel.model.MessageUiEntity
import com.elta.android.presentation.theme.GetLocalProperties
import java.util.regex.Pattern
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
fun NewsMessageContent(
    message: MessageUiEntity,
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onDocumentIconClick: (MessageUiEntity) -> Unit = {},
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        val backgroundColor = colors.shadeBlack4
        val borderColor = colors.shadeBlack4

        val smallCornerRadius = 4.dp
        val defaultCornerRadius = 12.dp

        val shape = RoundedCornerShape(
            topStart = if (message.cornerSequence?.top == true) smallCornerRadius else defaultCornerRadius,
            bottomStart = if (message.cornerSequence?.bottom == true) smallCornerRadius else defaultCornerRadius,
            topEnd = if (message.cornerSequence?.top == true) smallCornerRadius else defaultCornerRadius,
            bottomEnd = if (message.cornerSequence?.bottom == true) smallCornerRadius else defaultCornerRadius
        )

        val modifier = Modifier
            .widthIn(max = 250.dp)
            .clip(shape = shape)
            .border(
                shape = shape,
                color = borderColor,
                width = dimens.borderWidth
            )
            .background(backgroundColor)

        ModifiedCard(
            message = message,
            modifier = modifier,
            onDocumentIconClick = onDocumentIconClick,
            onMessageClick = onMessageClick
        )
    }
}

@Composable
fun ClickableTextWithLinks(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit
) {
    val annotatedString by remember(text) { mutableStateOf(buildAnnotatedStringWithLinks(text)) }

    ClickableText(
        text = annotatedString,
        style = style.copy(color = color),
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { annotation ->
                onLinkClick(annotation.item)
            }
        }
    )
}

private fun buildAnnotatedStringWithLinks(text: String): AnnotatedString {
    val urlPattern = Pattern.compile(
        "(https?://[\\w\\-_./?=&%]+)",
        Pattern.CASE_INSENSITIVE
    )
    val matcher = urlPattern.matcher(text)
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            append(text.substring(lastIndex, start))
            withStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(text.substring(start, end))
                addStringAnnotation(
                    tag = "URL",
                    annotation = text.substring(start, end),
                    start = length - (end - start),
                    end = length
                )
            }
            lastIndex = end
        }
        append(text.substring(lastIndex))
    }
    return annotatedString
}


@OptIn(ExperimentalEncodingApi::class)
@Composable
private fun ModifiedCard(
    message: MessageUiEntity,
    modifier: Modifier = Modifier,
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onDocumentIconClick: (MessageUiEntity) -> Unit
) {
    GetLocalProperties { dimens, _, colors, shapes, styles ->
        val imageShape = RoundedCornerShape(
            topStart = if (message.cornerSequence?.top == true) 4.dp else 12.dp,
            topEnd = if (message.cornerSequence?.top == true) 4.dp else 12.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        val density = LocalDensity.current
        val maxWidthPx = with(density) { 250.dp.toPx() }
        val context = LocalContext.current

        val computedSize = remember(message.image?.width, message.image?.height) {
            message.image?.let { imageDoc ->
                if (imageDoc.width != null && imageDoc.height != null && imageDoc.width > 0) {
                    val aspectRatio = imageDoc.height.toFloat() / imageDoc.width
                    val widthDp = with(density) { maxWidthPx.toDp() }
                    val heightDp = with(density) { (maxWidthPx * aspectRatio).toDp() }
                    Pair(widthDp, heightDp)
                } else {
                    Pair(250.dp, 150.dp)
                }
            } ?: Pair(250.dp, 150.dp)
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            message.image?.let { imageDoc ->
                // Запоминаем декодированные данные на основе base64Data или url
                val imageData = remember(imageDoc.base64Data, imageDoc.url) {
                    when {
                        imageDoc.base64Data != null -> {
                            try {
                                Base64.decode(imageDoc.base64Data)
                            } catch (e: Exception) {
                                Log.e("ImageDebug", "Failed to decode Base64: ${e.message}")
                                null
                            }
                        }
                        imageDoc.url != null -> imageDoc.url
                        else -> null
                    }
                }

                val imageRequest = remember(imageData, imageDoc.url, imageDoc.base64Data) {
                    ImageRequest.Builder(context)
                        .data(imageData)
                        .crossfade(true)
                        .allowHardware(true)
                        .size(512, 512)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .listener(
                            onSuccess = { _, result ->
                                Log.d("ImageDebug", "Image loaded successfully: ${imageDoc.url ?: "Base64"}")
                            },
                            onError = { _, throwable ->
                                Log.e("ImageDebug", "Failed to load image: ${imageDoc.url ?: "Base64"}, Error: ${throwable.throwable.message}", throwable.throwable)
                            }
                        )
                        .build()
                }

                Log.d("ImageDebug", "Loading image data: $imageData, Width: ${imageDoc.width}, Height: ${imageDoc.height}")
                AsyncImage(
                    model = imageRequest,
                    contentScale = ContentScale.Fit,
                    contentDescription = null,
                    placeholder = ColorPainter(colors.shadeBlack3),
                    error = ColorPainter(colors.shadeBlack3),
                    modifier = Modifier
                        .width(computedSize.first)
                        .height(computedSize.second)
                        .clip(imageShape)
                        .clickable { onMessageClick(message) }
                )
            }
            message.document?.let { doc ->
                DocumentLabel(
                    message = message,
                    onDocumentDownloadClick = onDocumentIconClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.chatCardTextContentPadding)
                )
                VSpacerVerySmall()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(colors.white)
                    .padding(dimens.chatCardTextContentPadding)
            ) {
                SelectionContainer {
                    Column {
                        if (!message.title.isNullOrEmpty()) {
                            ClickableTextWithLinks(
                                text = message.title,
                                style = androidx.compose.ui.text.TextStyle(fontSize = 20.sp),
                                color = colors.blackBlue,
                                modifier = Modifier.fillMaxWidth(),
                                onLinkClick = { url ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        if (message.text?.isNotEmpty() == true) {
                            ClickableTextWithLinks(
                                text = message.text,
                                style = styles.body1,
                                color = colors.blackBlue,
                                modifier = Modifier.fillMaxWidth(),
                                onLinkClick = { url ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            VSpacerVerySmall()
                        }
                    }
                }

                TimeLabel(
                    message = message,
                    textColor = colors.shadeBlack1,
                    modifier = Modifier
                        .align(Alignment.End)
                        .defaultMinSize(minWidth = dimens.timeLabelWidth)
                )
            }
        }
    }
}
@Composable
private fun DocumentLabel(
    message: MessageUiEntity,
    onDocumentDownloadClick: (MessageUiEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val document = message.document ?: return
    val isUploading = document.size == null
    val maxFileNameLength = 20
    val fileName = document.fileName.takeIf { it.isNotEmpty() }?.let {
        if (it.length > maxFileNameLength) "${it.take(maxFileNameLength)}..." else it
    } ?: stringResource(id = R.string.consultant_document_name_placeholder)
    val sizeTextId = if (isUploading) R.string.consultant_uploading else R.string.consultant_mb

    GetLocalProperties { _, _, colors, _, styles ->
        Row(modifier = modifier) {
            DocumentIcon(
                message = message,
                onDocumentIconClick = onDocumentDownloadClick
            )
            HSpacerSmall()
            Column {
                Text(
                    text = fileName,
                    style = styles.title3,
                    color = colors.blackBlue,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = stringResource(
                        id = sizeTextId,
                        document.size?.toString() ?: "0"
                    ),
                    style = styles.caption1,
                    color = colors.gGreenB
                )
            }
        }
    }
}

@Composable
private fun DocumentIcon(
    message: MessageUiEntity,
    onDocumentIconClick: (MessageUiEntity) -> Unit
) {
    val document = message.document ?: return
    val isDownloading = document.cachingState == CachingState.Downloading
    val isUploading = document.size == null

    GetLocalProperties { dimens, _, colors, shapes, _ ->
        val (colorAlpha, clickableModifier) = if (isUploading) {
            0.7f to Modifier
        } else {
            1f to Modifier.clickable { onDocumentIconClick(message) }
        }
        Box(contentAlignment = Alignment.Center) {
            val iconId = when (document.cachingState) {
                CachingState.Cached -> R.drawable.ic_file
                CachingState.NotCached -> R.drawable.ic_arrow_download
                CachingState.Downloading -> R.drawable.ic_dialog_close
            }
            Image(
                painter = painterResource(id = iconId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colors.white),
                modifier = Modifier
                    .clip(shape = shapes.round)
                    .background(color = colors.gGreenB.copy(alpha = colorAlpha))
                    .then(clickableModifier)
                    .padding(dimens.smallDim)
            )
            AnimatedVisibility(visible = isDownloading || isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .progressSemantics()
                        .size(38.dp),
                    strokeWidth = dimens.smallestDim,
                    color = colors.white
                )
            }
        }
    }
}

@Composable
private fun TimeLabel(
    message: MessageUiEntity,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    GetLocalProperties { _, _, colors, _, styles ->
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            HSpacerSmall()
            Text(
                text = message.timeSending,
                color = textColor,
                style = styles.caption1
            )
        }
    }
}