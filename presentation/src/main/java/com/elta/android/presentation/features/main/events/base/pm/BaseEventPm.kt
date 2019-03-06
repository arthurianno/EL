package com.elta.android.presentation.features.main.events.base.pm

import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.formSelectorControl
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserResult
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import me.dmdev.rxpm.widget.inputControl

abstract class BaseEventPm constructor(
    services: ServiceFacade
) : BasePm(services) {

    val formPickerValueChangedAction = Action<Double>()

    val formInput = inputControl()
    val formSelector = formSelectorControl()
    val tagSelector = formSelectorControl()
    val dateSelector = formSelectorControl()
    val timeSelector = formSelectorControl()
    val noteInput = inputControl()
    val mainActionTitleState = State<String>()
    val mainActionVisibilityState = State(false)
    val mainAction = Action<Unit>()

    private val formPickerValue = State<Double>()
    private val eventTypeState = State<EventType>()

    override fun onCreate() {
        super.onCreate()
        bindFormPicker()
        bindFormVariantSelection()
        bindFormTagSelection()
    }

    fun setEventType(eventType: EventType) {
        eventTypeState.consumer.accept(eventType)
    }

    private fun bindFormPicker() {
        formPickerValueChangedAction.observable
            .subscribe(formPickerValue.consumer)
            .untilDestroy()
    }

    private fun bindFormVariantSelection() {
        formSelector.clickAction.observable
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
            .map { ChooserConfiguration(ChooserType.GROUP_TAGS, eventTypeState.value) }
            .subscribe { router.navigateTo(Screens.EventsChooserScreen(it)) }
            .untilDestroy()

        bus.events<Events.ChooserTagSelected>()
            .map { it.chooserResult.toSelectorOption() }
            .subscribe(tagSelector.option.consumer)
            .untilDestroy()
    }

    private fun ChooserResult.toSelectorOption() =
        SelectorOption(
            text = name,
            icon = iconId?.let { id -> resources.getDrawable(id) },
            meta = id
        )
}