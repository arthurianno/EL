package com.elta.android.data.features.consultant.model // ktlint-disable filename

import com.elta.android.domain.common.getFileExtension
import com.elta.android.domain.common.model.FileType
import com.elta.android.domain.common.model.FileType.Companion.toFileType
import com.elta.android.domain.features.consultant.model.ChatState
import com.elta.android.domain.features.consultant.model.ConsultantChat
import com.elta.android.domain.features.consultant.model.ConsultantMessage
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.MessageOwner
import com.elta.android.domain.features.consultant.model.WebimUser
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nullgr.core.security.crypto.toHexDecimalString
import ru.webim.android.sdk.Message
import ru.webim.android.sdk.Message.Attachment
import ru.webim.android.sdk.Message.SendStatus
import ru.webim.android.sdk.MessageStream
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HMAC_SHA1_ALGORITHM = "HmacSHA256"

internal fun WebimChat.toDomain(): ConsultantChat =
    ConsultantChat(
        id = id,
        messages = messages.toDomain(),
        hasNewMessage = hasNewMessage
    )

internal fun List<Message>.toDomain(fromCache: Boolean = false): List<ConsultantMessage> =
    this.map { it.toDomain(fromCache) }

internal fun Message.toDomain(fromCache: Boolean = false): ConsultantMessage =
    ConsultantMessage(
        id = serverSideId.orEmpty(),
        attachment = attachment.toDomain(text),
        owner = type.toDomainOwner(),
        text = text,
        time = time,
        sendStatus = sendStatus.toDomain(),
        isRead = isReadByOperator,
        isEdited = isEdited,
        canBeEdited = !fromCache && canBeEdited()
    )

internal fun MessageStream.ChatState.toDomain(): ChatState =
    when (this) {
        MessageStream.ChatState.CHATTING -> ChatState.Chatting
        MessageStream.ChatState.CHATTING_WITH_ROBOT -> ChatState.Chatting
        MessageStream.ChatState.CLOSED_BY_OPERATOR -> ChatState.Close
        MessageStream.ChatState.CLOSED_BY_VISITOR -> ChatState.Close
        MessageStream.ChatState.DELETED -> ChatState.Close
        MessageStream.ChatState.INVITATION -> ChatState.Open
        MessageStream.ChatState.ROUTING -> ChatState.Open
        MessageStream.ChatState.NONE -> ChatState.Close
        MessageStream.ChatState.QUEUE -> ChatState.Open
        MessageStream.ChatState.UNKNOWN -> ChatState.Close
    }

internal fun WebimUser.toJsonObject(key: String): JsonObject =
    JsonParser.parseString(Gson().toJson(this.toAuth(key))).asJsonObject

private fun Attachment?.toDomain(text: String): ConsultantMessage.Attachment =
    ConsultantMessage.Attachment(
        name = this?.fileInfo?.fileName,
        fileType = getContentType(text),
        thumbnail = this?.fileInfo?.imageInfo?.thumbUrl,
        url = this?.fileInfo?.url,
        size = this?.fileInfo?.size ?: 0,
        imageSize = this?.fileInfo?.imageInfo.getImageSize(),
        uri = null
    )

private fun Message.ImageInfo?.getImageSize(): ConsultantMessage.Attachment.ImageSize? =
    this?.let {
        ConsultantMessage.Attachment.ImageSize(it.height, it.height)
    }

private fun Attachment?.getContentType(text: String) = run {
    val fileTypeByExtension = text.getFileExtension()?.let {
        runCatching { FileType.getByExtension(it) }.getOrNull()
    }
    this?.fileInfo?.contentType?.toFileType() ?: fileTypeByExtension
}

private fun WebimUser.toAuth(key: String): WebimUserAuthEntity =
    WebimUserAuthEntity(
        fields = WebimUserAuthEntity.Fields(displayName = name, id = id),
        hash = this.toString().hmacSha1Signature(key)
    )

private fun SendStatus.toDomain(): WebimMessageSendStatus =
    when (this) {
        SendStatus.SENDING -> WebimMessageSendStatus.Sending
        SendStatus.SENT -> WebimMessageSendStatus.Sent
//        SendStatus.FAILED -> WebimMessageSendStatus.Error(WEBIM_MESSAGE_SEND_STATUS_EXCEPTION)
    }

fun Message.Type.toDomainOwner(): MessageOwner =
    when (this) {
        Message.Type.ACTION_REQUEST -> MessageOwner.Operator
        Message.Type.CONTACT_REQUEST -> MessageOwner.Operator
        Message.Type.FILE_FROM_OPERATOR -> MessageOwner.Operator
        Message.Type.FILE_FROM_VISITOR -> MessageOwner.User
        Message.Type.INFO -> MessageOwner.Operator
        Message.Type.KEYBOARD -> MessageOwner.Operator
        Message.Type.KEYBOARD_RESPONSE -> MessageOwner.Operator
        Message.Type.OPERATOR -> MessageOwner.Operator
        Message.Type.OPERATOR_BUSY -> MessageOwner.Operator
        Message.Type.STICKER_VISITOR -> MessageOwner.User
        Message.Type.VISITOR -> MessageOwner.User
    }

private fun String.hmacSha1Signature(key: String): String {
    val singKey =
        SecretKeySpec(key.toByteArray(Charsets.US_ASCII), HMAC_SHA1_ALGORITHM)
    val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
    mac.init(singKey)
    return mac.doFinal(this.toByteArray(Charsets.UTF_8)).toHexDecimalString()
}
