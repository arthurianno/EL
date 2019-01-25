package com.elta.android.data.features.auth.datasource.social

import android.app.Activity
import android.content.Context
import com.elta.android.common.errors.SocialAuthError
import com.elta.android.data.features.auth.datasource.social.delegates.ActivityDelegate
import com.elta.android.data.features.auth.datasource.social.delegates.FbSocialDelegate
import com.elta.android.data.features.auth.datasource.social.delegates.OkSocialDelegate
import com.elta.android.data.features.auth.datasource.social.delegates.VkSocialDelegate
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.facebook.AccessToken
import com.vk.sdk.VKAccessToken
import io.reactivex.Observable
import org.json.JSONObject
import ru.ok.android.sdk.Odnoklassniki
import ru.ok.android.sdk.OkListener

fun SocialNetwork.getToken(): Observable<String> = Observable.create { emitter ->
    when (this) {
        SocialNetwork.FB -> {
            val token = AccessToken.getCurrentAccessToken()
            if (token != null && !token.isExpired) {
                if (!emitter.isDisposed) {
                    emitter.onNext(token.token)
                }
            } else {
                if (!emitter.isDisposed) {
                    emitter.onError(RuntimeException())
                }
            }
        }
        SocialNetwork.VK -> {
            val token = VKAccessToken.currentToken()
            if (token != null && !token.isExpired) {
                if (!emitter.isDisposed) {
                    emitter.onNext(token.accessToken)
                }
            } else {
                if (!emitter.isDisposed) {
                    emitter.onError(RuntimeException())
                }
            }
        }

        SocialNetwork.OK -> {
            Odnoklassniki.getInstance().checkValidTokens(object : OkListener {
                override fun onSuccess(json: JSONObject?) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(json.toString())
                    }
                }

                override fun onError(error: String?) {
                    if (!emitter.isDisposed) {
                        emitter.onError(RuntimeException())
                    }
                }
            })
        }
    }
}

fun SocialNetwork.authAndGetToken(context: Context): Observable<String> =
    RxSocialActivity.launchForResult(context, this)
        .switchMap { result ->
            when (result) {
                is SocialResult.Success -> Observable.just(result.token)
                is SocialResult.Error -> Observable.error(SocialAuthError(result.error))
                else -> Observable.empty()
            }
        }

fun SocialNetwork.getDelegate(activity: Activity): ActivityDelegate =
    when(this) {
        SocialNetwork.FB -> FbSocialDelegate(activity)
        SocialNetwork.VK -> VkSocialDelegate(activity)
        SocialNetwork.OK -> OkSocialDelegate(activity)
    }