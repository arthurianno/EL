package com.elta.android.presentation.features.profile.settings.dialogs.diabetes.pm

import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class DiabetesSettingDialogPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val diabetesTypeSelectedAction = Action<Diabetes>()
    val diabetesState = State(Diabetes.values())
    val selectedDiabetesState = State<Diabetes>()
    val actionButtonEnabledCommand = State(false)
    val selectTypeAction = Action<Unit>()
    val closeDialogCommand = Command<Unit>(bufferSize = 1)

    private val profileState = State<Profile>()
    private val loadScreeAction = Action<Unit>()

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

        selectTypeAction.observable
            .map(::updateProfile)
            .doOnNext { bus.event(Events.ProfileChanged(it)) }
            .doOnNext { closeDialogCommand.consumer.accept(Unit) }
            .subscribe()
            .untilDestroy()

        loadScreeAction.observable
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