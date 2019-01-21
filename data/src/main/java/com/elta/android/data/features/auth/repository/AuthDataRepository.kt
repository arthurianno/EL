package com.elta.android.data.features.auth.repository

import com.elta.android.data.features.auth.datasource.AuthDataSource
import javax.inject.Inject

class AuthDataRepository @Inject constructor(
    private val source: AuthDataSource
)