package com.elta.android.data.features.consultant.model // ktlint-disable filename

import com.elta.android.domain.common.getFileExtension
import com.elta.android.domain.features.consultant.model.WebimContentType
import com.elta.android.domain.features.consultant.model.WebimMessage
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimOwner
import com.elta.android.domain.features.consultant.model.WebimUser
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nullgr.core.security.crypto.toHexDecimalString
import ru.webim.android.sdk.Message
import ru.webim.android.sdk.Message.Attachment
import ru.webim.android.sdk.Message.SendStatus
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HMAC_SHA1_ALGORITHM = "HmacSHA256"
private const val JPG_TYPE = "image/jpeg"
private const val PNG_TYPE = "image/png"
private const val HEIF_TYPE = "image/heif"
private const val PDF_TYPE = "application/pdf"
private const val VOICE_TYPE = "audio/x-hx-aac-adts"

internal fun Message.toDomain(): WebimMessage =
    WebimMessage(
        id = serverSideId.orEmpty(),
        attachment = attachment.toDomain(text),
        owner = type.toDomainOwner(),
        text = text,
        time = time,
        sendStatus = sendStatus.toDomain(),
        isRead = isReadByOperator
    )

internal fun WebimUser.toJsonObject(key: String): JsonObject =
    JsonParser.parseString(Gson().toJson(this.toAuth(key))).asJsonObject

private fun Attachment?.toDomain(text: String): WebimMessage.Attachment =
    WebimMessage.Attachment(
        contentType = run {
            val contentType = text.getFileExtension()?.let {
                runCatching { WebimContentType.getByExtension(it) }.getOrNull()
            }
            this?.fileInfo?.contentType?.convertContentType() ?: contentType
        },
        thumbnail = this?.fileInfo?.imageInfo?.thumbUrl,
        url = this?.fileInfo?.url,
        size = this?.fileInfo?.size ?: 0
    )

private fun String.convertContentType(): WebimContentType? =
    when (this) {
        JPG_TYPE -> WebimContentType.Jpg
        PNG_TYPE -> WebimContentType.Png
        PDF_TYPE -> WebimContentType.Pdf
        HEIF_TYPE -> WebimContentType.Heif
        VOICE_TYPE -> WebimContentType.Voice
        else -> null
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
    }

private fun Message.Type.toDomainOwner(): WebimOwner =
    when (this) {
        Message.Type.ACTION_REQUEST -> WebimOwner.Operator
        Message.Type.CONTACT_REQUEST -> WebimOwner.Operator
        Message.Type.FILE_FROM_OPERATOR -> WebimOwner.Operator
        Message.Type.FILE_FROM_VISITOR -> WebimOwner.User
        Message.Type.INFO -> WebimOwner.Operator
        Message.Type.KEYBOARD -> WebimOwner.Operator
        Message.Type.KEYBOARD_RESPONSE -> WebimOwner.Operator
        Message.Type.OPERATOR -> WebimOwner.Operator
        Message.Type.OPERATOR_BUSY -> WebimOwner.Operator
        Message.Type.STICKER_VISITOR -> WebimOwner.User
        Message.Type.VISITOR -> WebimOwner.User
    }

private fun String.hmacSha1Signature(key: String): String {
    val singKey =
        SecretKeySpec(key.toByteArray(Charsets.US_ASCII), HMAC_SHA1_ALGORITHM)
    val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
    mac.init(singKey)
    return mac.doFinal(this.toByteArray(Charsets.UTF_8)).toHexDecimalString()
}
