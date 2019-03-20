package com.elta.android.data.features.user.cache

import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.cache.IllegalDeleteConditionError
import com.elta.android.data.features.common.cache.IllegalGetConditionError
import com.elta.android.data.features.user.cache.dto.SettingsCacheDto
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbSettingsCache @Inject constructor(
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
        when (condition) {
            is CommonConditions.All -> box.removeAll()
            is CommonConditions.ByIds -> box.removeByKeys(condition.ids)
            else -> throw IllegalDeleteConditionError(condition)
        }
    }

    override fun get(condition: Condition): List<SettingsCacheDto> =
        when (condition) {
            is CommonConditions.All -> box.all
            else -> throw IllegalGetConditionError(condition)
        }

}