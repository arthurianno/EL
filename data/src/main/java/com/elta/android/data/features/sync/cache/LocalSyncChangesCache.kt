package com.elta.android.data.features.sync.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto_
import io.objectbox.kotlin.query
import javax.inject.Inject

class LocalSyncChangesCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<LocalSyncCachedDto>(factory) {

    override val classToken: Class<LocalSyncCachedDto> = LocalSyncCachedDto::class.java

    override fun getAll(condition: Condition): List<LocalSyncCachedDto> =
        when (condition) {
            is LocalSyncChangesConditions.ByClassName -> getAllForClass(condition.className)
            is LocalSyncChangesConditions.ByClassNameAndId ->
                throw IllegalStateException("Condition ByClassNameAndId cannot be used for getAll method")
            else -> super.getAll(condition)
        }

    override fun get(condition: Condition): LocalSyncCachedDto? =
        when (condition) {
            is LocalSyncChangesConditions.ByClassName -> getForClass(condition.className)
            is LocalSyncChangesConditions.ByClassNameAndId -> getForClassAndId(condition.id, condition.className)
            else -> super.get(condition)
        }

    override fun delete(condition: Condition) {
        when (condition) {
            is LocalSyncChangesConditions.ByClassName -> deleteAllForClass(condition.className)
            is LocalSyncChangesConditions.ByClassNameAndId -> deleteForClassAndId(condition.id, condition.className)
            else -> super.delete(condition)
        }
    }

    override fun contains(condition: Condition): Boolean =
        when (condition) {
            is LocalSyncChangesConditions.ByClassNameAndId -> containsForClassAndId(condition.id, condition.className)
            else -> super.contains(condition)
        }

    private fun getAllForClass(className: String): List<LocalSyncCachedDto> = box.query {
        equal(LocalSyncCachedDto_.className, className)
    }.find()

    private fun getForClass(className: String): LocalSyncCachedDto? = box.query {
        equal(LocalSyncCachedDto_.className, className)
    }.findFirst()

    private fun deleteAllForClass(className: String) = box.query {
        equal(LocalSyncCachedDto_.className, className)
    }.remove()

    private fun getForClassAndId(id: String, className: String): LocalSyncCachedDto? = box.query {
        equal(LocalSyncCachedDto_.className, className)
        and()
        equal(LocalSyncCachedDto_.id, id)
    }.findFirst()

    private fun deleteForClassAndId(id: String, className: String) = box.query {
        equal(LocalSyncCachedDto_.className, className)
        and()
        equal(LocalSyncCachedDto_.id, id)
    }.remove()

    private fun containsForClassAndId(id: String, className: String) = box.query {
        equal(LocalSyncCachedDto_.className, className)
        and()
        equal(LocalSyncCachedDto_.id, id)
    }.count() > 0
}