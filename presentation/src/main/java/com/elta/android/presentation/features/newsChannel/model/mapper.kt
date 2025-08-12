package com.elta.android.presentation.features.newsChannel.model

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.elta.android.common.utils.MILLIS_IN_SECOND
import com.elta.android.domain.features.consultant.model.ChatState
import com.elta.android.domain.features.consultant.model.ConnectionStatus
import com.elta.android.domain.features.consultant.model.ContentType
import com.elta.android.domain.features.newsChannel.model.News
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.features.consultant.model.CachingState
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.DateUiEntity
import com.elta.android.presentation.features.consultant.model.DocumentUiEntity
import com.elta.android.presentation.features.consultant.model.MessageType
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.temporal.ChronoField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.io.encoding.Base64

private const val MB_SIZE = 1048576

fun ConnectionStatus.toUi(): ConnectState =
    when (this) {
        ConnectionStatus.Online -> ConnectState.Connect
        ConnectionStatus.Offline -> ConnectState.Offline
        ConnectionStatus.Connecting -> ConnectState.Connecting
    }

fun ChatState.toUi(): ConnectState =
    when (this) {
        ChatState.Open -> ConnectState.Connect
        ChatState.Close -> ConnectState.Offline
        ChatState.Chatting -> ConnectState.Connecting
    }

internal fun List<News>?.toUi(): List<MessageUiEntity> =
    this?.map { it.toUi() } ?: emptyList()

private fun News.toUi(): MessageUiEntity {
    // Извлекаем размеры изображения заранее, если есть Base64 данные
    val imageDimensions = image?.data?.let { base64Data ->
        extractImageDimensionsFromBase64(base64Data)
    }

    return MessageUiEntity(
        id = id,
        title = title,
        text = content,
        image = image?.let {
            DocumentUiEntity(
                fileName = "image_${id}.png",
                fileType = MessageType.Image,
                url = null,
                base64Data = it.data,
                size = null,
                isPortrait = imageDimensions?.let { (width, height) ->
                    height != null && width != null && height > width
                },
                cachingState = CachingState.NotCached,
                uri = null,
                width = imageDimensions?.first,    // Добавляем ширину
                height = imageDimensions?.second   // Добавляем высоту
            )
        },
        document = file?.let {
            DocumentUiEntity(
                fileName = it.name,
                fileType = MessageType.Document,
                url = it.url,
                base64Data = null,
                size = it.size.toSizeString(),
                isPortrait = null,
                cachingState = CachingState.NotCached,
                uri = null,
                width = null,
                height = null
            )
        },
        sendingStatus = MessageSendStatus.Sent,
        timeSending = Instant.ofEpochMilli(createdDateTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())),
        dateSending = createdDateTime.formatDate(),
        isDayChanged = false,
        cornerSequence = null
    )
}

private fun extractImageDimensionsFromBase64(base64Data: String?): Pair<Int?, Int?> {
    if (base64Data == null) return Pair(null, null)

    return try {
        val decodedBytes = Base64.decode(base64Data)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true // Только размеры, не загружаем изображение
        }
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

        if (options.outWidth > 0 && options.outHeight > 0) {
            Pair(options.outWidth, options.outHeight)
        } else {
            Pair(null, null)
        }
    } catch (e: Exception) {
        Log.e("NewsMapper", "Failed to extract image dimensions: ${e.message}")
        Pair(null, null)
    }
}

fun ContentType?.toUi(): MessageType {
    return when (this) {
        ContentType.Text -> MessageType.Text
        ContentType.Image -> MessageType.Image
        ContentType.Voice -> MessageType.Voice
        ContentType.DocumentPdf -> MessageType.Document
        ContentType.Video -> MessageType.Video
        else -> MessageType.Text
    }
}

fun ChatUiEntity.reduceChatState(
    messageId: String,
    cachingState: CachingState,
    uri: Uri? = null,
    size: Double? = null
): ChatUiEntity {
    val updatedMessages = messages.map { message ->
        if (message.id.toString() == messageId && message.document != null) {
            message.copy(
                document = message.document.copy(
                    cachingState = cachingState,
                    uri = uri ?: message.document.uri,
                    size = size // Сохраняем исходный размер, если новый не передан
                )
            )
        } else {
            message.copy()
        }
    }
    return copy(messages = updatedMessages.toList())
}

internal fun LocalTime.toUi(): String =
    format(
        DateTimeFormatterBuilder()
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter()
    )

internal fun Int?.toDuration(): String {
    val seconds = this?.div(MILLIS_IN_SECOND.toInt())
    val minutes = seconds?.div(60) ?: 0
    val remainsSeconds = seconds?.rem(60) ?: 0
    return String.format("%d:%02d", minutes, remainsSeconds)
}

private fun Long.toSizeString(): Double? =
    this.takeIf { it != 0L }?.toDouble()?.div(MB_SIZE)?.round(2)

fun Long.formatDate(): DateUiEntity {
    val messageDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val dateFormatterThisYear = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    val dateFormatterOtherYears = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    return when {
        messageDate.isEqual(today) -> DateUiEntity.Today(this)
        messageDate.isEqual(yesterday) -> DateUiEntity.Yesterday(this)
        messageDate.year == today.year -> DateUiEntity.ThisYear(
            timestampOfDate = this,
            date = messageDate.format(dateFormatterThisYear)
        )
        else -> DateUiEntity.Other(
            timestampOfDate = this,
            date = messageDate.format(dateFormatterOtherYears)
        )
    }
}