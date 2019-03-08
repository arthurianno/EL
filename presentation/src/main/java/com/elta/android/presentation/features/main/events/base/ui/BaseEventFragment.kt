package com.elta.android.presentation.features.main.events.base.ui

import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.events.base.initializer.makeFormInitializer
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import com.elta.android.presentation.utils.appbar.AppBarState
import com.elta.android.presentation.utils.appbar.observeState
import com.elta.android.presentation.utils.showDatePickerDialog
import com.elta.android.presentation.utils.showTimePickerDialog
import com.elta.android.presentation.utils.shows
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.fragment_event_form.*
import java.util.Date

abstract class BaseEventFragment<T : BaseEventPm> : BaseFragment<T>() {

    override val screenLayout: Int = R.layout.fragment_event_form
    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentLightStatusBarConfigProvider

    private val exitDialog by lazy {
        MaterialDialog.Builder(activity!!)
            .cancelable(false)
            .title(R.string.event_form_exit_dialog_title)
            .content(R.string.event_form_exit_dialog_body)
            .negativeText(R.string.event_form_exit_dialog_cancel_button)
            .positiveText(R.string.event_form_exit_dialog_confirm_button)
            .onPositive { _, _ -> passTo(presentationModel.exitConfirmedAction) }
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarView.setNavigationOnClickListener { activity?.onBackPressed() }
        getEventType().makeFormInitializer().init(view)
        presentationModel.setEventType(getEventType())
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        observeAppBarChanges()
        formPickerView.valueChanges().bindTo(pm.formPickerValueChangedAction)
        formSaveButtonView.clicks().bindTo(pm.mainAction)
        pm.mainActionTitleState.bindTo(formSaveButtonView.text())
        pm.mainActionVisibilityState.bindTo(formSaveButtonView.visibility())
        pm.formInput.bindTo(formInputView)
        pm.formSelector.bind(formVariantSelectorView, compositeUnbind)
        pm.tagSelector.bind(formTagSelectorView, compositeUnbind)
        pm.dateSelector.bind(formDateSelectorView, compositeUnbind)
        pm.timeSelector.bind(formTimeSelectorView, compositeUnbind)
        pm.noteInput.bindTo(formNoteView)
        pm.bindDateSelection()
        pm.confirmExitCommand.bindTo(exitDialog.shows())
    }

    override fun handleBack() {
        passTo(presentationModel.backHandleAction)
    }

    abstract fun getEventType(): EventType

    private fun T.bindDateSelection() {
        showDatePickerDialog.bindTo { originalDate ->
            activity.showDatePickerDialog(originalDate, maxDate = Date()) {
                dateTimeSelectedAction.consumer.accept(it)
            }
        }
        showTimePickerDialog.bindTo { originalDate ->
            activity.showTimePickerDialog(originalDate) {
                dateTimeSelectedAction.consumer.accept(it)
            }
        }
    }

    private fun observeAppBarChanges() {
        Observables.combineLatest(
            formPickerView.valueChangesFormatted(),
            appBarLayoutView.observeState())
            .filter { it.first.isNotEmpty() }
            .bindTo {
                when (it.second) {
                    AppBarState.COLLAPSED -> toolbarView.subtitle = it.first
                    else -> toolbarView.subtitle = null
                }
            }
    }
}
