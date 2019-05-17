package com.elta.android.data.features.sync.mappers

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto
import com.elta.android.data.features.sync.configuration.LocalSyncMapper
import com.elta.android.domain.features.user.model.Profile

class ProfileSyncMapper : LocalSyncMapper<Profile> {

    override fun mapToUpdate(entity: Profile): LocalSyncCachedDto =
        LocalSyncCachedDto(
            Profile::class.hashCode().toLong(),
            Profile::class.java.simpleName,
            StateDto.UPDATED,
            Profile::class.java.simpleName
        )

    override fun mapToCreate(entity: Profile): LocalSyncCachedDto {
        throw  UnsupportedOperationException("Create not available for ${Profile::class}")
    }

    override fun mapToDelete(entity: Profile): LocalSyncCachedDto {
        throw  UnsupportedOperationException("Delete not available for ${Profile::class}")
    }
}