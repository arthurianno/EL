package com.elta.android.presentation.features.profile.settings.reminders.base.pm

import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.reminder.model.ScheduleType
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.formSelectorControl
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.profile.settings.reminders.base.model.ReminderFormModel
import com.elta.android.presentation.features.profile.settings.reminders.utils.RemindersManager
import com.elta.android.presentation.utils.DATE_FORMAT_WITHOUT_ZERO
import com.elta.android.presentation.utils.toEventTime
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import com.elta.android.presentation.widgets.spinner.adapter.items.SpinnerItem
import com.nullgr.core.adapter.items.ListItem
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import org.threeten.bp.ZonedDateTime

abstract class BaseRemindPm constructor(
    protected val remindersManager: RemindersManager,
    services: ServiceFacade
) : BasePm(services) {

    val exitDialogControl = dialogControl<DialogData, DialogResult>()
    val formInput = inputControl()
    val dateSelector = formSelectorControl()
    val timeSelector = formSelectorControl()

    val showDatePickerDialog = command<ZonedDateTime>(bufferSize = 1)
    val showTimePickerDialog = command<ZonedDateTime>(bufferSize = 1)
    val saveReminderAction = action<Unit>()
    val dateTimeSelectedAction = action<ZonedDateTime>()
    val selectedScheduleAction = action<ListItem>()
    val backHandleAction = action<Unit>()
    val schedulesState = state<List<SpinnerItem>>()
    val schedulesDefaultState = state<String>()
    val saveChangesEnableState = state(false)

    protected val exitDialogAction = action<Unit>()
    protected val selectedDateState = state(ZonedDateTime.now())
    protected val reminderFormHolderState = state(ReminderFormModel())

    private val exitDialogData: DialogData by lazy { Dialogs.ExitAndLoseData(resources) }

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
        bus.event(Events.ReminderChanged)
        router.exit()
    }

    protected fun isFormValid(reminderModel: ReminderFormModel) =
        !reminderModel.inputValue.isNullOrEmpty() &&
            checkNotNull(reminderModel.date).isAfter(ZonedDateTime.now())

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
            .map { it.toStringWithFormat(DATE_FORMAT_WITHOUT_ZERO).toSimpleSelectorOption() }
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
}
