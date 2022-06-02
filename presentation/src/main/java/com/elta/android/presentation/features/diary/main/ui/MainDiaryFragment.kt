package com.elta.android.presentation.features.diary.main.ui

import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.features.diary.main.pm.MainDiaryPm
import com.elta.android.presentation.utils.showDatePickerDialog
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.ui.extensions.toggleVisibilityState
import kotlinx.android.synthetic.main.fragment_main_diary.*
import me.dmdev.rxpm.bindTo
import org.threeten.bp.LocalDate

class MainDiaryFragment : BaseListFragment<MainDiaryPm>() {

    override val screenLayout: Int = R.layout.fragment_main_diary
    override val classToken: Class<MainDiaryPm> = MainDiaryPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentStatusBarConfigProvider

    override val backgroundColor = R.color.pale_gray

    override fun onBindPresentationModel(pm: MainDiaryPm) {
        super.onBindPresentationModel(pm)
        pm.datePickerDateState.bindTo(datePickerView.date())
        datePickerView.dateChanged().bindTo(pm.dateSelectedAction)
        selectDateButtonView.clicks().bindTo(pm.selectDateInDialogAction)
        pm.monthTitleState.bindTo(selectedMonthTitleView.text())
        pm.showDatePickerDialogCommand.bindTo { originalDate ->
            activity.showDatePickerDialog(originalDate, maxDate = LocalDate.now()) {
                pm.dateInDialogSelectedAction.consumer.accept(it)
            }
        }
        pm.todayButtonVisibilityState.bindTo(todayButtonView.visibility())
        todayButtonView.clicks().bindTo(pm.todayClickedAction)
        pm.items.bindTo {
            itemsView?.toggleVisibilityState(
                it.isNotEmpty(),
                defaultFalseState = View.INVISIBLE
            )
        }
    }

    companion object {
        fun newInstance() = MainDiaryFragment()
    }
}
