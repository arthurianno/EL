package com.elta.android.data.features.auth.datasource.social

import android.content.Context
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.domain.features.auth.model.SocialNetwork
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class SocialNetworkSdkDataSource @Inject constructor(
    private val context: Context
) : SocialNetworkDataSource {

    override fun getToken(network: SocialNetwork): Observable<String> =
        network.getToken().onErrorResumeNext(network.authAndGetToken(context))

    override fun getSocialUser(network: SocialNetwork): Single<SocialUserDto> =
        network.getSocialUser(network)

}