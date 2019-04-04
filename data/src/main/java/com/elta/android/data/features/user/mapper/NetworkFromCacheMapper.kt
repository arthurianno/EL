package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.NetworkCacheDto
import com.elta.android.data.features.user.dto.SocialNetworkDto
import com.elta.android.data.features.user.dto.SocialNetworkTypeDto
import javax.inject.Inject

class NetworkFromCacheMapper @Inject constructor() : Mapper<NetworkCacheDto, SocialNetworkDto> {

    override fun mapFromObject(source: NetworkCacheDto): SocialNetworkDto =
        with(source) {
            SocialNetworkDto(
                type = SocialNetworkTypeDto.valueOf(type),
                isLinked = isLinked
            )
        }
}