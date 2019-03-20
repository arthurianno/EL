package com.elta.android.data.features.user.cache

import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.cache.doInUserExists
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.user.cache.dto.SettingsCacheDto
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbSettingsCache @Inject constructor(
    private val userHolder: UserHolder,
    boxStore: BoxStore
) : SettingsCache {
    private val box = boxStore.boxFor<SettingsCacheDto>()

    override fun add(objects: List<SettingsCacheDto>) {
        box.put(objects)
    }

    override fun update(objects: List<SettingsCacheDto>) {
        box.put(objects)
    }

    override fun delete(condition: Condition) {
        userHolder.doInUserExists {
            box.remove(it)
        }
    }

    override fun get(condition: Condition): List<SettingsCacheDto> =
        userHolder.doInUserExists {
            listOf(box.get(it))
        }
}