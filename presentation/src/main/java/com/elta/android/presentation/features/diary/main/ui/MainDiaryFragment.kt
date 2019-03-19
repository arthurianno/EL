package com.elta.android.presentation.features.diary.main.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.diary.main.pm.MainDiaryPm
import com.elta.android.presentation.utils.showDatePickerDialog
import com.elta.android.presentation.utils.visibility
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import kotlinx.android.synthetic.main.fragment_main_diary.*
import java.util.Date

class MainDiaryFragment : BaseListFragment<MainDiaryPm>() {

    override val screenLayout: Int = R.layout.fragment_main_diary
    override val classToken: Class<MainDiaryPm> = MainDiaryPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override val backgroundColor = R.color.pale_gray

    override fun onBindPresentationModel(pm: MainDiaryPm) {
        super.onBindPresentationModel(pm)
        pm.progressState.bindTo(progressDialog.visibility(childFragmentManager))
        pm.datePickerDateState.bindTo(datePickerView.date())
        datePickerView.dateChanged().bindTo(pm.dateSelectedAction.consumer)
        selectDateButtonView.clicks().bindTo(pm.selectDateInDialogAction)
        pm.monthTitleState.bindTo(selectedMonthTitleView.text())
        pm.showDatePickerDialogCommand.bindTo { originalDate ->
            activity.showDatePickerDialog(originalDate, maxDate = Date()) {
                pm.dateInDialogSelectedAction.consumer.accept(it)
            }
        }
    }

    companion object {
        fun newInstance() = MainDiaryFragment()
    }
}
