package com.elta.android.presentation.features.main.events.base.pm

import com.elta.android.common.utils.atEndOfDay
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.getValidator
import com.elta.android.domain.features.diary.tags.model.Tag
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
import com.elta.android.presentation.features.main.events.base.model.EventFormModel
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserResult
import com.elta.android.presentation.messages.SnackBarMessageData
import com.elta.android.presentation.utils.toEventDate
import com.elta.android.presentation.utils.toEventTime
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import org.threeten.bp.ZonedDateTime
import java.util.concurrent.TimeUnit

abstract class BaseEventPm(
    services: ServiceFacade
) : BasePm(services) {

    val formPickerValueChangedAction = action<Double>()
    val updateFormPickerValueCommand = command<Pair<Int, Int>>()

    val formInput = inputControl()
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

    val eventTypeState = state<EventType>()
    protected val formPickerValue = state<Double>()
    protected val selectedDateState = state(ZonedDateTime.now())

    private val exitDialogData: DialogData by lazy { Dialogs.ExitAndLoseData(resources) }
    private val dateInFutureSnackBarData by lazy {
        SnackBarMessageData.SimpleTextMessage(resources.getString(R.string.event_form_date_in_future))
    }

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
            insulin = form.insulin,
            date = form.date,
            note = form.note
        )
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
            .debounceAction()
            .doOnNext { hideKeyBoardCommand.consumer.accept(Unit) }
            .delay(OPEN_SCREEN_DELAY, TimeUnit.MILLISECONDS)
            .map { createChooserConfiguration() }
            .subscribe {
                when (it.eventType) {
                    EventType.BREAD -> router.navigateTo(Screens.CalculatorScreen(it))
                    else -> router.navigateTo(Screens.EventsChooserScreen(it))
                }
            }
            .untilDestroy()

        bus.events<Events.ChooserVariantSelected>()
            .map { it.chooserResult.toSelectorOption() }
            .subscribe(formSelector.option.consumer)
            .untilDestroy()
    }

    private fun createChooserConfiguration() =
        when (eventTypeState.value) {
            EventType.INSULIN -> ChooserConfiguration(
                ChooserType.VARIANTS_WITH_SUBTYPE,
                eventTypeState.value,
                generateChooserId()
            )

            EventType.BREAD -> ChooserConfiguration(
                ChooserType.VARIANTS_WITH_SUBTYPE,
                eventTypeState.value,
                generateChooserId()
            )

            else ->
                ChooserConfiguration(
                    ChooserType.VARIANTS,
                    eventTypeState.value,
                    generateChooserId()
                )
        }

    private fun bindFormTagSelection() {
        tagSelector.clickAction.observable
            .debounceAction()
            .doOnNext { hideKeyBoardCommand.consumer.accept(Unit) }
            .delay(OPEN_SCREEN_DELAY, TimeUnit.MILLISECONDS)
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
            .filter(::validateSelectedDate)
            .subscribe(selectedDateState.consumer)
            .untilDestroy()
    }

    private fun generateChooserId() = when (eventTypeState.value) {
        EventType.INSULIN, EventType.ACTIVITY -> formSelector.option.value.meta.toString()
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

    companion object {
        private const val OPEN_SCREEN_DELAY = 300L // millis
    }
}
