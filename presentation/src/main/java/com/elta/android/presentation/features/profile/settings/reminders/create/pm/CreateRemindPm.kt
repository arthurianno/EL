package com.elta.android.presentation.features.profile.settings.reminders.create.pm

import com.elta.android.common.errors.ReminderTimeInPastError
import com.elta.android.common.utils.isDateChanged
import com.elta.android.domain.features.reminder.interactor.AddNewReminderUseCase
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.reminders.base.model.ReminderFormModel
import com.elta.android.presentation.features.profile.settings.reminders.base.pm.BaseRemindPm
import com.elta.android.presentation.features.profile.settings.reminders.utils.RemindersManager
import com.elta.android.presentation.utils.toString
import com.elta.android.presentation.widgets.spinner.adapter.items.SpinnerItem
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.state
import org.threeten.bp.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

class CreateRemindPm @Inject constructor(
    private val addNewReminderUseCase: AddNewReminderUseCase,
    remindersManager: RemindersManager,
    services: ServiceFacade
) : BaseRemindPm(remindersManager, services) {

    private val isFormNotEmptyState = state(false)

    override fun onCreate() {
        super.onCreate()
        saveActionSubscribe()
        lifecycleSubscribe()
    }

    override fun handleBack(i: Unit) {
        if (isFormNotEmptyState.value) {
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
            .doOnNext(::checkIsEmpty)
            .map(::isFormValid)
            .subscribe(saveChangesEnableState.consumer)
            .untilDestroy()
    }

    private fun lifecycleSubscribe() {
        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .retry()
            .subscribe {
                createScheduleItems()
                setDefaultScheduler()
            }
            .untilDestroy()
    }

    private fun saveActionSubscribe() {
        saveReminderAction.observable
            .skipWhileInProgress()
            .map(::createAddReminderParams)
            .doOnError(::handleError)
            .flatMapSingle { params ->
                addNewReminderUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .trackEvent(AnalyticsEventType.REMINDER_ADD)
                    .doOnSuccess { remindersManager.addReminder(params.reminder) }
                    .map { Unit }
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun setDefaultScheduler() {
        selectedScheduleAction.consumer.accept(schedulesState.value.first())
        schedulesDefaultState.consumer.accept(schedulesState.value.first().type.toString(resources))
    }

    private fun checkIsEmpty(reminderModel: ReminderFormModel) {
        isFormNotEmptyState.consumer.accept(!reminderModel.inputValue.isNullOrEmpty())
    }

    private fun createAddReminderParams(i: Unit): AddNewReminderUseCase.Params {
        val form = reminderFormHolderState.value
        return if (form.date?.isAfter(ZonedDateTime.now()) == true) {
            AddNewReminderUseCase.Params(
                Reminder(
                    id = UUID.randomUUID().toString(),
                    title = checkNotNull(form.inputValue),
                    date = checkNotNull(form.date),
                    scheduleType = checkNotNull(form.schedule)
                )
            )
        } else {
            throw ReminderTimeInPastError()
        }
    }
}
