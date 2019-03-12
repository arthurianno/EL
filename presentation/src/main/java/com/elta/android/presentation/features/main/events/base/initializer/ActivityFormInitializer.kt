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
import java.util.concurrent.TimeUnit

object ActivityFormInitializer : FormInitializer() {

    private const val DEFAULT_PICKER_VALUE = 0
    private const val HOURS_MAX_VALUE = 23
    private const val HOURS_MIN_VALUE = 0
    private const val MINUTES_MAX_VALUE = 59
    private const val MINUTES_MIN_VALUE = 0

    private val converterFunction: (Int, Int) -> Double = { left, right ->
        (TimeUnit.HOURS.toSeconds(left.toLong()) + TimeUnit.MINUTES.toSeconds(right.toLong())).toDouble()
    }

    private val formatterFunction: Resources.(Int, Int) -> String = { left, right ->
        getString(R.string.event_form_activity_format_mask, left, right)
    }

    override val pickerConfiguration = FormMeasurementConfig(
        firstPickerMaxValue = HOURS_MAX_VALUE,
        firstPickerMinValue = HOURS_MIN_VALUE,
        secondPickerMaxValue = MINUTES_MAX_VALUE,
        secondPickerMinValue = MINUTES_MIN_VALUE,
        firstMeasureUnit = R.string.event_form_measure_unit_hours,
        secondMeasureUnit = R.string.event_form_measure_unit_minutes,
        resultMappingFunction = converterFunction,
        formatter = formatterFunction
    )

    override fun View.initHeaderView() {
        toolbarTitleView.text = resources.getString(R.string.events_form_screen_title_activity)
        appBarLayoutView.setBackgroundResource(R.drawable.bg_gradient_activity)
        eventFormContainerView.setBackgroundColor(ContextCompat.getColor(context, R.color.shade_g_purpur_a))
    }

    override fun FormPicker.initPickerView() {
        config = pickerConfiguration
        setValues(DEFAULT_PICKER_VALUE, DEFAULT_PICKER_VALUE)
    }

    override fun View.initFormView() {
        formInputView.hide()
        formVariantSelectorView.show()
        formVariantSelectorView.hint = resources.getString(R.string.events_creation_hint_activities)
        formVariantSelectorView.setIconRes(R.drawable.ic_activity_default)
        eventInfoTextView.setText(R.string.events_helper_text_activity)
    }
}