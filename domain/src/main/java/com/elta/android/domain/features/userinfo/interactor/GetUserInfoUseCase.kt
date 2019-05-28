package com.elta.android.domain.features.userinfo.interactor

import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.applySchedulers
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val userInfoRepo: UserInfoRepository,
    private val schedulers: SchedulersFacade
) : SingleUseCase<UserInfo, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<UserInfo> =
        userInfoRepo.getUserInfo().applySchedulers(schedulers)
}