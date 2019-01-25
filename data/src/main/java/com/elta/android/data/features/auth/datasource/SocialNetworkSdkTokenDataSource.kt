package com.elta.android.data.features.auth.datasource

import com.elta.android.domain.features.auth.model.SocialNetwork
import com.facebook.AccessToken
import com.vk.sdk.VKAccessToken
import javax.inject.Inject

class SocialNetworkSdkTokenDataSource @Inject constructor() : SocialNetworkTokenDataSource {
    override fun getToken(network: SocialNetwork): String =
        when (network) {
            SocialNetwork.FB -> AccessToken.getCurrentAccessToken().token
            SocialNetwork.VK -> VKAccessToken.currentToken().accessToken
            SocialNetwork.OK -> Ok
        }
}