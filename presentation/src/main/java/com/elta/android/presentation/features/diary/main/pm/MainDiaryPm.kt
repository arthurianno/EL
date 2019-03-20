package com.elta.android.presentation.features.diary.main.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.nullgr.core.date.isToday
import com.nullgr.core.date.toStringWithFormat
import java.util.Date
import javax.inject.Inject

class MainDiaryPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    val datePickerDateState = State(Date())
    val dateSelectedAction = Action<Date>()
    val selectDateInDialogAction = Action<Unit>()
    val dateInDialogSelectedAction = Action<Date>()
    val showDatePickerDialogCommand = Command<Date>()
    val monthTitleState = State<String>()
    val todayButtonVisibilityState = State<Boolean>()
    val todayClickedAction = Action<Unit>()

    private val selectedDateState = State(Date())

    override fun onCreate() {
        super.onCreate()

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
            .map { it.toStringWithFormat(FORMAT_MONTH_NAME_AND_YEAR) }
            .subscribe(monthTitleState.consumer)
            .untilDestroy()

        selectedDateState.observable
            .map { !it.isToday() }
            .subscribe(todayButtonVisibilityState.consumer)
            .untilDestroy()

        todayClickedAction.observable
            .map { Date() }
            .doOnNext(::passSelectedDate)
            .subscribe()
            .untilDestroy()
    }

    private fun passSelectedDate(date: Date) {
        datePickerDateState.consumer.accept(date)
        selectedDateState.consumer.accept(date)
    }

    companion object {
        const val FORMAT_MONTH_NAME_AND_YEAR = "LLL yyyy"
    }
}