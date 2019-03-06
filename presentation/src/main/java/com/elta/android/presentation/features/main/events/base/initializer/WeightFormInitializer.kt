package com.elta.android.presentation.features.main.events.base.initializer

import android.support.v4.content.ContextCompat
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.picker.FormPicker
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.extensions.hide
import kotlinx.android.synthetic.main.fragment_event_form.view.*

object WeightFormInitializer : FormInitializer() {

    private const val WEIGHT_FIRST_PICKER_MAX_VALUE = 200

    override val pickerConfiguration = FormMeasurementConfig(
        firstPickerMaxValue = WEIGHT_FIRST_PICKER_MAX_VALUE,
        firstPickerMinValue = DEFAULT_FIRST_PICKER_MIN_VALUE,
        secondPickerMaxValue = DEFAULT_SECOND_PICKER_MAX_VALUE,
        secondPickerMinValue = DEFAULT_SECOND_PICKER_MIN_VALUE,
        secondMeasureUnit = R.string.event_form_measure_unit_weight,
        resultMappingFunction = defaultDoubleConverter
    )

    override fun View.initHeaderView() {
        toolbarView.title = resources.getString(R.string.events_form_screen_title_weight)
        appBarLayoutView.setBackgroundResource(R.drawable.bg_gradient_weight)
        val endColor = ContextCompat.getColor(context, R.color.g_purpur_b)
        collapsingToolbarView.setContentScrimColor(endColor)
        eventFormContainerView.setBackgroundColor(endColor)
    }

    override fun FormPicker.initPickerView() {
        config = pickerConfiguration
        setValue(DEFAULT_PICKER_VALUE)
    }

    override fun View.initFormView() {
        formVariantSelectorView.hide()
        formInputView.hide()
        eventInfoTextView.setText(R.string.events_helper_text_weight)
    }
}