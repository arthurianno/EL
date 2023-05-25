package com.elta.android.domain.features.userinfo.interactor

import com.elta.android.domain.features.user.model.ProfileSettings
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetProfileSettingsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<ProfileSettings, GetProfileSettingsUseCase.Params>(schedulers) {
    override fun buildUseCaseObservable(params: Params?): Single<ProfileSettings> =
        profileRepository.getProfileSettings(params?.fromCache ?: Params().fromCache)

    data class Params(val fromCache: Boolean = true)
}
