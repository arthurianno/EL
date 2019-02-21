package com.elta.android.data.features.diary.tags.cache

import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.cache.IllegalDeleteConditionError
import com.elta.android.data.features.common.cache.IllegalGetConditionError
import com.elta.android.data.features.common.cache.doInUserExists
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto_
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.inValues
import io.objectbox.kotlin.query
import javax.inject.Inject

class DbTagsCache @Inject constructor(
    private val userHolder: UserHolder,
    boxStore: BoxStore
) : TagsCache {

    private val box = boxStore.boxFor<TagCachedDto>()

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
        userHolder.doInUserExists {
            val result = box.query {
                equal(TagCachedDto_.userId, it)
            }.findIds()
            box.removeByKeys(result.asList())
        }
    }

    private fun removeByIds(ids: List<Long>) {
        userHolder.doInUserExists {
            val result = box.query {
                equal(TagCachedDto_.userId, it)
                and()
                inValues(TagCachedDto_.id, ids.toLongArray())
            }.findIds()
            box.removeByKeys(result.asList())
        }
    }

    private fun getAll(): List<TagCachedDto> =
        userHolder.doInUserExists {
            box.query {
                equal(TagCachedDto_.userId, it)
            }.find()
        }
}