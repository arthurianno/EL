package com.elta.android.presentation.features.diary.main.pm

import com.elta.android.common.utils.isToday
import com.elta.android.domain.features.diary.events.interactor.GetEventsByDateUseCase
import com.elta.android.domain.features.diary.events.interactor.GetEventsWithInvalidTimeUseCase
import com.elta.android.domain.features.diary.home.model.EventsBlock
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.ExpandableListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.diary.main.DiaryEventsMapper
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.bindEmpty
import io.reactivex.Observable
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import org.threeten.bp.LocalDate
import javax.inject.Inject


class MainDiaryPm @Inject constructor(
    private val mapper: DiaryEventsMapper,
    private val getEventsByDateUseCase: GetEventsByDateUseCase,
    private val getEventsWithInvalidTimeUseCase: GetEventsWithInvalidTimeUseCase,
    services: ServiceFacade
) : ExpandableListPm(services) {

    private val monthNames: Array<String> by lazy {
        resources.getStringArray(R.array.month_names)
    }

    override val isEmptyScreen = false

    val datePickerDateState = state(LocalDate.now())
    val dateSelectedAction = action<LocalDate>()
    val selectDateInDialogAction = action<Unit>()
    val dateInDialogSelectedAction = action<LocalDate>()
    val showDatePickerDialogCommand = command<LocalDate>()
    val monthTitleState = state<String>()
    val todayButtonVisibilityState = state<Boolean>()
    val todayClickedAction = action<Unit>()
    val invalidTimeModeState = state(false)
    val toggleInvalidTimeModeAction = action<Unit>()

    private val loadScreenAction = action<Unit>()
    private val selectedDateState = state(LocalDate.now())

    override fun onCreate() {
        super.onCreate()
        observeDates()
        observeActions()
        observeEvents()

    }


    override fun onBind() {
        super.onBind()

        bus.clicks<Clicks.RecordClicked>()
            .map { it.item }
            .doOnNext(::navigateToEventScreen)
            .subscribe()
            .untilUnbind()
    }

    private fun observeEvents() {
        Observable.merge(
            selectedDateState.observable.map { Unit },
            invalidTimeModeState.observable.map { Unit },
            bus.events<Events.EventsChanged>().map { Unit },
            bus.events<Events.ProfileUpdated>().map { Unit }
        )
            .subscribe(loadScreenAction.consumer)
            .untilDestroy()
    }

    override fun onItemExpandCollapse(
        clickedItem: ListItem,
        allItems: List<ListItem>
    ): List<ListItem> {
        if (clickedItem !is RecordsGroupItem) return allItems
        var expanded = false
        return allItems.map {
            when {
                it is RecordsGroupItem && it.id == clickedItem.id -> {
                    expanded = !it.isExpanded
                    it.copy(isExpanded = expanded)
                }
                it is RecordItem && it.groupId == clickedItem.id -> {
                    it.copy(isVisible = expanded)
                }
                else -> {
                    it
                }
            }
        }
    }

    private fun observeActions() {
        loadScreenAction.observable
            .switchMap {
                val events = if (invalidTimeModeState.value) {
                    getEventsWithInvalidTimeUseCase.execute(Unit)
                } else {
                    getEventsByDateUseCase.execute(createUseCaseParams(selectedDateState.value))
                }
                events
                    .hideErrorContainer()
                    .bindProgress()
                    .bindEmpty(emptyControl.visibilityState.consumer)
                    .doOnNext(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun observeDates() {
        selectDateInDialogAction.observable
            .map { selectedDateState.value }
            .subscribe(showDatePickerDialogCommand.consumer)
            .untilDestroy()

        dateInDialogSelectedAction.observable
            .doOnNext(::passSelectedDate)
            .subscribe()
            .untilDestroy()

        dateSelectedAction.observable
            .doOnNext {
                datePickerDateState.consumer.accept(it)

            }
            .subscribe(::passSelectedDate)
            .untilDestroy()

        selectedDateState.observable
            .doOnNext { datePickerDateState.consumer.accept(it) }
            .map {
                todayButtonVisibilityState.consumer.accept(!it.isToday())
                "${monthNames[it.monthValue.minus(1)]} ${it.year}"
            }
            .subscribe(monthTitleState.consumer)
            .untilDestroy()

        toggleInvalidTimeModeAction.observable
            .map { !invalidTimeModeState.value }
            .subscribe(invalidTimeModeState.consumer)
            .untilDestroy()

        invalidTimeModeState.observable
            .subscribe { isInvalidTimeMode ->
                if (isInvalidTimeMode) {
                    monthTitleState.consumer.accept(resources.getString(R.string.diary_invalid_time_title))
                } else {
                    val date = selectedDateState.value
                    monthTitleState.consumer.accept("${monthNames[date.monthValue - 1]} ${date.year}")
                }
            }
            .untilDestroy()

        todayClickedAction.observable
            .map { LocalDate.now() }
            .doOnNext(::passSelectedDate)
            .subscribe()
            .untilDestroy()
    }

    private fun handleSuccess(blocks: List<EventsBlock>) {
        listItems.consumer.accept(
            blocks.flatMapIndexed { index, event ->
                mapper.mapFromObject((index == 0) to event)
            }
        )
    }

    private fun passSelectedDate(date: LocalDate) {
        if (invalidTimeModeState.value) invalidTimeModeState.consumer.accept(false)
        selectedDateState.consumer.accept(date)
    }

    private fun navigateToEventScreen(record: RecordItem) {
        router.startFlow(Screens.EditEventScreen(record.id as String, record.eventType))
    }

    private fun createUseCaseParams(date: LocalDate) =
        GetEventsByDateUseCase.Params(date.atStartOfDay())
}
