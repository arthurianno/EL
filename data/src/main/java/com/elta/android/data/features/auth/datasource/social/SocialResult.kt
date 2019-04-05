package com.elta.android.data.features.auth.datasource.social

import com.elta.android.domain.features.user.model.SocialNetworkType

sealed class SocialResult {
    data class Success(val network: SocialNetworkType, val token: String) : SocialResult()
    data class Error(val network: SocialNetworkType, val error: Any? = null) : SocialResult()
    object Cancel : SocialResult()
}