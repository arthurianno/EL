package com.elta.android.presentation.features.main.events.create.pm

import com.elta.android.common.utils.isDateChanged
import com.elta.android.domain.features.calculator.interactor.CachedDishesUseCase
import com.elta.android.domain.features.calculator.interactor.CalculatorFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.ClearCachedDishesUseCase
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.events.interactor.AddNewEventUseCase
import com.elta.android.domain.features.diary.events.interactor.GetLastInsulinEventUseCase
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.events.model.form.ActivityValidator.isValidDuration
import com.elta.android.domain.features.diary.events.model.toCapillaryGlucoseFormat
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.presentation.R
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.base.initializer.MEDICAMENT_MEASURE_SUFFIX
import com.elta.android.presentation.features.main.events.base.initializer.WeightFormInitializer
import com.elta.android.presentation.features.main.events.base.model.EventFormModel
import com.elta.android.presentation.features.main.events.base.model.MedicamentModel
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getFormAdditionalText
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getMedicament
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getSelectorOption
import com.elta.android.presentation.features.main.events.extensions.getLocalizedName
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.toDoubleFormat
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.state
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val MAIN_ACTON_BUTTON_DEBOUNCE = 100L
private val emptySelectorOption = SelectorOption(text = null)

class EventCreationPm @Inject constructor(
    private val addNewEventUseCase: AddNewEventUseCase,
    private val getProfileUseCase: GetUpdatedProfileUseCase,
    private val clearCachedDishes: ClearCachedDishesUseCase,
    private val getLastInsulinEventUseCase: GetLastInsulinEventUseCase,
    private val appMetricTracker: AppMetricTracker,
    cachedDishes: CachedDishesUseCase,
    calculatorFragmentResult: CalculatorFragmentResultHandler,
    services: ServiceFacade
) : BaseEventPm(services, calculatorFragmentResult, cachedDishes) {

    private val isFormNotEmptyState = state(false)
    private val eventFormHolderState = state(EventFormModel())
    private val selectorInsulinState = state(SelectorOption(text = null))

    override fun onCreate() {
        super.onCreate()

        getProfileAction.observable
            .flatMapSingle {
                getProfileUseCase.execute()
                    .bindProgress()
                    .hideErrorContainer()
                    .doOnSuccess(profileState.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { }
            .subscribe(getProfileAction.consumer)
            .untilDestroy()

        mainActionTitleState.consumer.accept(resources.getString(R.string.events_creation_save_button))
        observeSaveEventAction()
        loadLastEvent()

        launch {
            clearCachedDishes()
        }
    }

    override fun handleBack(i: Unit) {
        when (isFormNotEmptyState.value) {
            true -> exitDialogAction.consumer.accept(Unit)
            else -> router.exit()
        }
    }

    override fun observeEventChanges() {
        eventTypeState.observable.doOnEach {
            when (it.value) {
                is EventType.Bread -> appMetricTracker.trackEvent(AppMetricEvent.ViewScreenFood)
                is EventType.Activity -> appMetricTracker.trackEvent(AppMetricEvent.ViewScreenActivity)
                is EventType.Insulin -> appMetricTracker.trackEvent(AppMetricEvent.ViewScreenInsulin)
                is EventType.Medicaments -> appMetricTracker.trackEvent(AppMetricEvent.ViewScreenMedicines)
                is EventType.Weight -> appMetricTracker.trackEvent(AppMetricEvent.ViewScreenWeight)
                is EventType.Glucose -> appMetricTracker.trackEvent(AppMetricEvent.ViewScreenManualGlucose)
                else -> Unit
            }
        }
            .subscribe()
            .untilDestroy()

        Observables.combineLatest(
            eventTypeState.observable,
            formPickerValue.observable,
            formInput.text.observable,
            additionalInput.text.observable,
            formSelector.option.observable,
            tagSelector.option.observable,
            selectedDateState.observable,
            noteInput.text.observable,
            mealSelector.observable
        ) { eventType, pickerValue, inputValue, additionalValue, variant, tag, date, note, mealTag ->
            eventFormHolderState.value.apply {
                this.eventType = eventType
                this.pickerValue = pickerValue
                this.inputValue =
                    inputValue.removeSuffix(MEDICAMENT_MEASURE_SUFFIX).toDoubleFormat()
                this.additionalValue = additionalValue
                this.tag = tag.meta as? Tag
                this.isDateChanged = this.date.isDateChanged(date)
                this.date = date
                this.noteValue = note
                this.meta = variant.meta
                this.mealTag = mealTag.takeIf { eventTypeState.value is EventType.Glucose && it != MealTag.NOT_SELECTED }
            }
        }
            .debounce(MAIN_ACTON_BUTTON_DEBOUNCE, TimeUnit.MILLISECONDS)
            .doOnNext { checkIsEmpty(it, dishes.valueOrNull) }
            .map { isFormValid(it, profileState.valueOrNull?.glucoseFormat) }
            .subscribe(mainActionVisibilityState.consumer)
            .untilDestroy()
    }

    /**
     * Проверка отсуствия значения. Если значение пустое или равняется стандартному значению, то
     * значение будет true
     */
    private fun checkIsEmpty(eventFormModel: EventFormModel, dishes: List<Dish>?) {
        val isSpecialChanged = when (eventTypeState.valueOrNull) {
            EventType.Weight -> {
                eventFormModel.pickerValue != (
                        profileState.valueOrNull?.weight
                            ?: WeightFormInitializer.WEIGHT_DEFAULT_VALUE
                        ) ||
                        eventFormModel.meta != null
            }

            EventType.Insulin -> {
                eventFormModel.meta != selectorInsulinState.valueOrNull?.meta ||
                        eventFormModel.pickerValue != ZERO_PICKER_VALUE ||
                        !eventFormModel.additionalValue.isNullOrEmpty()
            }

            EventType.Medicaments -> {
                eventFormModel.meta != medicamentState.valueOrNull?.medicament ||
                        eventFormModel.additionalValue != medicamentState.valueOrNull?.otherName.orEmpty() ||
                        eventFormModel.inputValue != null
            }

            is EventType.Glucose -> {
                eventFormModel.mealTag != MealTag.NOT_SELECTED ||
                        eventFormModel.mealTag != null
                        eventFormModel.pickerValue != ZERO_PICKER_VALUE
            }

            else -> {
                eventFormModel.pickerValue != ZERO_PICKER_VALUE ||
                        eventFormModel.meta != null || !dishes.isNullOrEmpty() ||
                        eventFormModel.inputValue != null
            }
        }
        val isCommonChanged = eventFormModel.tag != null ||
                !eventFormModel.note.isNullOrEmpty() ||
                eventFormModel.isDateChanged
        isFormNotEmptyState.consumer.accept(isSpecialChanged || isCommonChanged)
    }

    private fun observeSaveEventAction() {
        mainAction.observable
            .skipWhileInProgress()
            .map(::createAddEventParams)
            .doOnEach {
                when (it.value?.eventType) {
                    is EventType.Bread -> appMetricTracker.trackEvent(AppMetricEvent.TapButtonFoodSave)
                    is EventType.Activity -> appMetricTracker.trackEvent(AppMetricEvent.TapButtonActivitySave)
                    is EventType.Insulin -> appMetricTracker.trackEvent(AppMetricEvent.TapButtonInsulinSave)
                    is EventType.Medicaments -> appMetricTracker.trackEvent(AppMetricEvent.TapButtonMedicinesSave)
                    is EventType.Weight -> appMetricTracker.trackEvent(AppMetricEvent.TapButtonWeightSave)
                    is EventType.Glucose -> appMetricTracker.trackEvent(AppMetricEvent.TapButtonManualGlucoseSave)
                    else -> Unit
                }
            }
            .filter { isValidDuration(it.duration) || it.duration == null }
            .flatMapSingle { params ->
                addNewEventUseCase.execute(params)
                    .hideErrorContainer()
                    .trackEvent { createCreationEvent(params) }
                    .andThen(Single.just(true))
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun loadLastEvent() {
        eventTypeState.observable
            .filter { it is EventType.Medicaments || it is EventType.Insulin }
            .flatMapSingle { type ->
                getLastInsulinEventUseCase.execute(GetLastInsulinEventUseCase.Params(type))
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess { event ->
                        when {
                            type is EventType.Insulin -> {
                                val option = event.getSelectorOption(resources)
                                formSelector.option.consumer.accept(option)
                                selectorInsulinState.consumer.accept(option)
                            }

                            type is EventType.Medicaments && event.medicament != null -> {
                                event.getFormAdditionalText()
                                    ?.let { additionalInput.text.consumer.accept(it) }
                                event.getSelectorOption(resources)
                                    ?.let { formSelector.option.consumer.accept(it) }
                                event.getMedicament()
                                    ?.let { medicament ->
                                        medicamentState.consumer.accept(
                                            MedicamentModel(
                                                medicament = medicament,
                                                fromEvent = false,
                                                otherName =
                                                if (medicament.isOther) event.getFormAdditionalText()
                                                else null
                                            )
                                        )
                                    }
                            }

                            else -> {}
                        }
                    }
                    .map { event -> event.getSelectorOption(resources) }
                    .onErrorReturn { emptySelectorOption }
                    .doOnError(::handleError)
            }
            .subscribe()
            .untilDestroy()
    }

    private fun createAddEventParams(i: Unit): AddNewEventUseCase.Params {
        val form = eventFormHolderState.value
        val pickerValue =
            if (eventTypeState.valueOrNull is EventType.Glucose)
                form.value?.toCapillaryGlucoseFormat(profileState.value.glucoseFormat)
            else form.value
        return AddNewEventUseCase.Params(
            value = pickerValue,
            kind = form.kind,
            name = form.name,
            duration = form.duration,
            date = form.date,
            tag = form.tag,
            activity = form.activityType,
            insulinMedicament = form.insulinMedicament,
            medicament = form.medicament,
            tabletsNumber = form.tabletsNumber,
            note = form.note,
            eventType = checkNotNull(form.eventType),
            glucometerSerialNumber = null,
            dishes = dishes.value,
            glucoseInputType = GlucoseInputType.MANUAL.takeIf { eventTypeState.value is EventType.Glucose },
            mealTag = form.mealTag.takeIf { eventTypeState.value is EventType.Glucose }
        )
    }

    private fun createCreationEvent(params: AddNewEventUseCase.Params): AnalyticsEvent? {
        val data = hashMapOf<String, String>()
        val name = when (params.eventType) {
            is EventType.Bread -> AnalyticsEventType.EVENT_BREAD_ADD
            EventType.Weight -> AnalyticsEventType.EVENT_WEIGHT_ADD
            EventType.Medicaments -> AnalyticsEventType.EVENT_MEDICAMENTS_ADD
            EventType.Activity -> {
                params.activity?.let { data[AnalyticsEventParam.TYPE] = it.name }
                AnalyticsEventType.EVENT_ACTIVITY_ADD
            }

            EventType.Insulin -> {
                data[AnalyticsEventParam.TYPE] =
                    checkNotNull(params.insulinMedicament).insulinType.getLocalizedName(resources)
                AnalyticsEventType.EVENT_INSULIN_ADD
            }

            else -> null
        }
        return if (name == null) null else AnalyticsEvent(name, data)
    }

    companion object {
        internal const val ZERO_PICKER_VALUE = 0.0
    }
}
