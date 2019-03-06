package com.elta.android.presentation.features.main.events.base.initializer

import android.support.v4.content.ContextCompat
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.picker.FormPicker
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.extensions.applyLengthFilter
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import kotlinx.android.synthetic.main.fragment_event_form.view.*

object MedicamentsFormInitializer : FormInitializer() {

    override val pickerConfiguration: FormMeasurementConfig? = null

    override fun View.initHeaderView() {
        toolbarView.title = resources.getString(R.string.events_form_screen_title_medicaments)
        appBarLayoutView.setBackgroundResource(R.drawable.bg_gradient_medicine)
        val endColor = ContextCompat.getColor(context, R.color.shade_blue3)
        collapsingToolbarView.setContentScrimColor(endColor)
        eventFormContainerView.setBackgroundColor(endColor)
        eventInfoContainerView.hide()
    }

    override fun FormPicker.initPickerView() {
        hide()
    }

    override fun View.initFormView() {
        formVariantSelectorView.hide()
        formInputView.show()
        formInputView.setHint(R.string.events_creation_hint_medicine)
        formInputView.applyLengthFilter(DEFAULT_NOTE_LENGTH)
    }

    override fun View.initNoteView() {
        formNoteView.hide()
    }
}