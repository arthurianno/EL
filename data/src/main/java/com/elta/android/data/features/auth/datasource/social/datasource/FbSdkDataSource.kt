package com.elta.android.data.features.auth.datasource.social.datasource

import android.content.Context
import android.os.Bundle
import com.elta.android.data.features.auth.datasource.social.SocialNetworkDataSource
import com.elta.android.data.features.auth.datasource.social.authAndGetToken
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.facebook.AccessToken
import com.facebook.GraphRequest
import io.reactivex.Observable
import io.reactivex.Single
import org.json.JSONException

@Suppress("SwallowedException", "TooGenericExceptionCaught")
class FbSdkDataSource(private val context: Context) : SocialNetworkDataSource {

    override fun getToken(): Observable<String> =
        Observable.create<String> { emitter ->
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
        }.onErrorResumeNext(SocialNetworkType.FB.authAndGetToken(context))

    override fun getSocialUser(): Single<SocialUserDto> =
        Single.create<SocialUserDto> { emitter ->
            val request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), null)
            val params = Bundle().apply { putString("fields", "first_name") }
            request.parameters = params
            val name = try {
                val response = request.executeAndWait()
                val json = response.jsonObject
                val field = try {
                    json.getString("first_name")
                } catch (e: JSONException) {
                    ""
                }
                field
            } catch (e: Exception) {
                ""
            }
            if (!emitter.isDisposed) {
                emitter.onSuccess(SocialUserDto(name))
            }
        }
}