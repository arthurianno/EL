package com.elta.android.data.features.auth.datasource

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.elta.android.domain.features.auth.model.SocialNetwork
import io.reactivex.Observable
import javax.inject.Inject

class SocialNetworkSdkTokenDataSource @Inject constructor(
    private val context: Context
) : SocialNetworkTokenDataSource {
    override fun getToken(network: SocialNetwork): Observable<String> =
        RxSocialActivity.newInstance(context, network).launchForResult()

    private fun Intent?.launchForResult(context: Activity?, requestCode: Int) {
        if (this != null && context != null && this.resolveActivity(context.packageManager) != null) {
            context.startActivityForResult(this, requestCode)
        }
    }
}