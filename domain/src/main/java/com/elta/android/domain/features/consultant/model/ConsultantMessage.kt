package com.elta.android.domain.features.consultant.model

import android.net.Uri
import com.elta.android.domain.common.model.FileType

data class ConsultantMessage(
    val id: String,
    val owner: MessageOwner,
    val text: String,
    val attachment: Attachment?,
    val time: Long,
    val sendStatus: WebimMessageSendStatus,
    val isRead: Boolean,
    val isEdited: Boolean,
    val canBeEdited: Boolean
) {
    data class Attachment(
        val name: String?,
        val fileType: FileType?,
        val thumbnail: String?,
        val uri: Uri?,
        val url: String?,
        val size: Long,
        val imageSize: ImageSize?
    ) {
        data class ImageSize(
            val height: Int,
            val width: Int
        )
    }
}
