package com.elta.android.data.features.consultant.datasource

import android.content.Context
import com.elta.android.common.di.qualifires.Webim
import com.elta.android.common.di.qualifires.WebimAnnotationType
import javax.inject.Inject

class WebimDataSource @Inject constructor(
    @Webim(WebimAnnotationType.Account) private val accountName: String,
    @Webim(WebimAnnotationType.Location) private val location: String,
    @Webim(WebimAnnotationType.PrivateKey) private val privateKey: String,
    private val context: Context
)
