package com.elta.android.presentation.features.profile.settings.dialogs.glucose.pm

import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.dialogs.base.pm.BaseSettingsDialogPm
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import javax.inject.Inject

class GlucoseRangeDialogPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    services: ServiceFacade
) : BaseSettingsDialogPm(services) {

    val beforeEatGlucoseRangeChangedAction = action<Pair<Double, Double>>()
    val afterEatGlucoseRangeChangedAction = action<Pair<Double, Double>>()
    val beforeEatGlucoseRangeState = state(DEFAULT_GLUCOSE_START to DEFAULT_GLUCOSE_END)
    val afterEatGlucoseRangeState = state(DEFAULT_GLUCOSE_START to DEFAULT_GLUCOSE_END)
    private val profileState = state<Profile>()
    private val loadScreeAction = action<Unit>()

    override fun onCreate() {
        super.onCreate()

        beforeEatGlucoseRangeChangedAction.observable
            .filter { it != beforeEatGlucoseRangeState.valueOrNull }
            .doOnNext(beforeEatGlucoseRangeState.consumer)
            .map { it.isRangeChanged() }
            .doOnNext { actionButtonEnabledCommand.consumer.accept(it) }
            .subscribe()
            .untilDestroy()

        afterEatGlucoseRangeChangedAction.observable
            .filter { it != afterEatGlucoseRangeState.valueOrNull }
            .doOnNext(afterEatGlucoseRangeState.consumer)
            .map { it.isRangeChanged() }
            .doOnNext { actionButtonEnabledCommand.consumer.accept(it) }
            .subscribe()
            .untilDestroy()

        profileState.observable
            .doOnNext { profile ->
                with(profile.glucoseLevelBeforeEatSettings.normal) {
                    beforeEatGlucoseRangeState.consumer.accept(start to end)
                }
                with(profile.glucoseLevelAfterEatSettings.normal) {
                    afterEatGlucoseRangeState.consumer.accept(start to end)
                }
            }
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
                    .map { it.copy(glucoseLevelBeforeEatSettings = it.glucoseLevelSettings) } // TODO После подключения сохранения профиля на бэке убрать.
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
                normalStart = beforeEatGlucoseRangeState.value.first,
                normalEnd = beforeEatGlucoseRangeState.value.second
            ),
            glucoseLevelBeforeEatSettings = GlucoseLevelSettings.fromNormalValues(
                normalStart = beforeEatGlucoseRangeState.value.first,
                normalEnd = beforeEatGlucoseRangeState.value.second
            ),
            glucoseLevelAfterEatSettings = GlucoseLevelSettings.fromNormalValues(
                normalStart = afterEatGlucoseRangeState.value.first,
                normalEnd = afterEatGlucoseRangeState.value.second
            )
        )

    companion object {
        private const val DEFAULT_GLUCOSE_START = 3.9
        private const val DEFAULT_GLUCOSE_END = 10.0
    }
}
