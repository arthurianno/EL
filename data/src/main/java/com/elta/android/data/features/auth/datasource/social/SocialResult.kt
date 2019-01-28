package com.elta.android.data.features.auth.datasource.social

import com.elta.android.domain.features.auth.model.SocialNetwork

sealed class SocialResult {
    data class Success(val network: SocialNetwork, val token: String) : SocialResult()
    data class Error(val network: SocialNetwork, val error: Any? = null) : SocialResult()
    object Cancel : SocialResult()
}