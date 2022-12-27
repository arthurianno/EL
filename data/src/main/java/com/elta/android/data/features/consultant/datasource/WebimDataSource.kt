package com.elta.android.data.features.consultant.datasource

import android.annotation.SuppressLint
import android.content.Context
import com.elta.android.common.di.qualifires.Webim
import com.elta.android.common.di.qualifires.WebimAnnotationType
import javax.inject.Inject

@SuppressLint("CheckResult")
class WebimDataSource @Inject constructor(
    @Webim(WebimAnnotationType.Account) private val accountName: String,
    @Webim(WebimAnnotationType.Location) private val location: String,
    @Webim(WebimAnnotationType.PrivateKey) private val privateKey: String,
    private val context: Context
)
