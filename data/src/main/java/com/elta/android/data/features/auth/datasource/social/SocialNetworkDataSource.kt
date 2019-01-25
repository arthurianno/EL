package com.elta.android.data.features.auth.datasource.social

import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.domain.features.auth.model.SocialNetwork
import io.reactivex.Observable
import io.reactivex.Single

interface SocialNetworkDataSource {

    fun getToken(network: SocialNetwork): Observable<String>

    fun getSocialUser(network: SocialNetwork): Single<SocialUserDto>
}