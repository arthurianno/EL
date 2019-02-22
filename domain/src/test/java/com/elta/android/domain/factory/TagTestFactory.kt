package com.elta.android.domain.factory

import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.diary.tags.model.TagImage
import java.util.Date
import java.util.UUID

object TagTestFactory {

    private val ids = arrayListOf<String>().apply {
        (0..10).forEach {
            add(UUID.randomUUID().toString())
        }
    }

    val nextId: String
        get() = ids.random()

    fun create(id: String? = null): Tag =
        Tag(
            id = id ?: nextId,
            name = "Test name",
            image = TagImage.BREAKFAST,
            isReadOnly = true,
            modificationTime = Date()
        )
}