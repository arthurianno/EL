package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.pm

import com.elta.android.domain.features.diary.events.interactor.AddNewEventUseCase
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.user.interactor.DEFAULT_VALUE
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.interactor.decrement
import com.elta.android.domain.features.user.interactor.getHemoglobinLevel
import com.elta.android.domain.features.user.interactor.increment
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.dialogs.base.pm.BaseSettingsDialogPm
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.toEventDate
import java.util.Date
import javax.inject.Inject

class HemoglobinSettingsPm @Inject constructor(
    private val addNewEventUseCase: AddNewEventUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    services: ServiceFacade
) : BaseSettingsDialogPm(services) {

    val dateState = State("")
    val dateSelectedAction = Action<Date>()
    val dateSelectedState = State(Date())

    val hemoglobinValueState = State<String>()

    val minusAction = Action<Unit>()
    val plusAction = Action<Unit>()

    private val profileState = State<Profile>()
    private val inputValueState = State(DEFAULT_VALUE)
    private val loadScreeAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()
        observeChangeHemoglobin()
        observeChangeDate()

        // always enabled for hemoglobin
        actionButtonEnabledCommand.consumer.accept(true)

        profileState.observable
            .doOnNext { inputValueState.consumer.accept(it.getHemoglobinLevel()) }
            .subscribe()
            .untilDestroy()

        mainAction.observable
            .debounceAction()
            .skipWhileInProgress()
            .map(::createNewEventParams)
            .flatMapCompletable { params ->
                addNewEventUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete {
                        bus.event(Events.EventsChanged)
                        closeDialogCommand.consumer.accept(Unit)
                    }
                    .doOnError(::handleError)
            }
            .retry()
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

    private fun observeChangeHemoglobin() {
        minusAction.observable
            .subscribe {
                val original = inputValueState.value
                val new = decrement(original)
                inputValueState.consumer.accept(new)
            }
            .untilDestroy()

        plusAction.observable
            .subscribe {
                val original = inputValueState.value
                val new = increment(original)
                inputValueState.consumer.accept(new)
            }
            .untilDestroy()

        inputValueState.observable
            .subscribe { hemoglobinValueState.consumer.accept(NumberFormatter.format(it)) }
            .untilDestroy()
    }

    private fun observeChangeDate() {
        dateSelectedAction.observable
            .subscribe(dateSelectedState.consumer)
            .untilDestroy()

        dateSelectedState.observable
            .doOnNext { dateState.consumer.accept(it.toEventDate(resources)) }
            .subscribe()
            .untilDestroy()
    }

    private fun createNewEventParams(i: Unit): AddNewEventUseCase.Params =
        AddNewEventUseCase.Params(
            value = inputValueState.value,
            date = dateSelectedState.value,
            eventType = EventType.GLYCATEDHEMOGLOBIN
        )
}