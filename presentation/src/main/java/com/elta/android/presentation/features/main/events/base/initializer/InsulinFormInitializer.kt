package com.elta.android.presentation.features.main.events.base.initializer

import android.content.res.Resources
import android.support.v4.content.ContextCompat
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.picker.FormPicker
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import kotlinx.android.synthetic.main.fragment_event_form.view.*

object InsulinFormInitializer : FormInitializer() {

    private val formatterFunction: Resources.(Int, Int) -> String = { left, right ->
        getString(R.string.event_form_insulin_format_mask, left, right)
    }

    override val pickerConfiguration = FormMeasurementConfig(
        firstPickerMaxValue = DEFAULT_FIRST_PICKER_MAX_VALUE,
        firstPickerMinValue = DEFAULT_FIRST_PICKER_MIN_VALUE,
        secondPickerMaxValue = DEFAULT_SECOND_PICKER_MAX_VALUE,
        secondPickerMinValue = DEFAULT_SECOND_PICKER_MIN_VALUE,
        secondMeasureUnit = R.string.event_form_measure_unit_insulin,
        resultMappingFunction = defaultDoubleConverter,
        formatter = formatterFunction
    )

    override fun View.initHeaderView() {
        toolbarView.title = resources.getString(R.string.events_form_screen_title_insulin)
        appBarLayoutView.setBackgroundResource(R.drawable.bg_gradient_insulin)
        val endColor = ContextCompat.getColor(context, R.color.shade_blue)
        collapsingToolbarView.setContentScrimColor(endColor)
        eventFormContainerView.setBackgroundColor(endColor)
    }

    override fun FormPicker.initPickerView() {
        config = pickerConfiguration
        setValue(DEFAULT_PICKER_VALUE)
    }

    override fun View.initFormView() {
        formVariantSelectorView.show()
        formVariantSelectorView.hint = resources.getString(R.string.events_creation_hint_insulin)
        formInputView.hide()
        eventInfoTextView.setText(R.string.events_helper_text_insulin)
    }
}