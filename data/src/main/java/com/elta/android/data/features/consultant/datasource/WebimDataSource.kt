package com.elta.android.data.features.consultant.datasource

import android.content.Context
import com.elta.android.common.di.qualifires.Webim
import com.elta.android.common.di.qualifires.WebimAnnotationType
import com.elta.android.data.features.consultant.model.toWebimUser
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.user.repository.ProfileRepository
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class WebimDataSource @Inject constructor(
    @Webim(WebimAnnotationType.Account) private val accountName: String,
    @Webim(WebimAnnotationType.Location) private val location: String,
    @Webim(WebimAnnotationType.PrivateKey) private val privateKey: String,
    private val context: Context,
    private val profileRepository: ProfileRepository
) {
    private var user: WebimUser? = null
    private val disposableContainer = CompositeDisposable()

    init {
        disposableContainer.add(
            profileRepository.getProfile()
                .map { it.toWebimUser() }
                .subscribe(
                    { user = it },
                    { user = null }
                )
        )
    }

    fun destroy() {
        disposableContainer.dispose()
    }
}
