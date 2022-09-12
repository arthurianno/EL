package com.elta.android.data.features.sync.configuration

import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto

interface LocalSyncMapper<T> {

    fun mapToUpdate(entity: T): LocalSyncCachedDto

    fun mapToCreate(entity: T): LocalSyncCachedDto

    fun mapToDelete(entity: T): LocalSyncCachedDto
}
