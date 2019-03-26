package com.elta.android.data.features.diary.tags.cache

import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.cache.IllegalDeleteConditionError
import com.elta.android.data.features.common.cache.IllegalGetConditionError
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto_
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.inValues
import io.objectbox.kotlin.query
import javax.inject.Inject

class DbTagsCache @Inject constructor(
    private val factory: BoxStoreFactory
) : TagsCache {

    private val box: Box<TagCachedDto>
        get() = factory.getBoxStore().boxFor()

    override fun add(objects: List<TagCachedDto>) {
        box.put(objects)
    }

    override fun update(objects: List<TagCachedDto>) {
        box.put(objects)
    }

    override fun delete(condition: Condition) =
        when (condition) {
            is CommonConditions.All -> removeAll()
            is CommonConditions.ByIds -> removeByIds(condition.ids)
            else -> throw IllegalDeleteConditionError(condition)
        }

    override fun get(condition: Condition): List<TagCachedDto> =
        when (condition) {
            is CommonConditions.All -> getAll()
            else -> throw IllegalGetConditionError(condition)
        }

    private fun removeAll() {
        box.removeAll()
    }

    private fun removeByIds(ids: List<Long>) {
        val result = box.query {
            inValues(TagCachedDto_.id, ids.toLongArray())
        }.findIds()
        box.removeByKeys(result.asList())
    }

    private fun getAll(): List<TagCachedDto> = box.all
}