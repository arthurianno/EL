package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class WebimSessionUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {

    fun create(webimUser: WebimUser) {
        repository.webimSessionCreate(webimUser)
    }

    fun onResume() {
        repository.webimResume()
    }

    fun onPause() {
        repository.webimPause()
    }

    fun onDestroy() {
        repository.webimDestroy()
    }

    fun startChat() {
        repository.startChat()
    }
}
