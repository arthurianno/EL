package com.elta.android.presentation.features.main.events.create.pm

import com.elta.android.domain.features.diary.events.interactor.AddNewEventUseCase
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.base.model.EventFormModel
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class EventCreationPm @Inject constructor(
    private val addNewEventUseCase: AddNewEventUseCase,
    services: ServiceFacade
) : BaseEventPm(services) {

    private val isFormNotEmptyState = State(false)
    private val eventFormHolderState = State(EventFormModel())

    override fun onCreate() {
        super.onCreate()
        mainActionTitleState.consumer.accept(resources.getString(R.string.event_form_save_new_entry_title))
        observeSaveEventAction()
    }

    override fun handleBack(i: Unit) {
        when (isFormNotEmptyState.value) {
            true -> exitDialogAction.consumer.accept(Unit)
            else -> router.exit()
        }
    }

    override fun observeEventChanges() {
        Observables.combineLatest(
            eventTypeState.observable,
            formPickerValue.observable,
            formInput.text.observable,
            formSelector.option.observable,
            tagSelector.option.observable,
            selectedDateState.observable,
            noteInput.text.observable
        ) { eventType, pickerValue, inputValue, variant, tag, date, note ->
            eventFormHolderState.value.apply {
                this.eventType = eventType
                this.pickerValue = pickerValue
                this.inputValue = inputValue
                this.tag = tag.meta as? Tag
                this.isDateChanged = this.date.isDateChanged(date)
                this.date = date
                this.noteValue = note
                this.meta = variant.meta
            }
        }
            .doOnNext(::checkIsEmpty)
            .map(::isFormValid)
            .subscribe(mainActionVisibilityState.consumer)
            .untilDestroy()
    }

    private fun checkIsEmpty(eventFormModel: EventFormModel) {
        isFormNotEmptyState.consumer.accept(
            eventFormModel.pickerValue != ZERO_PICKER_VALUE ||
                !eventFormModel.inputValue.isNullOrEmpty() ||
                eventFormModel.meta != null ||
                eventFormModel.tag != null ||
                !eventFormModel.note.isNullOrEmpty() ||
                eventFormModel.isDateChanged
        )
    }

    private fun observeSaveEventAction() {
        mainAction.observable
            .skipWhileInProgress()
            .map(::createAddEventParams)
            .flatMapCompletable { params ->
                addNewEventUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun createAddEventParams(i: Unit): AddNewEventUseCase.Params {
        val form = eventFormHolderState.value
        return AddNewEventUseCase.Params(
            value = form.value,
            kind = form.kind,
            name = form.name,
            duration = form.duration,
            date = form.date,
            tag = form.tag,
            activity = form.activityType,
            insulin = form.insulinType,
            note = form.note,
            eventType = checkNotNull(form.eventType)
        )
    }

    companion object {
        private const val ZERO_PICKER_VALUE = 0.0
    }
}