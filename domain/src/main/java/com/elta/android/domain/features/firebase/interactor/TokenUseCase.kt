package com.elta.android.domain.features.firebase.interactor

import com.elta.android.domain.features.firebase.repository.MessagingTokenRepository
import io.reactivex.Single
import javax.inject.Inject

class TokenUseCase @Inject constructor(private val messagingTokenRepository: MessagingTokenRepository) {

    operator fun invoke(): Single<String> =
        messagingTokenRepository.getToken()

}