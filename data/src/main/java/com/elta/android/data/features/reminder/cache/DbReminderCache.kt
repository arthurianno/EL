package com.elta.android.data.features.reminder.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.reminder.cache.dto.ReminderCacheDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbReminderCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<ReminderCacheDto>(factory) {

    override val classToken: Class<ReminderCacheDto> = ReminderCacheDto::class.java
}