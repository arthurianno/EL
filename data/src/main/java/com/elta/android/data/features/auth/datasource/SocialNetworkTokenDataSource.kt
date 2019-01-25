package com.elta.android.data.features.auth.datasource

import com.elta.android.domain.features.auth.model.SocialNetwork
import io.reactivex.Observable

interface SocialNetworkTokenDataSource {

    fun getToken(network: SocialNetwork): Observable<String>
}