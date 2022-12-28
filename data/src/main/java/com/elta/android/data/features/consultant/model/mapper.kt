package com.elta.android.data.features.consultant.model // ktlint-disable filename

import com.elta.android.domain.features.consultant.model.WebimMessage
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimMessageType
import com.elta.android.domain.features.consultant.model.WebimOwner
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.user.model.Profile
import com.google.gson.JsonObject
import com.nullgr.core.security.crypto.toHexDecimalString
import ru.webim.android.sdk.Message
import ru.webim.android.sdk.Message.SendStatus
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HMAC_SHA1_ALGORITHM = "HmacSHA256"

internal fun Message.toDomain(): WebimMessage =
    WebimMessage(
        type = type.toDomainType(),
        owner = type.toDomainOwner(),
        content = text,
        time = time,
        sendStatus = sendStatus.toDomain(),
        isRead = isReadByOperator
    )

internal fun WebimUser.toJSonObject(key: String): JsonObject =
    JsonObject().apply {
        add(
            "fields",
            JsonObject().apply {
                addProperty("display_name", name)
                addProperty("id", id)
            }
        )
        addProperty("hash", this@toJSonObject.toString().hmacSha1Signature(key))
    }

internal fun Profile.toWebimUser(): WebimUser =
    WebimUser(
        id = "$firstName$secondName$email".hashCode().toString(),
        name = "$firstName $secondName"
    )

private fun SendStatus.toDomain(): WebimMessageSendStatus =
    when (this) {
        SendStatus.SENDING -> WebimMessageSendStatus.Sending
        SendStatus.SENT -> WebimMessageSendStatus.Sent
    }

private fun Message.Type.toDomainType(): WebimMessageType =
    when (this) {
        Message.Type.FILE_FROM_OPERATOR -> WebimMessageType.File
        Message.Type.FILE_FROM_VISITOR -> WebimMessageType.File
        else -> WebimMessageType.Text
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
