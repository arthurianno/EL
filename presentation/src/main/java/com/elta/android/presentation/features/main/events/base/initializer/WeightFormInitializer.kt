package com.elta.android.presentation.features.main.events.base.initializer

import android.content.res.Resources
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.picker.FormPicker
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.extensions.hide

object WeightFormInitializer : FormInitializer() {

    private const val WEIGHT_FIRST_PICKER_MAX_VALUE = 200
    private const val WEIGHT_DEFAULT_PICKER_VALUE = 0.0

    private val formatterFunction: Resources.(Int, Int) -> String = { left, right ->
        getString(R.string.event_form_weight_format_mask, left, right)
    }

    override val pickerConfiguration = FormMeasurementConfig(
        firstPickerMaxValue = WEIGHT_FIRST_PICKER_MAX_VALUE,
        firstPickerMinValue = DEFAULT_FIRST_PICKER_MIN_VALUE,
        secondPickerMaxValue = DEFAULT_SECOND_PICKER_MAX_VALUE,
        secondPickerMinValue = DEFAULT_SECOND_PICKER_MIN_VALUE,
        secondMeasureUnit = R.string.event_form_measure_unit_weight,
        resultMappingFunction = defaultDoubleConverter,
        formatter = formatterFunction
    )

    override fun View.initHeaderView() = with(binding) {
        toolbarTitleView.text = resources.getString(R.string.events_form_screen_title_weight)
        appBarLayoutView.setBackgroundResource(R.drawable.bg_gradient_weight)
    }

    override fun FormPicker.initPickerView() {
        config = pickerConfiguration
        setValue(WEIGHT_DEFAULT_PICKER_VALUE)
    }

    override fun View.initFormView() = with(binding) {
        formVariantSelectorView.hide()
        formInputView.hide()
        eventInfoTextView.setText(R.string.events_helper_text_weight)
    }
}
