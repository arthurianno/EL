package com.elta.android.data.features.auth.datasource.social

import android.app.Activity
import android.content.Context
import com.elta.android.common.errors.SocialAuthError
import com.elta.android.data.features.auth.datasource.social.delegates.ActivityDelegate
import com.elta.android.data.features.auth.datasource.social.delegates.FbSocialDelegate
import com.elta.android.data.features.auth.datasource.social.delegates.OkSocialDelegate
import com.elta.android.data.features.auth.datasource.social.delegates.VkSocialDelegate
import com.elta.android.domain.features.user.model.SocialNetworkType
import io.reactivex.Observable

fun SocialNetworkType.authAndGetToken(context: Context): Observable<String> =
    RxSocialActivity.launchForResult(context, this)
        .switchMap { result ->
            when (result) {
                is SocialResult.Success -> Observable.just(result.token)
                is SocialResult.Error -> Observable.error(SocialAuthError(result.error))
                else -> Observable.empty()
            }
        }

fun SocialNetworkType.getDelegate(activity: Activity): ActivityDelegate =
    when (this) {
        SocialNetworkType.FB -> FbSocialDelegate(activity)
        SocialNetworkType.VK -> VkSocialDelegate(activity)
        SocialNetworkType.OK -> OkSocialDelegate(activity)
    }