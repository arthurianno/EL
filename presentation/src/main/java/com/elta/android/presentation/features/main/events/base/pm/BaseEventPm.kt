package com.elta.android.presentation.features.main.events.base.pm

import com.elta.android.common.utils.atEndOfDay
import com.elta.android.domain.features.calculator.interactor.CachedDishesUseCase
import com.elta.android.domain.features.calculator.interactor.CalculatorFragmentResultHandler
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.events.model.getValidator
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.formSelectorControl
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.calcutator.mappers.ZERO_COUNT_DOUBLE
import com.elta.android.presentation.features.main.events.base.mapper.toChooserInsulin
import com.elta.android.presentation.features.main.events.base.mapper.toChooserMedicament
import com.elta.android.presentation.features.main.events.base.model.EventFormModel
import com.elta.android.presentation.features.main.events.base.model.MedicamentModel
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserResult
import com.elta.android.presentation.features.main.events.mapper.toPickerValues
import com.elta.android.presentation.messages.SnackBarMessageData
import com.elta.android.presentation.utils.toEventDate
import com.elta.android.presentation.utils.toEventTime
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import org.threeten.bp.ZonedDateTime
import java.util.concurrent.TimeUnit

private const val OPEN_SCREEN_DELAY_MILLIS = 300L
private const val LOCKED_FORM_PICKER_DELAY_MILLIS = 500L

abstract class BaseEventPm(
    services: ServiceFacade,
    private val calculatorFragmentResultHandler: CalculatorFragmentResultHandler,
    private val cachedDishes: CachedDishesUseCase,
) : BasePm(services) {
    val formPickerValueChangedAction = action<Double>()
    val updateFormPickerValueCommand = command<Pair<Int, Int>>()
    val formInput = inputControl()
    val additionalInput = inputControl()
    val formSelector = formSelectorControl()
    val tagSelector = formSelectorControl()
    val dateSelector = formSelectorControl()
    val timeSelector = formSelectorControl()
    val noteInput = inputControl()
    val mainActionTitleState = state<String>()
    val mainActionVisibilityState = state(false)
    val mainAction = action<Unit>()
    val profileState = state<Profile>()
    val getProfileAction = action<Unit>()
    val showDatePickerDialog = command<ZonedDateTime>(bufferSize = 1)
    val showTimePickerDialog = command<ZonedDateTime>(bufferSize = 1)
    val dateTimeSelectedAction = action<ZonedDateTime>()
    val backHandleAction = action<Unit>()
    val exitDialogAction = action<Unit>()
    val exitDialogControl = dialogControl<DialogData, DialogResult>()
    val breadUnitsChangeDialogControl = dialogControl<DialogData, DialogResult>()
    val userHadChangesBreadUnitsDialogControl = dialogControl<DialogData, DialogResult>()
    val editingXEIsNotAvailableAction = action<Unit>()
    val editingXEIsNotAvailableControl = dialogControl<DialogData, DialogResult>()
    val eventTypeState = state<EventType>()
    val dishes = state<List<Dish>>(emptyList())
    val medicamentState = state<MedicamentModel>()
    val eventState = state<EventV2>()
    val mealSelector = state(MealTag.NOT_SELECTED)
    val beforeMealAction = action<Unit>()
    val afterMealAction = action<Unit>()
    val changeAppBarColorAction = action<Double>()
    val appBarColorState = state<Int>()
    private val calculatorFlowState = state<CalculatorFlow>()
    protected val formPickerValue = state<Double>()
    protected val selectedDateState = state(ZonedDateTime.now())
    protected val medicamentMeasureSuffix: String
        get() = resources.getString(R.string.medicament_measure_suffix)
    private val exitDialogData: DialogData by lazy { Dialogs.ExitAndLoseData(resources) }
    private val breadUnitsChangeNotifyDialogData: DialogData by lazy {
        Dialogs.ChangeBreadUnitsData(resources)
    }
    private val editingXEIsNotAvailableData: DialogData by lazy {
        Dialogs.EditingXEIsNotAvailableData(resources)
    }

    private val dateInFutureSnackBarData by lazy {
        SnackBarMessageData.SimpleTextMessage(resources.getString(R.string.event_form_date_in_future))
    }
    private var lockedChangeFormPicker: Boolean = false

    abstract fun handleBack(i: Unit)

    abstract fun observeEventChanges()

    override fun onCreate() {
        super.onCreate()
        observeFormPicker()
        observeFormVariantSelection()
        observeFormTagSelection()
        observeDateSelectors()
        observeHandleBack()
        observeEventChanges()
        observableCalculatorFlow()
        observeDishesResult()
        observeDishesChanges()
        observeMealsAction()
        observeAppBarColorAction()
    }

    private fun observableCalculatorFlow() {
        eventTypeState.observable
            .doOnNext { eventType ->
                if (eventType is EventType.Bread) calculatorFlowState.consumer.accept(eventType.calculatorFlow)
            }
            .subscribe()
            .untilDestroy()
    }

    fun setEventType(eventType: EventType) {
        eventTypeState.consumer.accept(eventType)
    }

    protected fun isFormValid(
        form: EventFormModel,
        glucoseFormat: GlucoseFormat?
    ): Boolean {
        val validator = checkNotNull(form.eventType).getValidator(glucoseFormat)
        return validator.isValid(
            value = form.value,
            kind = form.kind,
            name = form.name,
            duration = form.duration,
            insulinMedicament = form.insulinMedicament,
            medicament = form.medicament,
            tabletsNumber = form.tabletsNumber,
            dishes = dishes.valueOrNull,
            flowIsEdit = eventState.valueOrNull != null,
            date = form.date,
            note = form.note
        )
    }

    protected fun handleSuccess(isCreate: Boolean) {
        bus.event(Events.EventsChanged(isCreate))
        router.exit()
    }

    private fun observeDishesResult() {
        launch {
            calculatorFragmentResultHandler.resultAsFlow()
                .catch { handleError(it) }
                .collect { list ->
                    dishes.consumer.accept(list)
                    cachedDishes(list)

                    when (calculatorFlowState.value) {
                        CalculatorFlow.BREAD_UNITS -> {
                            val breadUnits = list.sumOf { dish -> dish.breadUnits ?: 0.0 }
                            val currentBreadUnits = formPickerValue.valueOrNull
                            if (currentBreadUnits != breadUnits) {
                                if (currentBreadUnits != 0.0) {
                                    breadUnitsChangeDialogControl.show(
                                        breadUnitsChangeNotifyDialogData
                                    )
                                }
                                updateFormPickerValueCommand.consumer.accept(breadUnits.toPickerValues())
                            }
                        }

                        CalculatorFlow.PRODUCT_ONLY -> {

                        }
                    }
                }
        }
    }

    private fun observeDishesChanges() {
        dishes.observable.subscribe {
            if (it.isNotEmpty()) {
                launch {
                    delay(LOCKED_FORM_PICKER_DELAY_MILLIS)
                    lockedChangeFormPicker = true
                }
            }
        }
    }

    private fun observeHandleBack() {
        backHandleAction.observable
            .doOnNext(::handleBack)
            .subscribe()
            .untilDestroy()

        exitDialogAction.observable
            .switchMapMaybe {
                exitDialogControl.showForResult(exitDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
            .doOnNext { router.exit() }
            .subscribe()
            .untilDestroy()
    }

    private fun observeFormPicker() {
        formPickerValueChangedAction.observable
            .subscribe {
                formPickerValue.consumer.accept(it)
                if (lockedChangeFormPicker) {
                    lockedChangeFormPicker = false
                }
            }
            .untilDestroy()

        editingXEIsNotAvailableAction.observable
            .subscribe {
                editingXEIsNotAvailableControl.show(editingXEIsNotAvailableData)
            }
            .untilDestroy()
    }

    private fun observeFormVariantSelection() {
        formSelector.clickAction.observable
            .debounceAction()
            .doOnNext { hideKeyBoardCommand.consumer.accept(Unit) }
            .delay(OPEN_SCREEN_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .map { createChooserConfiguration() }
            .subscribe { configurator ->
                when (configurator.eventType) {
                    is EventType.Bread -> {
                        lockedChangeFormPicker = false
                        router.navigateTo(Screens.CalculatorScreen(calculatorFlowState.value))
                    }

                    EventType.Medicaments -> router.navigateTo(
                        Screens.EventSelectorScreen(configurator)
                    )

                    else -> router.navigateTo(Screens.EventsChooserScreen(configurator))
                }
            }
            .untilDestroy()
        bus.events<Events.ChooserVariantSelected>()
            .map { it.chooserResult.toSelectorOption() }
            .doOnNext {
                val option = it.meta
                if (option is Pair<*, *>)
                    medicamentState.consumer.accept(
                        MedicamentModel(
                            medicament = option.first as Medicament,
                            fromEvent = false,
                            otherName = option.second as String?
                        )
                    )
            }
            .subscribe(formSelector.option.consumer)
            .untilDestroy()
    }

    private fun observeMealsAction() {
        beforeMealAction.observable
            .doOnNext { switchMealTag(MealTag.BEFOREMEAL) }
            .subscribe()
            .untilDestroy()
        afterMealAction.observable
            .doOnNext { switchMealTag(MealTag.AFTERMEAL) }
            .subscribe()
            .untilDestroy()
    }

    private fun switchMealTag(mealTag: MealTag) {
        val selectedValue = mealSelector.value
        mealSelector.consumer.accept(
            if (selectedValue == MealTag.NOT_SELECTED || selectedValue != mealTag) {
                mealTag
            } else {
                MealTag.NOT_SELECTED
            }
        )
    }

    private fun observeAppBarColorAction() {
        changeAppBarColorAction.observable
            .doOnNext {
                if(eventTypeState.valueOrNull is EventType.Glucose){
                    val glucoseNormalRange = profileState.valueOrNull?.glucoseLevelSettings?.normal ?: GlucoseLevelSettings.defaultRange
                    val drawableId = when {
                        it == ZERO_COUNT_DOUBLE -> R.drawable.bg_gradient_blue
                        it in glucoseNormalRange -> R.drawable.bg_gradient_green
                        it > glucoseNormalRange.end -> R.drawable.bg_gradient_red
                        else -> R.drawable.bg_gradient_blue
                    }
                    appBarColorState.consumer.accept(drawableId)
                }
            }
            .subscribe()
            .untilDestroy()
    }

    private fun createChooserConfiguration() =
        when (eventTypeState.value) {
            EventType.Insulin -> ChooserConfiguration(
                chooserType = ChooserType.VARIANTS_WITH_SUBTYPE,
                eventType = eventTypeState.value,
                id = generateChooserId(),
                insulinMedicament = formSelector.option.valueOrNull.toChooserInsulin(resources)
            )

            is EventType.Bread -> ChooserConfiguration(
                chooserType = ChooserType.VARIANTS_WITH_SUBTYPE,
                eventType = eventTypeState.value,
                id = generateChooserId()
            )

            EventType.Medicaments -> ChooserConfiguration(
                chooserType = ChooserType.VARIANTS,
                eventType = eventTypeState.value,
                id = generateChooserId(),
                medicament = formSelector.option.valueOrNull.toChooserMedicament()
            )

            else ->
                ChooserConfiguration(
                    ChooserType.VARIANTS,
                    eventTypeState.value,
                    generateChooserId()
                )
        }

    private fun observeFormTagSelection() {
        tagSelector.clickAction.observable
            .debounceAction()
            .doOnNext { hideKeyBoardCommand.consumer.accept(Unit) }
            .delay(OPEN_SCREEN_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .map {
                ChooserConfiguration(
                    chooserType = ChooserType.GROUP_TAGS,
                    eventType = eventTypeState.value,
                    id = (tagSelector.option.value.meta as? Tag)?.id
                )
            }
            .subscribe { router.navigateTo(Screens.EventsChooserScreen(it)) }
            .untilDestroy()
        bus.events<Events.ChooserTagSelected>()
            .map { it.chooserResult.toSelectorOption() }
            .subscribe(tagSelector.option.consumer)
            .untilDestroy()
    }

    private fun observeDateSelectors() {
        selectedDateState.observable
            .map { it.toEventTime(resources).toSimpleSelectorOption() }
            .subscribe(timeSelector.option.consumer)
            .untilDestroy()
        selectedDateState.observable
            .map { it.toEventDate(resources).toSimpleSelectorOption() }
            .subscribe(dateSelector.option.consumer)
            .untilDestroy()
        dateSelector.clickAction.observable
            .map { selectedDateState.value }
            .subscribe(showDatePickerDialog.consumer)
            .untilDestroy()
        timeSelector.clickAction.observable
            .map { selectedDateState.value }
            .subscribe(showTimePickerDialog.consumer)
            .untilDestroy()
        dateTimeSelectedAction.observable
            .filter(::validateSelectedDate)
            .subscribe(selectedDateState.consumer)
            .untilDestroy()

        formInput.focusChanges
            .observable
            .subscribe { focus ->
                val textInField = formInput.text.valueOrNull.orEmpty()

                val text = when {
                    focus && textInField.isNotBlank() -> textInField.removeSuffix(medicamentMeasureSuffix)
                    !textInField.contains(medicamentMeasureSuffix) -> textInField + medicamentMeasureSuffix
                    else -> textInField
                }
                formInput.text.consumer.accept(text)
            }
            .untilDestroy()
    }

    private fun generateChooserId() = when (eventTypeState.value) {
        EventType.Insulin, EventType.Activity, EventType.Medicaments -> formSelector.option.value.meta.toString()
        else -> null
    }

    private fun ChooserResult.toSelectorOption() =
        SelectorOption(
            text = name,
            icon = iconId?.let { id -> resources.getDrawable(id) },
            meta = meta
        )

    private fun String.toSimpleSelectorOption() =
        SelectorOption(this)

    private fun validateSelectedDate(date: ZonedDateTime) =
        !date.isAfter(ZonedDateTime.now().atEndOfDay()).also {
            if (it) showSnackBar(dateInFutureSnackBarData)
        }
}
