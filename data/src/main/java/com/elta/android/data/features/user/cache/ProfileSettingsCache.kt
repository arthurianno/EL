package com.elta.android.data.features.user.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.user.cache.dto.ProfileSettingsDbEntity
import javax.inject.Inject

class ProfileSettingsCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<ProfileSettingsDbEntity>(factory) {
    override val classToken: Class<ProfileSettingsDbEntity> = ProfileSettingsDbEntity::class.java
}
