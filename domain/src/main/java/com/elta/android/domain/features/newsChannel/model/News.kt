package com.elta.android.domain.features.newsChannel.model

import org.threeten.bp.LocalDateTime
import java.util.UUID

data class News(
    val id: UUID,
    val title: String?,
    val content: String?,
    val createdDateTime: Long, // В миллисекундах
    val modifiedDateTime: Long?, // Может быть null
    val file: NewsFile?,
    val image: NewsImage?,
    val orderNumber: Long, // Новое поле
    val state: String // Новое поле: "Активен" или "Не активен"
)