package com.elta.android.data.features.auth.datasource.social

import com.elta.android.data.features.auth.dto.SocialUserDto
import io.reactivex.Observable
import io.reactivex.Single

interface SocialNetworkDataSource {

    fun getToken(): Observable<String>

    fun getSocialUser(): Single<SocialUserDto>
}