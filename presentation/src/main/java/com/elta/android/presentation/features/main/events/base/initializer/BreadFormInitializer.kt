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
object BreadFormInitializer : FormInitializer {

    private val converterFunction: (Int, Int) -> Double = { left, right ->
        (left * 10 + right) / 10.0
    }

    override val pickerConfiguration = FormMeasurementConfig(
        firstPickerMaxValue = 99,
        firstPickerMinValue = 0,
        secondPickerMaxValue = 9,
        secondPickerMinValue = 0,
        secondMeasureUnit = R.string.event_form_measure_unit_bread,
        resultMappingFunction = converterFunction
    )

    override fun init(view: View) {
        with(view) {
            toolbarView.title = resources.getString(R.string.events_form_screen_title_bread)
            appBarLayoutView.setBackgroundResource(R.drawable.bg_gradient_bread)
            val endColor = ContextCompat.getColor(context, R.color.g_orange_b)
            collapsingToolbarView.setContentScrimColor(endColor)
            eventFormContainerView.setBackgroundColor(endColor)

            formPickerView.config = pickerConfiguration
            formPickerView.setValue(0.0)

            formVariantSelectorView.hide()
            formInputView.show()
            formInputView.setHint(R.string.events_creation_hint_bread)
            formInputView.applyLengthFilter(40)
            eventInfoTextView.setText(R.string.events_helper_text_bread)
            formNoteView.applyLengthFilter(120)
        }
    }
}