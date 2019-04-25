package com.elta.android.presentation.features.profile.settings.reminders.base.pm

import com.elta.android.domain.features.reminder.model.ScheduleType
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.formSelectorControl
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.features.profile.settings.reminders.base.model.ReminderFormModel
import com.elta.android.presentation.utils.toEventDate
import com.elta.android.presentation.utils.toEventTime
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import com.elta.android.presentation.widgets.spinner.adapter.items.SpinnerItem
import com.nullgr.core.adapter.items.ListItem
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import java.util.Date

abstract class BaseRemindPm constructor(
    services: ServiceFacade
) : BasePm(services) {

    val exitDialogControl = dialogControl<DialogData, DialogResult>()
    val formInput = inputControl()
    val dateSelector = formSelectorControl()
    val timeSelector = formSelectorControl()

    val showDatePickerDialog = Command<Date>(bufferSize = 1)
    val showTimePickerDialog = Command<Date>(bufferSize = 1)
    val saveReminderAction = Action<Unit>()
    val dateTimeSelectedAction = Action<Date>()
    val selectedScheduleAction = Action<ListItem>()
    val backHandleAction = Action<Unit>()
    val schedulesState = State<List<SpinnerItem>>()
    val schedulesDefaultState = State<String>()
    val saveChangesEnableState = State(false)

    protected val exitDialogAction = Action<Unit>()
    protected val selectedDateState = State(Date())
    protected val reminderFormHolderState = State(ReminderFormModel())

    private val exitDialogData: DialogData by lazy { Dialogs.EventExit(resources) }

    abstract fun handleBack(i: Unit)

    abstract fun observeFormChanges()

    override fun onCreate() {
        super.onCreate()
        bindHandleBack()
        bindDateSelectors()
        observeFormChanges()
    }

    protected fun handleSuccess(i: Unit) {
        hideKeyBoardCommand.consumer.accept(Unit)
        bus.event(Events.EventsChanged)
        router.exit()
    }

    protected fun Date?.isDateChanged(other: Date): Boolean {
        return when {
            this == null -> false
            else -> this != other
        }
    }

    protected fun isFormValid(reminderModel: ReminderFormModel) =
        !reminderModel.inputValue.isNullOrEmpty() &&
                checkNotNull(reminderModel.date).after(Date())

    protected fun createScheduleItems() {
        schedulesState.consumer.accept(
            listOf(
                SpinnerItem(ScheduleType.NONE),
                SpinnerItem(ScheduleType.DAY),
                SpinnerItem(ScheduleType.WEEK),
                SpinnerItem(ScheduleType.MONTH),
                SpinnerItem(ScheduleType.YEAR)
            )
        )
    }

    private fun bindHandleBack() {
        backHandleAction.observable
            .doOnNext(::handleBack)
            .subscribe()
            .untilDestroy()

        exitDialogAction.observable
            .switchMapMaybe {
                exitDialogControl.showForResult(exitDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
            .doOnNext { router.exit() }
            .subscribe()
            .untilDestroy()
    }

    private fun bindDateSelectors() {
        selectedDateState.observable
            .map { it.toEventTime(resources).toSimpleSelectorOption() }
            .subscribe(timeSelector.option.consumer)
            .untilDestroy()

        selectedDateState.observable
            .map { it.toEventDate(resources).toSimpleSelectorOption() }
            .subscribe(dateSelector.option.consumer)
            .untilDestroy()

        dateSelector.clickAction.observable
            .map { selectedDateState.value }
            .subscribe(showDatePickerDialog.consumer)
            .untilDestroy()

        timeSelector.clickAction.observable
            .map { selectedDateState.value }
            .subscribe(showTimePickerDialog.consumer)
            .untilDestroy()

        dateTimeSelectedAction.observable
            .subscribe(selectedDateState.consumer)
            .untilDestroy()
    }

    private fun String.toSimpleSelectorOption() = SelectorOption(this)

    enum class DialogResult {
        NEGATIVE, POSITIVE
    }
}