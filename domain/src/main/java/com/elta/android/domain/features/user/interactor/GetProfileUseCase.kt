package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.user.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dispatcher: CoroutineDispatcher
) {
    operator fun invoke() =
        profileRepository.getProfile()
            .toObservable()
            .asFlow()
            .flowOn(dispatcher)
}
