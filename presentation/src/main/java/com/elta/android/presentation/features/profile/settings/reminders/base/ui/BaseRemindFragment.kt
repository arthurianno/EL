package com.elta.android.presentation.features.profile.settings.reminders.base.ui

import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.profile.settings.reminders.base.pm.BaseRemindPm
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.showDatePickerDialog
import com.elta.android.presentation.utils.showTimePickerDialog
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_reminder_form.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.util.Date

abstract class BaseRemindFragment<T : BaseRemindPm> : BaseFragment<T>() {

    override val screenLayout: Int = R.layout.fragment_reminder_form
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        pm.formInput.bindTo(formInputView)
        pm.dateSelector.bind(formDateSelectorView, compositeUnbind)
        pm.timeSelector.bind(formTimeSelectorView, compositeUnbind)
        pm.bindDateSelection()
        formSaveButtonView.clicks().bindTo(pm.saveReminderAction)
        pm.schedulesState.bindTo { scheduleView.attachDataList(it) }
        pm.schedulesDefaultState.bindTo { scheduleView.setTitle(it) }
        pm.saveChangesEnableState.bindTo { formSaveButtonView.isEnabled = it }
        scheduleView.spinnerClicks().bindTo(pm.selectedScheduleAction)
        pm.exitDialogControl.bindTo { data, dc ->
            MaterialDialog.Builder(checkNotNull(activity))
                .cancelable(false)
                .title(data.title)
                .content(data.message)
                .negativeText(data.negative)
                .positiveText(data.positive)
                .onPositive { _, _ -> dc.sendResult(BaseRemindPm.DialogResult.POSITIVE) }
                .onNegative { _, _ -> dc.sendResult(BaseRemindPm.DialogResult.NEGATIVE) }
                .build()
        }
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
    }

    override fun handleBack() {
        view?.hideKeyboardFun()
        passTo(presentationModel.backHandleAction)
    }

    private fun T.bindDateSelection() {
        showDatePickerDialog.bindTo { originalDate ->
            activity.showDatePickerDialog(originalDate, minDate = Date()) {
                dateTimeSelectedAction.consumer.accept(it)
            }
        }
        showTimePickerDialog.bindTo { originalDate ->
            activity.showTimePickerDialog(originalDate) {
                dateTimeSelectedAction.consumer.accept(it)
            }
        }
    }
}