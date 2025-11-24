package com.elta.android.data.features.newsChannel.cache

import com.elta.android.domain.features.newsChannel.model.News
import com.elta.android.domain.features.newsChannel.model.NewsFile
import com.elta.android.domain.features.newsChannel.model.NewsImage
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import org.threeten.bp.LocalDateTime
import java.util.UUID

@Entity
data class NewsEntity(
    @Id var objectBoxId: Long = 0,
    @Convert(converter = UUIDConverter::class, dbType = String::class)
    val id: UUID,
    val title: String?,
    val content: String?,
    val createdDateTime: Long,
    val modifiedDateTime: Long?,
    val fileName: String?,
    val fileUrl: String?,
    val fileSize: Long,
    val imageData: String?,
    val orderNumber: Long, // Новое поле
    val state: String, // Новое поле
    val cachedDateTime: Long = System.currentTimeMillis()
) {
    fun toDomain(): News {
        return News(
            id = id,
            title = title,
            content = content,
            createdDateTime = createdDateTime,
            modifiedDateTime = modifiedDateTime,
            file = fileName?.let { NewsFile(name = it, url = fileUrl, size = fileSize) },
            image = imageData?.let { NewsImage(url = null, data = it) },
            orderNumber = orderNumber,
            state = state
        )
    }

    companion object {
        fun fromDomain(news: News): NewsEntity {
            return NewsEntity(
                id = news.id,
                title = news.title,
                content = news.content,
                createdDateTime = news.createdDateTime,
                modifiedDateTime = news.modifiedDateTime,
                fileName = news.file?.name,
                fileUrl = news.file?.url,
                fileSize = news.file?.size ?: 0,
                imageData = news.image?.data,
                orderNumber = news.orderNumber,
                state = news.state,
                cachedDateTime = System.currentTimeMillis()
            )
        }
    }
}

// Конвертер для LocalDateTime
class LocalDateTimeConverter : io.objectbox.converter.PropertyConverter<LocalDateTime?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): LocalDateTime? {
        return databaseValue?.let { LocalDateTime.parse(it) }
    }

    override fun convertToDatabaseValue(entityProperty: LocalDateTime?): String? {
        return entityProperty?.toString()
    }
}