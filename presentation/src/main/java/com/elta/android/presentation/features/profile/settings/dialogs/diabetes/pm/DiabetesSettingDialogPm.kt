package com.elta.android.presentation.features.profile.settings.dialogs.diabetes.pm

import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Events
import com.elta.android.presentation.analytics.updateStableParam
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.dialogs.base.pm.BaseSettingsDialogPm
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import javax.inject.Inject

class DiabetesSettingDialogPm @Inject constructor(
    private val getProfileUseCase: GetUpdatedProfileUseCase,
    services: ServiceFacade
) : BaseSettingsDialogPm(services) {

    val diabetesTypeSelectedAction = action<Diabetes>()
    val diabetesState = state(Diabetes.values())
    val selectedDiabetesState = state<Diabetes>()

    private val profileState = state<Profile>()
    private val loadScreeAction = action<Unit>()

    override fun onCreate() {
        super.onCreate()
        diabetesTypeSelectedAction.observable
            .filter { it != selectedDiabetesState.valueOrNull }
            .doOnNext(selectedDiabetesState.consumer)
            .map { it != profileState.valueOrNull?.diabetes }
            .doOnNext { actionButtonEnabledCommand.consumer.accept(it) }
            .subscribe()
            .untilDestroy()

        profileState.observable
            .filter { it.diabetes != null }
            .map { it.diabetes }
            .doOnNext(diabetesTypeSelectedAction.consumer)
            .subscribe()
            .untilDestroy()

        mainAction.observable
            .debounceAction()
            .map(::updateProfile)
            .doOnNext { updateStableParam(profile = it) }
            .doOnNext { bus.event(Events.ProfileChanged(it)) }
            .doOnNext { closeDialogCommand.consumer.accept(Unit) }
            .subscribe()
            .untilDestroy()

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

    private fun updateProfile(i: Unit): Profile =
        profileState.value.copy(diabetes = selectedDiabetesState.value)
}
