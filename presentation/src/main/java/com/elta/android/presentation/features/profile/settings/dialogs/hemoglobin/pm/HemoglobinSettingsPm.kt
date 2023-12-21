package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.pm

import com.elta.android.domain.features.diary.events.interactor.AddNewEventUseCase
import com.elta.android.domain.features.diary.events.interactor.DeleteEventUseCase
import com.elta.android.domain.features.diary.events.interactor.GetGlycatedHemoglobinEventsUseCase
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.user.interactor.DEFAULT_VALUE
import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.domain.features.user.interactor.decrement
import com.elta.android.domain.features.user.interactor.getHemoglobinLevel
import com.elta.android.domain.features.user.interactor.increment
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.dialogs.base.pm.BaseSettingsDialogPm
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.mapper.HemoglobinItemsBuilder
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.toEventDate
import com.nullgr.core.adapter.items.ListItem
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class HemoglobinSettingsPm @Inject constructor(
    private val addNewEventUseCase: AddNewEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
    private val getProfileUseCase: GetUpdatedProfileUseCase,
    private val getHemoglobinEventsUseCase: GetGlycatedHemoglobinEventsUseCase,
    private val hemoglobinItemsBuilder: HemoglobinItemsBuilder,
    services: ServiceFacade
) : BaseSettingsDialogPm(services) {

    val dateState = state("")
    val dateSelectedAction = action<ZonedDateTime>()
    val dateSelectedState = state(ZonedDateTime.now())

    val hemoglobinValueState = state<String>()
    val hemoglobinItemsState = state<List<ListItem>>()

    val minusAction = action<Unit>()
    val plusAction = action<Unit>()

    private val profileState = state<Profile>()
    private val inputValueState = state(DEFAULT_VALUE)
    private val loadScreeAction = action<Unit>()
    private val hemoglobinEventsState = state<List<EventV2>>()

    override fun onCreate() {
        super.onCreate()
        // always enabled for hemoglobin
        actionButtonEnabledCommand.consumer.accept(true)

        observeDateSelection()
        observeValueChanges()
        observeMainAction()
        observeDeleteHemoglobinClicks()

        profileState.observable
            .doOnNext { inputValueState.consumer.accept(it.getHemoglobinLevel()) }
            .subscribe()
            .untilDestroy()

        loadScreeAction.observable
            .skipWhileInProgress()
            .flatMap { loadScreenData() }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(loadScreeAction.consumer)
            .untilDestroy()
    }

    private fun observeDeleteHemoglobinClicks() =
        bus.clicks<Clicks.DeleteHemoglobinEventClicked>()
            .skipWhileInProgress()
            .map { clickEvent -> hemoglobinEventsState.value.first { it.id == clickEvent.id } }
            .map { createDeleteEventParams(it) }
            .flatMap {
                deleteEventUseCase.execute(it)
                    .hideErrorContainer()
                    .andThen(
                        loadScreenData()
                            .doOnNext { bus.event(Events.EventsChanged(false)) }
                    )
                    .bindProgress()
                    .doOnError(::handleError)
            }
            .subscribe()
            .untilDestroy()

    private fun observeMainAction() =
        mainAction.observable
            .debounceAction()
            .skipWhileInProgress()
            .map(::createNewEventParams)
            .flatMapCompletable { params ->
                addNewEventUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .trackEvent(AnalyticsEventType.GLYCATED_HEMOGLOBIN_ADD)
                    .doOnComplete {
                        bus.event(Events.EventsChanged(true))
                        closeDialogCommand.consumer.accept(Unit)
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

    private fun handleSuccess(result: Pair<Profile, List<EventV2>>) {
        profileState.consumer.accept(result.first)
        hemoglobinItemsState.consumer.accept(hemoglobinItemsBuilder.buildItems(result.second))
        hemoglobinEventsState.consumer.accept(result.second)
    }

    private fun observeDateSelection() {
        dateSelectedAction.observable
            .subscribe(dateSelectedState.consumer)
            .untilDestroy()

        dateSelectedState.observable
            .doOnNext { dateState.consumer.accept(it.toEventDate(resources)) }
            .subscribe()
            .untilDestroy()
    }

    private fun observeValueChanges() {
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

    private fun loadScreenData() =
        Observables.zip(
            getProfileUseCase.execute(Unit).toObservable(),
            getHemoglobinEventsUseCase.execute(Unit)
        )
            .bindProgress()
            .doOnNext(::handleSuccess)
            .doOnError(::handleError)

    private fun createNewEventParams(i: Unit): AddNewEventUseCase.Params =
        AddNewEventUseCase.Params(
            value = inputValueState.value,
            date = dateSelectedState.value,
            eventType = EventType.Glycatedhemoglobin,
            glucometerSerialNumber = null
        )

    private fun createDeleteEventParams(event: EventV2): DeleteEventUseCase.Params =
        DeleteEventUseCase.Params(event)
}
