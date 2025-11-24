package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import com.elta.android.domain.features.firebase.repository.MessagingTokenRepository
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class WebimSessionUseCase @Inject constructor(
    private val repository: ConsultantRepository,
    private val messagingTokenRepository: MessagingTokenRepository
) {

    suspend fun create(webimUser: WebimUser) {
        val firebaseToken = try {
            messagingTokenRepository.getToken()
                .await()
        } catch (e: Throwable) {
            null
        }
        repository.webimSessionCreate(webimUser, firebaseToken)
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
