package com.elta.android.data.features.auth.datasource.social.datasource

import com.elta.android.data.features.auth.datasource.social.SocialNetworkDataSource
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.vk.sdk.api.VKApi
import com.vk.sdk.api.model.VKApiOwner
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class VkSdkDataSource @Inject constructor(): SocialNetworkDataSource {

    override fun getToken(network: SocialNetwork): Observable<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSocialUser(network: SocialNetwork): Single<SocialUserDto> {
       VKApi.users().
    }
}