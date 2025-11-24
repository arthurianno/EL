package com.elta.android.presentation.features.main.events.base.initializer

import android.content.res.Resources
import android.view.View
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.picker.FormPicker
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show

private const val INPUT_LENGTH = 40

data class CalculatorFormInitializer(private val calculatorFlow: CalculatorFlow) : FormInitializer() {

    private val formatterFunction: Resources.(Int, Int) -> String = { left, right ->
        getString(R.string.event_form_bread_format_mask, left, right)
    }

    override val pickerConfiguration = FormMeasurementConfig(
        firstPickerMaxValue = DEFAULT_FIRST_PICKER_MAX_VALUE,
        firstPickerMinValue = DEFAULT_FIRST_PICKER_MIN_VALUE,
        secondPickerMaxValue = DEFAULT_SECOND_PICKER_MAX_VALUE,
        secondPickerMinValue = DEFAULT_SECOND_PICKER_MIN_VALUE,
        secondMeasureUnit = R.string.event_form_measure_unit_bread,
        resultMappingFunction = defaultDoubleConverter,
        formatter = formatterFunction
    )

    override fun View.initHeaderView() = with(binding) {
        toolbarTitleView.text = resources.getString(R.string.events_form_screen_title_bread)
        appBarLayoutView.setBackgroundResource(R.drawable.bg_gradient_bread)
    }

    override fun FormPicker.initPickerView() {
        config = pickerConfiguration
        setValue(DEFAULT_PICKER_VALUE)
    }

    override fun View.initFormView() = with(binding) {
        formInputView.hide()
        with(formVariantSelectorView) {
            hint = context.getString(R.string.events_creation_hint_bread)
            icon = context.getDrawable(R.drawable.ic_tag_def)
        }
        when(calculatorFlow) {
            CalculatorFlow.BREAD_UNITS -> {
                eventInfoContainerView.show()
                formPickerView.show()
            }
            CalculatorFlow.PRODUCT_ONLY -> {
                eventInfoContainerView.hide()
                formPickerView.hide()
            }
        }
        eventInfoTextView.setText(R.string.events_helper_text_bread)
    }
}
