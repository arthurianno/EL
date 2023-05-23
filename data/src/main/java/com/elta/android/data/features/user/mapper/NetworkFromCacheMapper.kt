package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.NetworkCacheDto
import com.elta.android.data.features.user.dto.SocialNetworkDto
import com.elta.android.data.features.user.dto.SocialNetworkTypeNetworkEntity
import javax.inject.Inject

class NetworkFromCacheMapper @Inject constructor() : Mapper<NetworkCacheDto, SocialNetworkDto> {

    override fun mapFromObject(source: NetworkCacheDto): SocialNetworkDto =
        with(source) {
            SocialNetworkDto(
                type = SocialNetworkTypeNetworkEntity.valueOf(type),
                isLinked = isLinked
            )
        }
}
