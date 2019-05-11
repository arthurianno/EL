package com.elta.android.presentation.features.profile.settings.reminders.edit.pm

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
import com.elta.android.presentation.features.profile.settings.reminders.base.model.ReminderFormModel
import com.elta.android.presentation.features.profile.settings.reminders.base.pm.BaseRemindPm
import com.elta.android.presentation.jobs.ReminderWorker
import com.elta.android.presentation.utils.toString
import com.elta.android.presentation.widgets.spinner.adapter.items.SpinnerItem
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class EditRemindPm @Inject constructor(
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val getReminderByIdUseCase: GetReminderByIdUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    services: ServiceFacade
) : BaseRemindPm(services) {

    val deleteRemindAction = Action<Unit>()
    val deleteRemindDialogControl = dialogControl<DialogData, DialogResult>()
    val defaultScheduleState = State<String>()

    private val reminderIdState = State<String>()
    private val reminderState = State<Reminder>()
    private val getReminderById = Action<Unit>()
    private val isFormChangedState = State(false)

    private val deleteRemindDialogData: DialogData by lazy { Dialogs.EventDeleteReminder(resources) }

    override fun onCreate() {
        super.onCreate()
        loadReminder()

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
                    .doOnSuccess { id ->
                        ReminderWorker.startReminder(id, true)
                    }
                    .map { Unit }
                    .doOnSuccess(::handleDeleted)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        saveReminderAction.observable
            .skipWhileInProgress()
            .map(::createUpdateReminderParams)
            .flatMapSingle { params ->
                updateReminderUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess { id ->
                        ReminderWorker.cancelReminder(id)
                        ReminderWorker.startReminder(id)
                    }
                    .map { false }
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .retry()
            .subscribe { createScheduleItems() }
            .untilDestroy()
    }

    override fun handleBack(i: Unit) {
        when (isFormChangedState.value) {
            true -> exitDialogAction.consumer.accept(Unit)
            else -> router.exit()
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
        return UpdateReminderUseCase.Params(
            reminderState.value.copy(
                title = checkNotNull(form.inputValue),
                time = checkNotNull(form.date),
                scheduleType = checkNotNull(form.schedule)
            )
        )
    }

    private fun bindReminder(reminder: Reminder) {
        formInput.text.consumer.accept(reminder.title)
        dateTimeSelectedAction.consumer.accept(reminder.time)
        defaultScheduleState.consumer.accept(reminder.scheduleType.toString(resources))
        selectedScheduleAction.consumer.accept(SpinnerItem(reminder.scheduleType))
    }

    private fun handleDeleted(i: Unit) {
        bus.event(Events.ReminderDeleted)
        router.exit()
    }
}