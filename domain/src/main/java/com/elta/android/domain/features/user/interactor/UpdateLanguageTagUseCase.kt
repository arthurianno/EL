package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class UpdateLanguageTagUseCase @Inject constructor(
    private val repository: ProfileRepository,
    schedulersFacade: SchedulersFacade
) : CompletableUseCase<UpdateLanguageTagUseCase.Params>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Params?): Completable =
        repository.updateLanguageTag(checkNotNull(params).languageTag)

    data class Params(val languageTag: String)
}
