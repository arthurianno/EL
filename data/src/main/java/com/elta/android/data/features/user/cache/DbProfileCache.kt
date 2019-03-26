package com.elta.android.data.features.user.cache

import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbProfileCache @Inject constructor(
    private val factory: BoxStoreFactory
) : ProfileCache {

    private val box: Box<ProfileCacheDto>
        get() = factory.getBoxStore().boxFor()

    override fun add(objects: List<ProfileCacheDto>) {
        box.put(objects)
    }

    override fun update(objects: List<ProfileCacheDto>) {
        box.put(objects)
    }

    override fun delete(condition: Condition) {
        box.removeAll()
    }

    override fun get(condition: Condition): List<ProfileCacheDto> = box.all
}