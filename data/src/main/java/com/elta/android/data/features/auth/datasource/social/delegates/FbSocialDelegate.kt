package com.elta.android.data.features.auth.datasource.social.delegates

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.elta.android.data.features.auth.datasource.social.SocialResult
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

class FbSocialDelegate(activity: Activity) : SocialDelegate(activity) {

    override val network: SocialNetworkType = SocialNetworkType.FB

    private val facebookCallbackManager = CallbackManager.Factory.create()

    private val fbCallback: FacebookCallback<LoginResult> = object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            sendResult(SocialResult.Success(network, result.accessToken.token))
        }

        override fun onCancel() {
            sendResult(SocialResult.Error(network))
        }

        override fun onError(error: FacebookException?) {
            sendResult(SocialResult.Error(network, error))
        }
    }

    override fun onCreate(state: Bundle?) {
        LoginManager.getInstance().registerCallback(facebookCallbackManager, fbCallback)
        val token = AccessToken.getCurrentAccessToken()
        if (token != null && !token.isExpired) {
            sendResult(SocialResult.Success(network, token.token))
        } else {
            LoginManager.getInstance().logInWithReadPermissions(activity, emptyList())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
}
