package com.elta.android.data.features.auth.datasource

import com.elta.android.domain.features.auth.model.SocialNetwork

interface SocialNetworkTokenDataSource {

    fun getToken(network: SocialNetwork): String
}