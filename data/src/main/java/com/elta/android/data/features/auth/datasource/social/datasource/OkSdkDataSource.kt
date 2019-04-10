package com.elta.android.data.features.auth.datasource.social.datasource

import android.content.Context
import com.elta.android.data.features.auth.datasource.social.SocialNetworkDataSource
import com.elta.android.data.features.auth.datasource.social.authAndGetToken
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.domain.features.user.model.SocialNetworkType
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.json.JSONObject
import ru.ok.android.sdk.Odnoklassniki
import ru.ok.android.sdk.OkListener
import ru.ok.android.sdk.OkRequestMode

@Suppress("UnnecessaryParentheses")
class OkSdkDataSource(private val context: Context) : SocialNetworkDataSource {

    private val ok: Odnoklassniki = Odnoklassniki.getInstance()

    override fun getToken(): Observable<String> =
        Observable.create<String> { emitter ->
            ok.checkValidTokens(object : OkListener {
                override fun onSuccess(json: JSONObject) {
                    if (!emitter.isDisposed) {
                        emitter.onNext((json["access_token"] as String))
                    }
                }

                override fun onError(error: String?) {
                    if (!emitter.isDisposed) {
                        emitter.onError(RuntimeException())
                    }
                }
            })
        }.onErrorResumeNext(SocialNetworkType.OK.authAndGetToken(context))

    override fun getSocialUser(): Single<SocialUserDto> = Single.create { emitter ->
        ok.requestAsync("users.getCurrentUser", null, OkRequestMode.DEFAULT, object : OkListener {
            override fun onSuccess(json: JSONObject) {
                if (!emitter.isDisposed) {
                    val name = json["first_name"] as String
                    emitter.onSuccess(SocialUserDto(name))
                }
            }

            override fun onError(error: String) {
                if (!emitter.isDisposed) {
                    emitter.onError(RuntimeException(error))
                }
            }
        })
    }

    override fun logout() = Completable.fromCallable { ok.clearTokens() }
}