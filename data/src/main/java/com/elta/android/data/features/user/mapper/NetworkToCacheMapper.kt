package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.NetworkCacheDto
import com.elta.android.data.features.user.dto.SocialNetworkDto
import javax.inject.Inject

class NetworkToCacheMapper @Inject constructor() : Mapper<SocialNetworkDto, NetworkCacheDto> {

    override fun mapFromObject(source: SocialNetworkDto): NetworkCacheDto =
        with(source) {
            NetworkCacheDto(
                id = type.name.hashCode().toLong(),
                type = type.name,
                isLinked = isLinked
            )
        }
}
