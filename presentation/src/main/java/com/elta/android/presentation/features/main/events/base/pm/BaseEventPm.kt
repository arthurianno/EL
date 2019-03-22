package com.elta.android.presentation.features.main.events.base.pm

import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.formSelectorControl
import com.elta.android.presentation.core.ui.dialog.DialogData
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

    private val exitDialogData: DialogData by lazy { Dialogs.EventExit(resources) }

    override fun onCreate() {
        super.onCreate()
        bindFormPicker()
        bindFormVariantSelection()
        bindFormTagSelection()
        bindDateSelectors()
        bindHandleBack()
    }

    fun setEventType(eventType: EventType) {
        eventTypeState.consumer.accept(eventType)
    }

    abstract fun handleBack(i: Unit)

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
            .map { ChooserConfiguration(ChooserType.VARIANTS, eventTypeState.value) }
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
            .map { ChooserConfiguration(ChooserType.GROUP_TAGS, eventTypeState.value) }
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

    private fun ChooserResult.toSelectorOption() =
        SelectorOption(
            text = name,
            icon = iconId?.let { id -> resources.getDrawable(id) },
            meta = meta
        )

    private fun String.toSimpleSelectorOption() =
        SelectorOption(this)

    enum class DialogResult {
        NEGATIVE, POSITIVE
    }

    companion object {
        private const val OPEN_SCREEN_DELAY = 300L // millis
    }
}