package com.elta.android.presentation.features.main.events.base.initializer

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.picker.FormPicker
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import com.redmadrobot.inputmask.MaskedTextChangedListener

object MedicamentsFormInitializer : FormInitializer() {

    override val pickerConfiguration: FormMeasurementConfig? = null

    override fun View.initHeaderView() = with(binding) {
        toolbarTitleView.text = resources.getString(R.string.events_form_screen_title_medicaments)
        appBarLayoutView.setBackgroundResource(R.color.g_purpur_b)
        eventInfoContainerView.hide()
    }

    override fun FormPicker.initPickerView() {
        hide()
    }

    override fun View.initFormView() = with(binding) {
        formVariantSelectorView.show()

        val hint = resources.getString(R.string.events_creation_hint_add_medicine)
        val spannable = SpannableString(hint)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.g_orange_b)),
            hint.length - 1,
            hint.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        formVariantSelectorView.hint = spannable

        val mask = MaskedTextChangedListener(
            primaryFormat = FORMAT,
            affineFormats = listOf(AFFINE_FORMAT),
            field = formInputView,
            autocomplete = false
        )
        formInputView.addTextChangedListener(mask)

        formInputView.isLongClickable = false
        formInputView.setTextIsSelectable(false)

        formInputView.show()
        formInputView.setHint(R.string.events_creation_hint_count_medicine)

        additionalInput.setHint(R.string.events_creation_hint_name_medicine)
    }

}

const val MEDICAMENT_MEASURE_SUFFIX = " табл/мл"
private const val FORMAT = "[099]{,}[0]$MEDICAMENT_MEASURE_SUFFIX"
private const val AFFINE_FORMAT = "[099]$MEDICAMENT_MEASURE_SUFFIX"
