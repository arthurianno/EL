package com.elta.android.presentation.features.main.events.base.pm

import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventType.*
import com.elta.android.domain.features.diary.events.model.getValidator
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.formSelectorControl
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.main.events.base.model.EventFormModel
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserResult
import com.elta.android.presentation.utils.toEventDate
import com.elta.android.presentation.utils.toEventTime
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import java.util.Date
import java.util.concurrent.TimeUnit

abstract class BaseEventPm constructor(
    services: ServiceFacade
) : BasePm(services) {

    val formPickerValueChangedAction = Action<Double>()
    val updateFormPickerValueCommand = Command<Pair<Int, Int>>()

    val formInput = inputControl()
    val formSelector = formSelectorControl()
    val tagSelector = formSelectorControl()
    val dateSelector = formSelectorControl()
    val timeSelector = formSelectorControl()
    val noteInput = inputControl()
    val mainActionTitleState = State<String>()
    val mainActionVisibilityState = State(false)
    val mainAction = Action<Unit>()

    val showDatePickerDialog = Command<Date>(bufferSize = 1)
    val showTimePickerDialog = Command<Date>(bufferSize = 1)
    val dateTimeSelectedAction = Action<Date>()

    val backHandleAction = Action<Unit>()
    val exitDialogAction = Action<Unit>()

    val exitDialogControl = dialogControl<DialogData, DialogResult>()

    protected val formPickerValue = State<Double>()
    protected val eventTypeState = State<EventType>()
    protected val selectedDateState = State(Date())

    private val exitDialogData: DialogData by lazy { Dialogs.ExitAndLoseData(resources) }

    abstract fun handleBack(i: Unit)

    abstract fun observeEventChanges()

    override fun onCreate() {
        super.onCreate()
        bindFormPicker()
        bindFormVariantSelection()
        bindFormTagSelection()
        bindDateSelectors()
        bindHandleBack()
        observeEventChanges()
    }

    fun setEventType(eventType: EventType) {
        eventTypeState.consumer.accept(eventType)
    }

    protected fun isFormValid(form: EventFormModel): Boolean {
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

    protected fun Date?.isDateChanged(other: Date): Boolean {
        return when {
            this == null -> false
            else -> this != other
        }
    }

    protected fun handleSuccess(isCreate: Boolean) {
        bus.event(Events.EventsChanged(isCreate))
        router.exit()
    }

    private fun bindHandleBack() {
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

    private fun bindFormPicker() {
        formPickerValueChangedAction.observable
            .subscribe(formPickerValue.consumer)
            .untilDestroy()
    }

    private fun bindFormVariantSelection() {
        formSelector.clickAction.observable
            .doOnNext { hideKeyBoardCommand.consumer.accept(Unit) }
            .delay(OPEN_SCREEN_DELAY, TimeUnit.MILLISECONDS)
            .map { ChooserConfiguration(ChooserType.VARIANTS, eventTypeState.value, generateChooserId()) }
            .subscribe { router.navigateTo(Screens.EventsChooserScreen(it)) }
            .untilDestroy()

        bus.events<Events.ChooserVariantSelected>()
            .map { it.chooserResult.toSelectorOption() }
            .subscribe(formSelector.option.consumer)
            .untilDestroy()
    }

    private fun bindFormTagSelection() {
        tagSelector.clickAction.observable
            .doOnNext { hideKeyBoardCommand.consumer.accept(Unit) }
            .delay(OPEN_SCREEN_DELAY, TimeUnit.MILLISECONDS)
            .map {
                ChooserConfiguration(ChooserType.GROUP_TAGS, eventTypeState.value,
                    (tagSelector.option.value.meta as? Tag)?.id)
            }
            .subscribe { router.navigateTo(Screens.EventsChooserScreen(it)) }
            .untilDestroy()

        bus.events<Events.ChooserTagSelected>()
            .map { it.chooserResult.toSelectorOption() }
            .subscribe(tagSelector.option.consumer)
            .untilDestroy()
    }

    private fun bindDateSelectors() {
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
            .subscribe(selectedDateState.consumer)
            .untilDestroy()
    }

    private fun generateChooserId() = when (eventTypeState.value) {
        INSULIN, ACTIVITY -> formSelector.option.value.meta.toString()
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

    companion object {
        private const val OPEN_SCREEN_DELAY = 300L // millis
    }
}