package com.elta.android.presentation.features.profile.settings.reminders.base.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentReminderFormBinding
import com.elta.android.presentation.features.profile.settings.reminders.base.pm.BaseRemindPm
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.showDatePickerDialog
import com.elta.android.presentation.utils.showTimePickerWithoutPastTimeDialog
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.toast.showToast
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import me.dmdev.rxpm.widget.bindTo
import org.threeten.bp.ZonedDateTime

abstract class BaseRemindFragment<T : BaseRemindPm> :
    BaseFragment<T, FragmentReminderFormBinding>(FragmentReminderFormBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_reminder_form
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        pm.formInput.bindTo(binding.formInputView)
        pm.dateSelector.bind(binding.formDateSelectorView, compositeUnbind)
        pm.timeSelector.bind(binding.formTimeSelectorView, compositeUnbind)
        pm.bindDateSelection()
        binding.formSaveButtonView.clicks().bindTo(pm.saveReminderAction)
        pm.schedulesState.bindTo { binding.scheduleView.attachDataList(it) }
        pm.schedulesDefaultState.bindTo { binding.scheduleView.setTitle(it) }
        pm.saveChangesEnableState.bindTo { binding.formSaveButtonView.isEnabled = it }
        binding.scheduleView.spinnerClicks().bindTo(pm.selectedScheduleAction)
        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
        pm.showExistingReminderDialog.bindTo { activity.showToast(getString(R.string.reminder_is_exists)) }
    }

    override fun handleBack() {
        view?.hideKeyboardFun()?.passTo(presentationModel.backHandleAction)
    }

    private fun T.bindDateSelection() {
        showDatePickerDialog.bindTo { originalDate ->
            activity.showDatePickerDialog(originalDate, minDate = ZonedDateTime.now()) {
                dateTimeSelectedAction.consumer.accept(it)
            }
        }
        showTimePickerDialog.bindTo { originalDate ->
            activity.showTimePickerWithoutPastTimeDialog(originalDate) {
                dateTimeSelectedAction.consumer.accept(it)
            }
        }
    }
}
