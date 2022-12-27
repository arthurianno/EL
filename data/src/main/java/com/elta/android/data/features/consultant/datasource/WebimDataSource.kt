package com.elta.android.data.features.consultant.datasource

import android.annotation.SuppressLint
import android.content.Context
import com.elta.android.common.di.qualifires.Webim
import com.elta.android.common.di.qualifires.WebimAnnotationType
import com.elta.android.data.features.consultant.model.toWebimUser
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.user.repository.ProfileRepository
import javax.inject.Inject

@SuppressLint("CheckResult")
class WebimDataSource @Inject constructor(
    @Webim(WebimAnnotationType.Account) private val accountName: String,
    @Webim(WebimAnnotationType.Location) private val location: String,
    @Webim(WebimAnnotationType.PrivateKey) private val privateKey: String,
    private val context: Context,
    profileRepository: ProfileRepository
) {
    private var user: WebimUser? = null

    init {
        profileRepository.getProfile()
            .map { it.toWebimUser() }
            .subscribe(
                { user = it },
                { user = null }
            )
    }
}
