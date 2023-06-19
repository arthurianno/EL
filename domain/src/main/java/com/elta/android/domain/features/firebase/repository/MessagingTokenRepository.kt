package com.elta.android.domain.features.firebase.repository

import io.reactivex.Single

interface MessagingTokenRepository {

    fun getToken(): Single<String>

}