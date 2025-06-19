package com.elta.android.domain.features.remoteconfig.interactor

import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.remoteconfig.repository.RemoteConfigRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

class FetchRemoteConfigUseCase @Inject constructor(
    private val repository: RemoteConfigRepository,
    private val crashlyticsReport: CrashlyticsReport,
    schedulersFacade: SchedulersFacade
): CompletableUseCase<Unit>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Unit?): Completable {
        return rxCompletable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            val enabledConfig = repository.fetchRemoteConfig()
            crashlyticsReport.log("Remote config enabled: $enabledConfig")
        }
    }
}
