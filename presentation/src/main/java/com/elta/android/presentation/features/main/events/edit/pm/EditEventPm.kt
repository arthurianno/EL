package com.elta.android.presentation.features.main.events.edit.pm

import com.elta.android.common.utils.isDateChanged
import com.elta.android.domain.features.calculator.interactor.CachedDishesUseCase
import com.elta.android.domain.features.calculator.interactor.CalculatorFragmentResultHandler
import com.elta.android.domain.features.diary.events.interactor.DeleteEventUseCase
import com.elta.android.domain.features.diary.events.interactor.GetEventByIdUseCase
import com.elta.android.domain.features.diary.events.interactor.UpdateEventUseCase
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.form.ActivityValidator.isValidDuration
import com.elta.android.domain.features.diary.events.model.isChanged
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.main.events.base.initializer.MEDICAMENT_MEASURE_SUFFIX
import com.elta.android.presentation.features.main.events.base.model.EventFormModel
import com.elta.android.presentation.features.main.events.base.model.MedicamentModel
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getFormAdditionalText
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getFormInputText
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getMedicament
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getPickerValues
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getSelectorOption
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getTag
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.toDoubleFormat
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import javax.inject.Inject

private const val DEFAULT_SCREEN = 1L

class EditEventPm @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
    private val cachedDishes: CachedDishesUseCase,
    calculatorFragmentResult: CalculatorFragmentResultHandler,
    services: ServiceFacade
) : BaseEventPm(services, calculatorFragmentResult, cachedDishes) {

    val deleteEventAction = action<Unit>()

    private val loadScreenAction = action<Unit>()
    private val eventIdState = state<String>()
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
        if (isFormChangedState.value) {
            exitDialogAction.consumer.accept(Unit)
        } else {
            router.exit()
        }
    }

    override fun observeEventChanges() {
        Observables.combineLatest(
            eventTypeState.observable,
            formPickerValue.observable,
            formInput.text.observable,
            additionalInput.text.observable,
            formSelector.option.observable,
            tagSelector.option.observable,
            selectedDateState.observable,
            noteInput.text.observable,
            dishes.observable
        ) { eventType, pickerValue, inputValue, additionalValue, variant, tag, date, note, dishes ->
            eventFormHolderState.value.apply {
                this.eventType = eventType
                this.pickerValue = pickerValue
                this.inputValue = inputValue.removeSuffix(MEDICAMENT_MEASURE_SUFFIX).toDoubleFormat()
                this.additionalValue = additionalValue
                this.tag = tag.meta as? Tag
                this.isDateChanged = this.date.isDateChanged(date)
                this.date = date
                this.noteValue = note
                this.meta = variant.meta
            }
        }
            .skip(DEFAULT_SCREEN)
            .doOnNext(::checkIsChanged)
            .map { isFormValid(it) && isFormChangedState.value }
            .subscribe(mainActionVisibilityState.consumer)
            .untilDestroy()
    }

    fun setEventIdState(id: String) {
        eventIdState.consumer.accept(id)
    }

    private fun loadEvent() {
        loadScreenAction.observable
            .map(::createGetEventUseCaseParams)
            .flatMapSingle {
                getEventByIdUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess { event ->
                        launch {
                            cachedDishes(event.dishes)
                        }
                        eventState.consumer.accept(event)
                    }
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
            .map { }
            .subscribe(loadScreenAction.consumer)
            .untilDestroy()
    }

    private fun bindEvent(event: EventV2) {
        event.getPickerValues()?.let { updateFormPickerValueCommand.consumer.accept(it) }
        event.getFormInputText()?.let { formInput.text.consumer.accept(it) }
        event.getFormAdditionalText()?.let { additionalInput.text.consumer.accept(it) }
        event.getSelectorOption(resources)?.let { formSelector.option.consumer.accept(it) }
        event.getTag(resources)?.let { tagSelector.option.consumer.accept(it) }
        event.getMedicament().let { medicament ->
            medicamentState.consumer.accept(
                MedicamentModel(
                    medicament = medicament,
                    fromEvent = true,
                )
            )
        }
        dateTimeSelectedAction.consumer.accept(event.additionTime)
        event.note?.let { noteInput.text.consumer.accept(it) }
        dishes.consumer.accept(event.dishes)
    }

    private fun createGetEventUseCaseParams(i: Unit) =
        GetEventByIdUseCase.Params(eventIdState.value)

    private fun createEditEventParams(i: Unit): UpdateEventUseCase.Params {
        val form = eventFormHolderState.value
        return UpdateEventUseCase.Params(
            eventState.value.copy(
                value = form.value,
                kind = form.kind,
                name = if (form.medicament == null
                    || form.medicament?.isOther == true
                    || form.eventType !is EventType.Medicaments
                ) form.name else null,
                duration = form.duration,
                additionTime = checkNotNull(form.date),
                tagId = form.tag?.id,
                tag = form.tag,
                activityType = form.activityType,
                insulinMedicament = form.insulinMedicament,
                medicament = form.medicament,
                tabletsNumber = form.tabletsNumber,
                note = form.note?.trim(),
                type = checkNotNull(form.eventType),
                dishes = dishes.value
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
            insulinMedicament = eventFormModel.insulinMedicament,
            dishes = dishes.valueOrNull,
            medicament = eventFormModel.medicament,
            tabletsNumber = eventFormModel.tabletsNumber,
            activity = eventFormModel.activityType,
            note = eventFormModel.note
        ) ?: false
        isFormChangedState.consumer.accept(isChanged)
    }

    private fun observeSaveEventAction() {
        mainAction.observable
            .skipWhileInProgress()
            .map(::createEditEventParams)
            .filter { isValidDuration(it.event.duration) || it.event.duration == null }
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

    private fun createDeleteEventUseCaseParams(event: EventV2) =
        DeleteEventUseCase.Params(event)
}
