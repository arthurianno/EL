package com.elta.android.presentation.features.registration.main.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.presentation.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.SingletonRxBusProvider
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import org.json.JSONException
import org.json.JSONObject
import ru.ok.android.sdk.Odnoklassniki
import ru.ok.android.sdk.OkListener
import ru.ok.android.sdk.util.OkAuthType
import ru.ok.android.sdk.util.OkScope

class RxSocialActivity : AppCompatActivity() {

    private val facebookCallbackManager = CallbackManager.Factory.create()

    private val fbCallback: FacebookCallback<LoginResult> = object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            sendResult(Result.Success(SocialNetwork.FB, result.accessToken.token))
        }

        override fun onCancel() {
            sendResult(Result.Error(SocialNetwork.FB))
        }

        override fun onError(error: FacebookException?) {
            sendResult(Result.Error(SocialNetwork.FB, error))
        }
    }

    private val vkCallback: VKCallback<VKAccessToken> = object : VKCallback<VKAccessToken> {
        override fun onResult(result: VKAccessToken) {
            sendResult(Result.Success(SocialNetwork.VK, result.accessToken))
        }

        override fun onError(error: VKError) {
            sendResult(Result.Error(SocialNetwork.VK, error))
        }
    }

    private val okCallback = object : OkListener {
        override fun onSuccess(result: JSONObject) {
            try {
                sendResult(Result.Success(SocialNetwork.OK, result.getString("access_token")))
            } catch (e: JSONException) {
                sendResult(Result.Error(SocialNetwork.OK, e))
            }
        }

        override fun onError(error: String) {
            sendResult(Result.Error(SocialNetwork.OK, error))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val network = intent.extras[EXTRA_SOCIAL] as SocialNetwork

        Odnoklassniki.createInstance(this, getString(R.string.OK_APP_ID), getString(R.string.OK_APP_KEY))

        LoginManager.getInstance().registerCallback(facebookCallbackManager, fbCallback)

        when (network) {
            SocialNetwork.FB -> requestFb()
            SocialNetwork.VK -> requestVk()
            SocialNetwork.OK -> requestOk()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (facebookCallbackManager.onActivityResult(requestCode, resultCode, data)) {
            return
        }
        if (VKSdk.onActivityResult(requestCode, resultCode, data, vkCallback)) {
            return
        }
        if (Odnoklassniki.getInstance().isActivityRequestOAuth(requestCode)) {
            Odnoklassniki.getInstance().onAuthActivityResult(requestCode, resultCode, data, okCallback)
            return
        }
    }

    private fun requestFb() {
        val token = AccessToken.getCurrentAccessToken()
        if (token != null && !token.isExpired) {
            sendResult(Result.Success(SocialNetwork.FB, token.token))
        } else {
            LoginManager.getInstance().logInWithReadPermissions(this, emptyList())
        }
    }

    private fun requestVk() {
        val token = VKAccessToken.currentToken()
        if (token != null && !token.isExpired) {
            sendResult(Result.Success(SocialNetwork.VK, token.accessToken))
        } else {
            VKSdk.login(this)
        }
    }

    private fun requestOk() {
        Odnoklassniki.getInstance().checkValidTokens(object : OkListener {
            override fun onSuccess(json: JSONObject?) {
                sendResult(Result.Success(SocialNetwork.OK, json.toString()))
            }

            override fun onError(error: String?) {
                Odnoklassniki.getInstance().requestAuthorization(this@RxSocialActivity, getString(R.string.uri_redirect),
                    OkAuthType.ANY, OkScope.VALUABLE_ACCESS, OkScope.LONG_ACCESS_TOKEN)
            }
        })
    }

    private fun sendResult(result: RxSocialActivity.Result) {
        SingletonRxBusProvider.BUS.post(RxBus.Keys.SINGLE, result)
        finish()
        overridePendingTransition(0, 0)
    }

    companion object {
        private const val EXTRA_SOCIAL = "extra_social"

        fun newInstance(context: Context, network: SocialNetwork): Intent {
            return Intent(context, RxSocialActivity::class.java)
                .apply {
                    putExtra(EXTRA_SOCIAL, network)
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
        }
    }

    sealed class Result {
        data class Success(val network: SocialNetwork, val token: String) : Result()
        data class Error(val network: SocialNetwork, val error: Any? = null) : Result()
    }
}