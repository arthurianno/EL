package com.elta.android.presentation.features.profile.settings.reminders.create.pm

import com.elta.android.domain.features.reminder.interactor.AddNewReminderUseCase
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.reminders.base.model.ReminderFormModel
import com.elta.android.presentation.features.profile.settings.reminders.base.pm.BaseRemindPm
import com.elta.android.presentation.jobs.ReminderWorker
import com.elta.android.presentation.utils.toString
import com.elta.android.presentation.widgets.spinner.adapter.items.SpinnerItem
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class CreateRemindPm @Inject constructor(
    private val addNewReminderUseCase: AddNewReminderUseCase,
    services: ServiceFacade
) : BaseRemindPm(services) {

    private val isFormNotEmptyState = State(false)

    override fun onCreate() {
        super.onCreate()

        saveReminderAction.observable
            .skipWhileInProgress()
            .map(::createAddReminderParams)
            .flatMapSingle {
                addNewReminderUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess { id ->
                        ReminderWorker.startReminder(id)
                    }
                    .map { Unit }
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
            .subscribe {
                createScheduleItems()
                setDefaultScheduler()
            }
            .untilDestroy()
    }

    override fun handleBack(i: Unit) {
        when (isFormNotEmptyState.value) {
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
            .doOnNext(::checkIsEmpty)
            .map(::isFormValid)
            .subscribe(saveChangesEnableState.consumer)
            .untilDestroy()
    }

    private fun setDefaultScheduler() {
        selectedScheduleAction.consumer.accept(schedulesState.value.first())
        schedulesDefaultState.consumer.accept(schedulesState.value.first().type.toString(resources))
    }

    private fun checkIsEmpty(reminderModel: ReminderFormModel) {
        isFormNotEmptyState.consumer.accept(
            !reminderModel.inputValue.isNullOrEmpty()
        )
    }

    private fun createAddReminderParams(i: Unit): AddNewReminderUseCase.Params {
        val form = reminderFormHolderState.value
        return AddNewReminderUseCase.Params(
            title = checkNotNull(form.inputValue),
            date = checkNotNull(form.date),
            schedule = checkNotNull(form.schedule)
        )
    }
}