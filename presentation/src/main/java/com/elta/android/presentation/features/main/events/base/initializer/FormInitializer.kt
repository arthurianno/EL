package com.elta.android.presentation.features.main.events.base.initializer

import android.text.InputFilter
import android.view.View
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.databinding.FragmentEventFormBinding
import com.elta.android.presentation.widgets.picker.FormPicker
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig

internal const val DEFAULT_NOTE_LENGTH = 120
internal const val DEFAULT_PICKER_VALUE = 0.0
internal const val DEFAULT_FIRST_PICKER_MAX_VALUE = 99
internal const val DEFAULT_FIRST_PICKER_MIN_VALUE = 0
internal const val DEFAULT_SECOND_PICKER_MAX_VALUE = 9
internal const val DEFAULT_SECOND_PICKER_MIN_VALUE = 0
internal const val TEN = 10.0

internal val defaultDoubleConverter: (Int, Int) -> Double = { left, right ->
    (left * TEN + right) / TEN
}

abstract class FormInitializer {

    abstract val pickerConfiguration: FormMeasurementConfig?

    private var _binding: FragmentEventFormBinding? = null
    val binding: FragmentEventFormBinding
        get() = checkNotNull(_binding)

    fun init(view: View) {
        _binding = FragmentEventFormBinding.bind(view)
        with(view) {
            initHeaderView()
            binding.formPickerView.initPickerView()
            initFormView()
            initNoteView()
        }
    }

    open fun setPickerValue(pickerValue: Double?) {
        binding.formPickerView.setValue(pickerValue ?: DEFAULT_PICKER_VALUE)
    }

    open fun View.initNoteView() {
        binding.formNoteView.filters = arrayOf(InputFilter.LengthFilter(DEFAULT_NOTE_LENGTH))
    }

    abstract fun View.initHeaderView()

    abstract fun FormPicker.initPickerView()

    abstract fun View.initFormView()
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
