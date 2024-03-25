package com.elta.android.domain.features.version.interactor

import com.elta.android.domain.features.FeatureToggles
import com.elta.android.domain.features.version.model.VersionStatus
import com.elta.android.domain.features.version.repository.VersionRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class CheckAppVersionUseCase @Inject constructor(
    private val repository: VersionRepository, schedulers: SchedulersFacade
) : SingleUseCase<VersionStatus, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<VersionStatus> {
        return if (FeatureToggles.isEnableForUntrackedBuild) Single.just(VersionStatus.NEEDLESS)
        else repository.checkAppVersion().onErrorReturn { VersionStatus.NEEDLESS }
    }
}
