package com.elta.android.data.features.user.cache

import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.cache.doInUserExists
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbProfileCache @Inject constructor(
    private val userHolder: UserHolder,
    boxStore: BoxStore
) : ProfileCache {
    private val box = boxStore.boxFor<ProfileCacheDto>()

    override fun add(objects: List<ProfileCacheDto>) {
        box.put(objects)
    }

    override fun update(objects: List<ProfileCacheDto>) {
        box.put(objects)
    }

    override fun delete(condition: Condition) {
        userHolder.doInUserExists {
            box.remove(it)
        }
    }

    override fun get(condition: Condition): List<ProfileCacheDto> =
        userHolder.doInUserExists {
            val result= box.get(it)
            if (result == null) emptyList() else listOf(result)
        }
}