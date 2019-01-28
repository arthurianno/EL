package com.elta.android.data.features.auth.datasource.social

import android.app.Activity
import android.content.Context
import com.elta.android.common.errors.SocialAuthError
import com.elta.android.data.features.auth.datasource.social.delegates.ActivityDelegate
import com.elta.android.data.features.auth.datasource.social.delegates.FbSocialDelegate
import com.elta.android.data.features.auth.datasource.social.delegates.OkSocialDelegate
import com.elta.android.data.features.auth.datasource.social.delegates.VkSocialDelegate
import com.elta.android.domain.features.auth.model.SocialNetwork
import io.reactivex.Observable

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
    when (this) {
        SocialNetwork.FB -> FbSocialDelegate(activity)
        SocialNetwork.VK -> VkSocialDelegate(activity)
        SocialNetwork.OK -> OkSocialDelegate(activity)
    }