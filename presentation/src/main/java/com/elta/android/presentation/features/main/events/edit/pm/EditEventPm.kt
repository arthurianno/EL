package com.elta.android.presentation.features.main.events.edit.pm

import com.elta.android.common.utils.isDateChanged
import com.elta.android.domain.features.diary.events.interactor.DeleteEventUseCase
import com.elta.android.domain.features.diary.events.interactor.GetEventByIdUseCase
import com.elta.android.domain.features.diary.events.interactor.UpdateEventUseCase
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.isChanged
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.main.events.base.model.EventFormModel
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getFormInputText
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getPickerValues
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getSelectorOption
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getTag
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import javax.inject.Inject

class EditEventPm @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
    services: ServiceFacade
) : BaseEventPm(services) {

    val deleteEventAction = action<Unit>()

    private val loadScreenAction = action<Unit>()
    private val eventIdState = state<String>()
    private val eventState = state<Event>()
    private val isFormChangedState = state(false)
    private val eventFormHolderState = state(EventFormModel())

    private val deleteDialogData: DialogData by lazy { Dialogs.EventDelete(resources) }

    override fun onCreate() {
        super.onCreate()
        mainActionTitleState.consumer.accept(resources.getString(R.string.event_form_save_updated_entry_title))
        observeSaveEventAction()
        observeDeleteEventAction()
        loadEvent()
    }

    override fun handleBack(i: Unit) {
        when (isFormChangedState.value) {
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
            .doOnNext(::checkIsChanged)
            .map { isFormValid(it) && isFormChangedState.value }
            .subscribe(mainActionVisibilityState.consumer)
            .untilDestroy()
    }

    fun setEventId(id: String) {
        eventIdState.consumer.accept(id)
    }

    private fun loadEvent() {
        loadScreenAction.observable
            .skipWhileInProgress()
            .map(::createGetEventUseCaseParams)
            .flatMapSingle {
                getEventByIdUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess(eventState.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        eventState.observable
            .take(1)
            .doOnNext(::bindEvent)
            .subscribe()
            .untilDestroy()

        eventIdState.observable
            .map { Unit }
            .subscribe(loadScreenAction.consumer)
            .untilDestroy()
    }

    private fun bindEvent(event: Event) {
        event.getPickerValues()?.let { updateFormPickerValueCommand.consumer.accept(it) }
        event.getFormInputText()?.let { formInput.text.consumer.accept(it) }
        event.getSelectorOption(resources)?.let { formSelector.option.consumer.accept(it) }
        event.getTag(resources)?.let { tagSelector.option.consumer.accept(it) }
        dateTimeSelectedAction.consumer.accept(event.additionTime)
        event.note?.let { noteInput.text.consumer.accept(it) }
    }

    private fun createGetEventUseCaseParams(i: Unit) =
        GetEventByIdUseCase.Params(eventIdState.value)

    private fun createEditEventParams(i: Unit): UpdateEventUseCase.Params {
        val form = eventFormHolderState.value
        return UpdateEventUseCase.Params(
            eventState.value.copy(
                value = form.value,
                kind = form.kind,
                name = form.name,
                duration = form.duration,
                additionTime = checkNotNull(form.date),
                tagId = form.tag?.id,
                tag = form.tag,
                activityType = form.activityType,
                insulinType = form.insulin?.type,
                note = form.note,
                type = checkNotNull(form.eventType)
            )
        )
    }

    private fun checkIsChanged(eventFormModel: EventFormModel) {
        val isChanged = eventState.valueOrNull?.isChanged(
            value = eventFormModel.value,
            kind = eventFormModel.kind,
            name = eventFormModel.name,
            duration = eventFormModel.duration,
            date = eventFormModel.date,
            tagId = eventFormModel.tag?.id,
            insulin = eventFormModel.insulin?.type,
            activity = eventFormModel.activityType,
            note = eventFormModel.note
        ) ?: false
        isFormChangedState.consumer.accept(isChanged)
    }

    private fun observeSaveEventAction() {
        mainAction.observable
            .skipWhileInProgress()
            .map(::createEditEventParams)
            .flatMapSingle { params ->
                updateEventUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .andThen(Single.just(false))
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun observeDeleteEventAction() {
        deleteEventAction.observable
            .switchMapMaybe {
                exitDialogControl.showForResult(deleteDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
            .map { eventState.value }
            .map(::createDeleteEventUseCaseParams)
            .flatMapSingle { params ->
                deleteEventUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .andThen(Single.just(false))
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun createDeleteEventUseCaseParams(event: Event) =
        DeleteEventUseCase.Params(event)
}
