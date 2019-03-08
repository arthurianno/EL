package com.elta.android.presentation.features.main.events.create.pm

import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.base.model.EventFormModel
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import io.reactivex.rxkotlin.Observables
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class EventCreationPm @Inject constructor(
    services: ServiceFacade
) : BaseEventPm(services) {

    private val isFormNotEmptyState = State(false)
    private val eventFormHolderState = State(EventFormModel())

    override fun onCreate() {
        super.onCreate()
        mainActionTitleState.consumer.accept(resources.getString(R.string.event_form_save_new_entry_title))
        observeFormChanges()
        observeSaveEventAction()
    }

    private fun observeFormChanges() {
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
                this.variantId = variant.meta as? String
                this.tagId = tag.meta as? String
                this.isDateChanged = this.date.isDateChanged(date)
                this.date = date
                this.note = note
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
                !eventFormModel.variantId.isNullOrEmpty() ||
                !eventFormModel.tagId.isNullOrEmpty() ||
                !eventFormModel.note.isNullOrEmpty() ||
                eventFormModel.isDateChanged
        )
    }

    private fun isFormValid(eventFormModel: EventFormModel): Boolean {
        return isFormNotEmptyState.value //TODO use domain methods
    }

    private fun observeSaveEventAction() {
        mainAction.observable
            .map { eventFormHolderState.value } // TODO and then map to UseCaseParams
            .doOnNext { Timber.d("save $it") } // TODO Execute UseCase
            .subscribe()
            .untilDestroy()
    }

    override fun handleBack(i: Unit) {
        when (isFormNotEmptyState.value) {
            true -> confirmExitCommand.consumer.accept(Unit)
            else -> router.exit()
        }
    }

    private fun Date?.isDateChanged(other: Date): Boolean {
        return when {
            this == null -> false
            else -> this != other
        }
    }

    companion object {
        private const val ZERO_PICKER_VALUE = 0.0
    }
}