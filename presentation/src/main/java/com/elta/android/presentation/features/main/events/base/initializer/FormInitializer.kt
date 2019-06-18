package com.elta.android.presentation.features.main.events.base.initializer

import android.text.InputFilter
import android.view.View
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.widgets.picker.FormPicker
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import kotlinx.android.synthetic.main.fragment_event_form.view.*

abstract class FormInitializer {

    abstract val pickerConfiguration: FormMeasurementConfig?

    fun init(view: View) {
        with(view) {
            initHeaderView()
            formPickerView.initPickerView()
            initFormView()
            initNoteView()
        }
    }

    open fun View.initNoteView() {
        formNoteView.filters = arrayOf(InputFilter.LengthFilter(DEFAULT_NOTE_LENGTH))
    }

    abstract fun View.initHeaderView()

    abstract fun FormPicker.initPickerView()

    abstract fun View.initFormView()
}

const val DEFAULT_NOTE_LENGTH = 120
const val DEFAULT_PICKER_VALUE = 0.0
const val DEFAULT_FIRST_PICKER_MAX_VALUE = 99
const val DEFAULT_FIRST_PICKER_MIN_VALUE = 0
const val DEFAULT_SECOND_PICKER_MAX_VALUE = 9
const val DEFAULT_SECOND_PICKER_MIN_VALUE = 0
const val TEN = 10.0

val defaultDoubleConverter: (Int, Int) -> Double = { left, right ->
    (left * TEN + right) / TEN
}

fun EventType.makeFormInitializer() =
    when (this) {
        EventType.BREAD -> BreadFormInitializer
        EventType.INSULIN -> InsulinFormInitializer
        EventType.MEDICAMENTS -> MedicamentsFormInitializer
        EventType.ACTIVITY -> ActivityFormInitializer
        EventType.WEIGHT -> WeightFormInitializer
        else -> throw IllegalArgumentException("No form initializer for GLUCOSE type")
    }