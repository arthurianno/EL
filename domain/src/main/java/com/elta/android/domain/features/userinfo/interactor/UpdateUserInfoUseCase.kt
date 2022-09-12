package com.elta.android.domain.features.userinfo.interactor

import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class UpdateUserInfoUseCase @Inject constructor(
    private val repository: UserInfoRepository,
    schedulersFacade: SchedulersFacade
) : CompletableUseCase<UpdateUserInfoUseCase.Params>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Params?): Completable =
        repository.updateUserInfo(checkNotNull(params).info)

    data class Params(val info: UserInfo)
}
