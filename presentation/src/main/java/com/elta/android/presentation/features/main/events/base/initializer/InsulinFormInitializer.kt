package com.elta.android.presentation.features.main.events.base.initializer

import android.support.v4.content.ContextCompat
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.extensions.applyLengthFilter
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import kotlinx.android.synthetic.main.fragment_event_form.view.*

@Suppress("MagicNumber")
object InsulinFormInitializer : FormInitializer {

    private val converterFunction: (Int, Int) -> Double = { left, right ->
        (left * 10 + right) / 10.0
    }

    override val pickerConfiguration = FormMeasurementConfig(
        firstPickerMaxValue = 99,
        firstPickerMinValue = 0,
        secondPickerMaxValue = 9,
        secondPickerMinValue = 0,
        secondMeasureUnit = R.string.event_form_measure_unit_insulin,
        resultMappingFunction = converterFunction
    )

    override fun init(view: View) {
        with(view) {
            toolbarView.title = resources.getString(R.string.events_form_screen_title_insulin)
            appBarLayoutView.setBackgroundResource(R.drawable.bg_gradient_insulin)
            val endColor = ContextCompat.getColor(context, R.color.shade_blue)
            collapsingToolbarView.setContentScrimColor(endColor)
            eventFormContainerView.setBackgroundColor(endColor)

            formPickerView.config = pickerConfiguration
            formPickerView.setValue(0.0)

            formVariantSelectorView.show()
            formVariantSelectorView.hint = resources.getString(R.string.events_creation_hint_insulin)
            formInputView.hide()
            eventInfoTextView.setText(R.string.events_helper_text_insulin)
            formNoteView.applyLengthFilter(120)
        }
    }
}