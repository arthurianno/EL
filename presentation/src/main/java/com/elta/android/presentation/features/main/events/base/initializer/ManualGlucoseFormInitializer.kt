package com.elta.android.presentation.features.main.events.base.initializer

import android.content.res.Resources
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.picker.FormPicker
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.extensions.hide

object ManualGlucoseFormInitializer : FormInitializer() {

    private val formatterFunction: Resources.(Int, Int) -> String = { left, right ->
        getString(R.string.event_form_glucose_format_mask, left, right)
    }

    override val pickerConfiguration = FormMeasurementConfig(
        firstPickerMaxValue = GLUCOSE_CAPILLARY_LEVEL_MAX_VALUE,
        firstPickerMinValue = DEFAULT_FIRST_PICKER_MIN_VALUE,
        secondPickerMaxValue = DEFAULT_SECOND_PICKER_MAX_VALUE,
        secondPickerMinValue = DEFAULT_SECOND_PICKER_MIN_VALUE,
        secondMeasureUnit = R.string.event_form_measure_unit_glucose,
        resultMappingFunction = defaultDoubleConverter,
        formatter = formatterFunction
    )

    val pickerPlasmaConfiguration = FormMeasurementConfig(
        firstPickerMaxValue = GLUCOSE_PLASMA_LEVEL_MAX_VALUE,
        firstPickerMinValue = DEFAULT_FIRST_PICKER_MIN_VALUE,
        secondPickerMaxValue = DEFAULT_SECOND_PICKER_MAX_VALUE,
        secondPickerMinValue = DEFAULT_SECOND_PICKER_MIN_VALUE,
        secondMeasureUnit = R.string.event_form_measure_unit_glucose,
        resultMappingFunction = defaultDoubleConverter,
        formatter = formatterFunction
    )

    override fun View.initHeaderView() = with(binding) {
        toolbarTitleView.text =
            resources.getString(R.string.events_form_screen_title_glucose_manual)
        appBarLayoutView.setBackgroundResource(R.drawable.bg_gradient_weight)
    }

    override fun FormPicker.initPickerView() {
        config = pickerConfiguration
        setValue(DEFAULT_PICKER_VALUE)
    }

    override fun View.initFormView() = with(binding) {
        formVariantSelectorView.hide()
        formInputView.hide()
        // Текст меняет в зависимости от формата глюкозы в BaseEventPm
        eventInfoTextView.setText(R.string.events_helper_text_glucose_capillary)
    }
}

const val GLUCOSE_CAPILLARY_LEVEL_MAX_VALUE = 28
const val GLUCOSE_PLASMA_LEVEL_MAX_VALUE = 31
