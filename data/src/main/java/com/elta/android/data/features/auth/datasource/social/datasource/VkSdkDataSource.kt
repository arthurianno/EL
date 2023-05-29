package com.elta.android.data.features.auth.datasource.social.datasource

import android.content.Context
import com.elta.android.data.features.auth.datasource.social.SocialNetworkDataSource
import com.elta.android.data.features.auth.datasource.social.authAndGetToken
import com.elta.android.data.features.auth.model.SocialUserDto
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKApi
import com.vk.sdk.api.VKError
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse
import com.vk.sdk.api.model.VKApiUserFull
import com.vk.sdk.api.model.VKList
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class VkSdkDataSource(private val context: Context) : SocialNetworkDataSource {

    override fun getToken(): Observable<String> =
        Observable.create<String> { emitter ->
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
        }.onErrorResumeNext(SocialNetworkType.VK.authAndGetToken(context))

    override fun getSocialUser(): Single<SocialUserDto> = Single.create { emitter ->
        val request = VKApi.users().get()
        request.parseModel = true
        request.executeWithListener(object : VKRequest.VKRequestListener() {
            override fun onComplete(response: VKResponse) {
                if (!emitter.isDisposed) {
                    val users = response.parsedModel as VKList<*>
                    val user = users.first() as VKApiUserFull
                    emitter.onSuccess(SocialUserDto(user.first_name))
                }
            }

            override fun onError(error: VKError) {
                if (!emitter.isDisposed) {
                    emitter.onError(RuntimeException(error.errorMessage))
                }
            }
        })
    }

    override fun logout() = Completable.fromCallable {
        if (VKSdk.isLoggedIn()) {
            VKSdk.logout()
        }
    }
}
