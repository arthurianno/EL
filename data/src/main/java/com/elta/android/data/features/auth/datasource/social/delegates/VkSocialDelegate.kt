package com.elta.android.data.features.auth.datasource.social.delegates

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.elta.android.data.features.auth.datasource.social.SocialResult
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError

class VkSocialDelegate(activity: Activity) : SocialDelegate(activity) {

    override val network: SocialNetworkType = SocialNetworkType.VK

    private val vkCallback: VKCallback<VKAccessToken> = object : VKCallback<VKAccessToken> {
        override fun onResult(result: VKAccessToken) {
            sendResult(SocialResult.Success(network, result.accessToken))
        }

        override fun onError(error: VKError) {
            sendResult(SocialResult.Error(network, error))
        }
    }

    override fun onCreate(state: Bundle?) {
        val token = VKAccessToken.currentToken()
        if (token != null && !token.isExpired) {
            sendResult(SocialResult.Success(network, token.accessToken))
        } else {
            VKSdk.login(activity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        VKSdk.onActivityResult(requestCode, resultCode, data, vkCallback)
}
