package com.elta.android.presentation.features.profile.settings.dialogs.glucose.pm

import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.dialogs.base.pm.BaseSettingsDialogPm
import javax.inject.Inject

class GlucoseRangeDialogPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    services: ServiceFacade
) : BaseSettingsDialogPm(services) {

    val glucoseRangeChangedAction = Action<Pair<Double, Double>>()
    val glucoseRangeState = State(DEFAULT_GLUCOSE_START to DEFAULT_GLUCOSE_END)
    private val profileState = State<Profile>()
    private val loadScreeAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        glucoseRangeChangedAction.observable
            .filter { it != glucoseRangeState.valueOrNull }
            .doOnNext(glucoseRangeState.consumer)
            .map { it.isRangeChanged() }
            .doOnNext { actionButtonEnabledCommand.consumer.accept(it) }
            .subscribe()
            .untilDestroy()

        profileState.observable
            .filter { it.glucoseLevelSettings != null }
            .map { it.glucoseLevelSettings?.normal }
            .map { it.start to it.end }
            .doOnNext(glucoseRangeState.consumer)
            .subscribe()
            .untilDestroy()

        mainAction.observable
            .debounceAction()
            .map(::updateProfile)
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

    private fun Pair<Double, Double>.isRangeChanged(): Boolean {
        profileState.valueOrNull?.glucoseLevelSettings?.let {
            return this.first != it.normal.start || this.second != it.normal.end
        }
        return true
    }

    private fun updateProfile(i: Unit): Profile =
        profileState.value.copy(
            glucoseLevelSettings = GlucoseLevelSettings.fromNormalValues(
                normalStart = glucoseRangeState.value.first,
                normalEnd = glucoseRangeState.value.second
            )
        )

    companion object {
        private const val DEFAULT_GLUCOSE_START = 3.9
        private const val DEFAULT_GLUCOSE_END = 10.0
    }
}