package com.elta.android.data.features.auth.datasource.social.delegates

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.elta.android.data.R
import com.elta.android.data.features.auth.datasource.social.SocialResult
import com.elta.android.domain.features.user.model.SocialNetworkType
import org.json.JSONException
import org.json.JSONObject
import ru.ok.android.sdk.Odnoklassniki
import ru.ok.android.sdk.OkListener
import ru.ok.android.sdk.util.OkAuthType
import ru.ok.android.sdk.util.OkScope

@Suppress("UnnecessaryParentheses")
class OkSocialDelegate(activity: Activity) : SocialDelegate(activity) {

    override val network: SocialNetworkType = SocialNetworkType.OK

    private val okCallback = object : OkListener {
        override fun onSuccess(result: JSONObject) {
            try {
                sendResult(SocialResult.Success(network, (result["access_token"] as String)))
            } catch (e: JSONException) {
                sendResult(SocialResult.Error(network, e))
            }
        }

        override fun onError(error: String) {
            sendResult(SocialResult.Error(network, error))
        }
    }

    override fun onCreate(state: Bundle?) {
        Odnoklassniki.getInstance().checkValidTokens(object : OkListener {
            override fun onSuccess(json: JSONObject?) {
                sendResult(SocialResult.Success(network, json.toString()))
            }

            override fun onError(error: String?) {
                Odnoklassniki.getInstance().requestAuthorization(
                    activity, activity.getString(R.string.OK_REDIRECT_URL),
                    OkAuthType.ANY, OkScope.VALUABLE_ACCESS, OkScope.LONG_ACCESS_TOKEN
                )
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        if (Odnoklassniki.getInstance().isActivityRequestOAuth(requestCode)) {
            Odnoklassniki.getInstance().onAuthActivityResult(requestCode, resultCode, data, okCallback)
            true
        } else {
            false
        }
}
