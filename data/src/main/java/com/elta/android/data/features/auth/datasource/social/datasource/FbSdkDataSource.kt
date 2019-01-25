package com.elta.android.data.features.auth.datasource.social.datasource

import android.content.Context
import com.elta.android.data.features.auth.datasource.social.SocialNetworkDataSource
import com.elta.android.data.features.auth.datasource.social.authAndGetToken
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.facebook.AccessToken
import com.facebook.Profile
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class FbSdkDataSource @Inject constructor(
    private val context: Context
) : SocialNetworkDataSource {

    override fun getToken(network: SocialNetwork): Observable<String> =
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
        }.onErrorResumeNext(network.authAndGetToken(context))

    override fun getSocialUser(network: SocialNetwork): Single<SocialUserDto> =
        Single.fromCallable {
            SocialUserDto(Profile.getCurrentProfile().firstName)
        }
}