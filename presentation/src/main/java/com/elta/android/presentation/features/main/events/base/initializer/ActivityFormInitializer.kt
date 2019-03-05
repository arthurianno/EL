package com.elta.android.presentation.features.main.events.base.initializer

import android.support.v4.content.ContextCompat
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.extensions.applyLengthFilter
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import kotlinx.android.synthetic.main.fragment_event_form.view.*
import java.util.concurrent.TimeUnit

@Suppress("MagicNumber")
object ActivityFormInitializer : FormInitializer {

    private val converterFunction: (Int, Int) -> Double = { left, right ->
        (TimeUnit.HOURS.toSeconds(left.toLong()) + TimeUnit.MINUTES.toSeconds(right.toLong())).toDouble()
    }

    override val pickerConfiguration = FormMeasurementConfig(
        firstPickerMaxValue = 23,
        firstPickerMinValue = 0,
        secondPickerMaxValue = 59,
        secondPickerMinValue = 0,
        firstMeasureUnit = R.string.event_form_measure_unit_hours,
        secondMeasureUnit = R.string.event_form_measure_unit_minutes,
        resultMappingFunction = converterFunction
    )

    override fun init(view: View) {
        with(view) {
            toolbarView.title = resources.getString(R.string.events_form_screen_title_activity)
            appBarLayoutView.setBackgroundResource(R.drawable.bg_gradient_activity)
            val endColor = ContextCompat.getColor(context, R.color.shade_g_purpur_a)
            collapsingToolbarView.setContentScrimColor(endColor)
            eventFormContainerView.setBackgroundColor(endColor)

            formPickerView.config = pickerConfiguration
            formPickerView.setValues(0, 0)

            formInputView.hide()
            formVariantSelectorView.show()
            formVariantSelectorView.hint = resources.getString(R.string.events_creation_hint_activities)
            formVariantSelectorView.setIconRes(R.drawable.ic_activity_default)
            eventInfoTextView.setText(R.string.events_helper_text_activity)
            formNoteView.applyLengthFilter(120)
        }
    }
}