package com.elta.android.presentation.features.diary.main.pm

import com.elta.android.common.utils.isToday
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.diary.events.interactor.GetEventsByDateUseCase
import com.elta.android.domain.features.diary.home.model.EventsBlock
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.date.DateChangedEvent
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.diary.main.DiaryEventsMapper
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
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
    services: ServiceFacade
) : BaseListPm(services) {

    override val isEmptyScreen = false

    val datePickerDateState = state(LocalDate.now())
    val dateSelectedAction = action<LocalDate>()
    val selectDateInDialogAction = action<Unit>()
    val dateInDialogSelectedAction = action<LocalDate>()
    val showDatePickerDialogCommand = command<LocalDate>()
    val monthTitleState = state<String>()
    val todayButtonVisibilityState = state<Boolean>()
    val todayClickedAction = action<Unit>()

    private val loadScreenAction = action<LocalDate>()
    private val selectedDateState = state(LocalDate.now())

    override fun onCreate() {
        super.onCreate()

        observeDates()

        loadScreenAction.observable
            .map(::createUseCaseParams)
            .switchMap {
                getEventsByDateUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .bindEmpty(emptyControl.visibilityState.consumer)
                    .doOnNext(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observable.merge(
            selectedDateState.observable.map { Unit },
            bus.events<Events.EventsChanged>().map { Unit },
            bus.events<DateChangedEvent>().map { Unit }
        )
            .map { selectedDateState.value }
            .subscribe(loadScreenAction.consumer)
            .untilDestroy()
    }

    override fun onBind() {
        super.onBind()
        bus.clicks<Clicks.RecordClicked>()
            .map { it.item }
            .doOnNext(::navigateToEventScreen)
            .subscribe()
            .untilUnbind()
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
            .doOnNext(::passSelectedDate)
            .subscribe()
            .untilDestroy()

        selectedDateState.observable
            .map {
                todayButtonVisibilityState.consumer.accept(!it.isToday())
                it.toStringWithFormat(FORMAT_MONTH_NAME_AND_YEAR)
            }
            .subscribe(monthTitleState.consumer)
            .untilDestroy()

        todayClickedAction.observable
            .map { LocalDate.now() }
            .doOnNext(::passSelectedDate)
            .subscribe()
            .untilDestroy()
    }

    private fun handleSuccess(blocks: List<EventsBlock>) {
        items.consumer.accept(
            blocks.mapIndexed { index, event ->
                mapper.apply { expand = index == 0 }.mapFromObject(event)
            }
        )
    }

    private fun passSelectedDate(date: LocalDate) {
        datePickerDateState.consumer.accept(date)
        selectedDateState.consumer.accept(date)
    }

    private fun navigateToEventScreen(record: RecordItem) {
        router.startFlow(Screens.EditEventScreen(record.id as String, record.eventType))
    }

    private fun createUseCaseParams(date: LocalDate) =
        GetEventsByDateUseCase.Params(date.atStartOfDay())

    companion object {
        const val FORMAT_MONTH_NAME_AND_YEAR = "LLL yyyy"
    }
}
