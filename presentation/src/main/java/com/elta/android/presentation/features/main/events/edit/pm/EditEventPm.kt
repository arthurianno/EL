package com.elta.android.presentation.features.main.events.edit.pm

import com.elta.android.domain.features.diary.events.interactor.DeleteEventUseCase
import com.elta.android.domain.features.diary.events.interactor.GetEventByIdUseCase
import com.elta.android.domain.features.diary.events.interactor.UpdateEventUseCase
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.getValidator
import com.elta.android.domain.features.diary.events.model.isChanged
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.base.model.EventFormModel
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getFormInputText
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getPickerValues
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getSelectorOption
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getTag
import io.reactivex.rxkotlin.Observables
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class EditEventPm @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
    services: ServiceFacade
) : BaseEventPm(services) {

    val deleteEventAction = Action<Unit>()

    private val eventId = State<String>()
    private val event = State<Event>()
    private val loadScreenAction = Action<String>()
    private val isFormChangedState = State(false)
    private val eventFormHolderState = State(EventFormModel())

    fun setEventId(id: String) {
        eventId.consumer.accept(id)
    }

    override fun onCreate() {
        super.onCreate()
        mainActionTitleState.consumer.accept(resources.getString(R.string.event_form_save_updated_entry_title))
        observeEventChanges()
        observeSaveEventAction()
        loadEvent()
    }

    private fun loadEvent() {
        loadScreenAction.observable
            .map(::createGetEventUseCaseParams)
            .flatMapSingle {
                getEventByIdUseCase.execute(it)
                    .bindProgress()
                    .doOnSuccess(event.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        event.observable
            .doOnNext(::bindEvent)
            .subscribe()
            .untilDestroy()

        Observables.combineLatest(
            lifecycleObservable,
            eventId.observable
        )
            .filter { it.first == Lifecycle.CREATED }
            .map { it.second }
            .doOnNext { loadScreenAction.consumer.accept(it) }
            .subscribe()
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

    override fun handleBack(i: Unit) {
        when (isFormChangedState.value) {
            true -> confirmExitCommand.consumer.accept(Unit)
            else -> router.exit()
        }
    }

    private fun createGetEventUseCaseParams(id: String) =
        GetEventByIdUseCase.Params(id)

    private fun createEditEventParams(i: Unit): UpdateEventUseCase.Params {
        val form = eventFormHolderState.value
        return UpdateEventUseCase.Params(
            event.value.copy(value = form.value,
                kind = form.kind,
                name = form.name,
                duration = form.duration,
                additionTime = checkNotNull(form.date),
                tagId = form.tag?.id,
                tag = form.tag,
                activityType = form.activityType,
                insulinType = form.insulinType,
                note = form.note,
                type = checkNotNull(form.eventType)
            )
        )
    }

    private fun observeEventChanges() {
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
            .filter { isFormChangedState.value }
            .map(::isFormValid)
            .subscribe(mainActionVisibilityState.consumer)
            .untilDestroy()
    }

    private fun checkIsChanged(eventFormModel: EventFormModel) {
        isFormChangedState.consumer.accept(
            event.valueOrNull?.isChanged(
                value = eventFormModel.value,
                kind = eventFormModel.kind,
                name = eventFormModel.name,
                duration = eventFormModel.duration,
                date = eventFormModel.date,
                tagId = eventFormModel.tag?.id,
                insulin = eventFormModel.insulinType,
                activity = eventFormModel.activityType,
                note = eventFormModel.note
            ) ?: false
        )
    }

    private fun isFormValid(form: EventFormModel): Boolean {
        val validator = checkNotNull(form.eventType).getValidator()
        return validator.isValid(
            value = form.value,
            kind = form.kind,
            name = form.name,
            duration = form.duration,
            insulin = form.insulinType,
            date = form.date,
            note = form.note
        )
    }

    private fun observeSaveEventAction() {
        mainAction.observable
            .skipWhileInProgress()
            .map(::createEditEventParams)
            .flatMapCompletable { params ->
                updateEventUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete(::handleEventAdded)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun observeDeleteEventAction() {

    }

    private fun Date?.isDateChanged(other: Date): Boolean {
        return when {
            this == null -> false
            else -> this != other
        }
    }

    private fun handleEventAdded() {
        bus.event(Events.EventsChanged)
        router.exit()
    }
}