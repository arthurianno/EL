package com.elta.android.presentation.features.consultant.model // ktlint-disable filename

import android.net.Uri
import com.elta.android.common.utils.MILLIS_IN_SECOND
import com.elta.android.domain.features.consultant.model.ChatState
import com.elta.android.domain.features.consultant.model.ConnectionStatus
import com.elta.android.domain.features.consultant.model.ConsultantMessage
import com.elta.android.domain.features.consultant.model.ContentType
import com.elta.android.domain.features.consultant.model.ContentType.Companion.toContentType
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.domain.features.user.model.Profile
import com.nullgr.core.date.CommonFormats
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

internal fun List<ConsultantMessage>.toUi(): List<MessageUiEntity> =
    this.map {
            it.toUi()
        }

private fun ConsultantMessage.toUi(): MessageUiEntity {
    return MessageUiEntity(
        id = id,
        owner = owner,
        text = text,
        document = attachment?.let {
            DocumentUiEntity(
                fileName = it.name.orEmpty(),
                fileType = it.fileType?.toContentType().toUi(),
                url = it.url,
                size = it.size.toSizeString(),
                isPortrait = it.imageSize?.let { size -> size.height > size.width },
                cachingState = if (it.uri == null) CachingState.NotCached else CachingState.Cached,
                uri = it.uri
            )
        },
        timeSending = SimpleDateFormat(CommonFormats.FORMAT_TIME).format(Date(time)),
        dateSending = time.formatDate(),
        sendingStatus = sendStatus,
        type = attachment?.fileType?.toContentType().toUi(),
        isRead = isRead,
        isEdited = isEdited,
        canBeEdit = canBeEdited,
        cornerSequence = null,
        isDayChanged = false,
        audioState = null
    )
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
    uri: Uri? = null
): ChatUiEntity =
    this.copy(
        messages = messages.map { message ->
            message.takeIf { it.id == messageId }
                ?.copy(
                    document = message.document?.copy(
                        cachingState = cachingState,
                        uri = uri
                    )
                )
                ?: message
        }
    )

fun ChatUiEntity.reduceAudioState(
    messageId: String,
    isPlaying: Boolean? = null,
    trackPosition: Int? = null,
    duration: Int? = null
): ChatUiEntity {
    val messages = this.messages.map { message ->
        if (message.id == messageId) {
            message.copy(
                audioState = message.audioState?.let { audioState ->
                    audioState.copy(
                        isPlaying = isPlaying ?: audioState.isPlaying,
                        trackPosition = trackPosition ?: audioState.trackPosition,
                        duration = duration ?: audioState.duration
                    )
                }
            )
        } else message
    }
    return this.copy(messages = messages)
}

internal fun Profile.toWebimUser(): WebimUser =
    WebimUser(
        id = email.orEmpty(),
        name = "$firstName $secondName"
    )

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
