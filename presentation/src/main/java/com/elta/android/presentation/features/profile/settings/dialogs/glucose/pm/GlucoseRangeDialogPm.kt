package com.elta.android.presentation.features.profile.settings.dialogs.glucose.pm

import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.dialogs.base.pm.BaseSettingsDialogPm
import javax.inject.Inject

class GlucoseRangeDialogPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    services: ServiceFacade
) : BaseSettingsDialogPm(services) {

    private val profileState = State<Profile>()
    private val loadScreeAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()
        loadScreeAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                getProfileUseCase.execute(Unit)
                    .bindProgress()
                    .doOnSuccess(profileState.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(loadScreeAction.consumer)
            .untilDestroy()
    }
}