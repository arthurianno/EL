package com.elta.android.data.features.user.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto_
import io.objectbox.kotlin.query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbProfileCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<ProfileCacheDto>(factory) {

    override val classToken: Class<ProfileCacheDto> = ProfileCacheDto::class.java

    override fun contains(condition: Condition): Boolean =
        when (condition) {
            is CommonConditions.ById -> containsById(condition.id)
            else -> super.contains(condition)
        }

    private fun containsById(id: Long): Boolean = box.query {
        equal(ProfileCacheDto_.id, id)
    }.count() > 0
}