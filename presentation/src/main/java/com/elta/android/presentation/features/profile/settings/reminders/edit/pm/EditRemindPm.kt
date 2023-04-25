package com.elta.android.presentation.features.profile.settings.reminders.edit.pm

import com.elta.android.common.errors.ReminderTimeInPastError
import com.elta.android.common.utils.isDateChanged
import com.elta.android.domain.features.reminder.interactor.DeleteReminderUseCase
import com.elta.android.domain.features.reminder.interactor.GetReminderByIdUseCase
import com.elta.android.domain.features.reminder.interactor.UpdateReminderUseCase
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.model.isChanged
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.profile.settings.reminders.base.model.ReminderFormModel
import com.elta.android.presentation.features.profile.settings.reminders.base.pm.BaseRemindPm
import com.elta.android.presentation.features.profile.settings.reminders.utils.RemindersManager
import com.elta.android.presentation.utils.toString
import com.elta.android.presentation.widgets.spinner.adapter.items.SpinnerItem
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class EditRemindPm @Inject constructor(
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val getReminderByIdUseCase: GetReminderByIdUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    remindersManager: RemindersManager,
    services: ServiceFacade
) : BaseRemindPm(remindersManager, services) {

    val deleteRemindAction = action<Unit>()
    val deleteRemindDialogControl = dialogControl<DialogData, DialogResult>()
    val defaultScheduleState = state<String>()

    private val reminderIdState = state<String>()
    private val reminderState = state<Reminder>()
    private val getReminderById = action<Unit>()
    private val isFormChangedState = state(false)

    private val deleteRemindDialogData: DialogData by lazy { Dialogs.EventDeleteReminder(resources) }

    override fun onCreate() {
        super.onCreate()
        loadReminder()
        deleteActionSubscribe()
        saveActionSubscribe()
        lifecycleSubsribe()
    }

    override fun handleBack(i: Unit) {
        if (isFormChangedState.value) {
            exitDialogAction.consumer.accept(Unit)
        } else {
            router.exit()
        }
    }

    override fun observeFormChanges() {
        Observables.combineLatest(
            formInput.text.observable,
            selectedDateState.observable,
            selectedScheduleAction.observable
        ) { inputValue, date, schedule ->
            reminderFormHolderState.value.apply {
                this.inputValue = inputValue
                this.isDateChanged = this.date.isDateChanged(date)
                this.date = date
                this.schedule = (schedule as SpinnerItem).type
            }
        }
            .doOnNext(::checkIsChanged)
            .map { isFormValid(it) && isFormChangedState.value }
            .subscribe(saveChangesEnableState.consumer)
            .untilDestroy()
    }

    fun setReminderId(id: String) {
        reminderIdState.consumer.accept(id)
    }

    private fun lifecycleSubsribe() {
        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .retry()
            .subscribe { createScheduleItems() }
            .untilDestroy()
    }

    private fun saveActionSubscribe() {
        saveReminderAction.observable
            .skipWhileInProgress()
            .map(::createUpdateReminderParams)
            .doOnError(::handleError)
            .flatMapSingle { params ->
                updateReminderUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .map { params.reminder }
                    .doOnSuccess { reminder ->
                        remindersManager.cancelReminder(reminder)
                        remindersManager.addReminder(reminder)
                    }
                    .map { Unit }
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun deleteActionSubscribe() {
        deleteRemindAction.observable
            .switchMapMaybe {
                deleteRemindDialogControl.showForResult(deleteRemindDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
            .map { reminderState.value }
            .map(::createDeleteReminderUseCaseParams)
            .flatMapSingle { params ->
                deleteReminderUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .map { params.reminder }
                    .doOnSuccess(remindersManager::cancelReminder)
                    .map { Unit }
                    .doOnSuccess(::handleDeleted)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun loadReminder() {
        getReminderById.observable
            .skipWhileInProgress()
            .map(::createGetReminderUseCaseParams)
            .flatMapSingle {
                getReminderByIdUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess(reminderState.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        reminderState.observable
            .take(1)
            .doOnNext(::bindReminder)
            .subscribe()
            .untilDestroy()

        reminderIdState.observable
            .map { Unit }
            .subscribe(getReminderById.consumer)
            .untilDestroy()
    }

    private fun checkIsChanged(form: ReminderFormModel) {
        val isChanged = reminderState.valueOrNull?.isChanged(
            title = form.inputValue,
            date = form.date,
            schedule = form.schedule
        ) ?: false
        isFormChangedState.consumer.accept(isChanged)
    }

    private fun createGetReminderUseCaseParams(i: Unit) =
        GetReminderByIdUseCase.Params(reminderIdState.value)

    private fun createDeleteReminderUseCaseParams(reminder: Reminder) =
        DeleteReminderUseCase.Params(reminder)

    private fun createUpdateReminderParams(i: Unit): UpdateReminderUseCase.Params {
        val form = reminderFormHolderState.value
        return if (form.date?.isAfter(ZonedDateTime.now()) == true) {
            UpdateReminderUseCase.Params(
                reminderState.value.copy(
                    title = checkNotNull(form.inputValue),
                    date = checkNotNull(form.date),
                    scheduleType = checkNotNull(form.schedule)
                )
            )
        } else {
            throw ReminderTimeInPastError()
        }
    }

    private fun bindReminder(reminder: Reminder) {
        formInput.text.consumer.accept(reminder.title)
        dateTimeSelectedAction.consumer.accept(reminder.date)
        defaultScheduleState.consumer.accept(reminder.scheduleType.toString(resources))
        selectedScheduleAction.consumer.accept(SpinnerItem(reminder.scheduleType))
    }

    private fun handleDeleted(i: Unit) {
        bus.event(Events.ReminderDeleted)
        router.exit()
    }
}
