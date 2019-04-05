package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.dto.SocialNetworkDto
import com.elta.android.domain.features.user.model.SocialNetwork
import com.elta.android.domain.features.user.model.SocialNetworkType
import javax.inject.Inject

class NetworkToDomainMapper @Inject constructor() : Mapper<SocialNetworkDto, SocialNetwork> {

    override fun mapFromObject(source: SocialNetworkDto): SocialNetwork =
        with(source) {
            SocialNetwork(
                type = SocialNetworkType.valueOf(type.name),
                isLinked = isLinked
            )
        }
}