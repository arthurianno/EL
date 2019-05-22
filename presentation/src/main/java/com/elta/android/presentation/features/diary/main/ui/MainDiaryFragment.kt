package com.elta.android.presentation.features.diary.main.ui

import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.features.diary.main.pm.MainDiaryPm
import com.elta.android.presentation.features.diary.main.ui.widgets.GlucoseSharingView
import com.elta.android.presentation.utils.getFileUri
import com.elta.android.presentation.utils.getInternalRootPath
import com.elta.android.presentation.utils.showDatePickerDialog
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import kotlinx.android.synthetic.main.fragment_main_diary.*
import java.util.Date

class MainDiaryFragment : BaseListFragment<MainDiaryPm>() {

    override val screenLayout: Int = R.layout.fragment_main_diary
    override val classToken: Class<MainDiaryPm> = MainDiaryPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentStatusBarConfigProvider

    override val backgroundColor = R.color.pale_gray

    override fun onBindPresentationModel(pm: MainDiaryPm) {
        super.onBindPresentationModel(pm)
        pm.datePickerDateState.bindTo(datePickerView.date())
        datePickerView.dateChanged().bindTo(pm.dateSelectedAction)
        selectDateButtonView.clicks().bindTo(pm.selectDateInDialogAction)
        pm.monthTitleState.bindTo(selectedMonthTitleView.text())
        pm.showDatePickerDialogCommand.bindTo { originalDate ->
            activity.showDatePickerDialog(originalDate, maxDate = Date()) {
                pm.dateInDialogSelectedAction.consumer.accept(it)
            }
        }
        pm.todayButtonVisibilityState.bindTo(todayButtonView.visibility())
        todayButtonView.clicks().bindTo(pm.todayClickedAction)
        bindProgressDialog(pm)

        // todo move to glucose screen
        pm.createBitmapCommand.bindTo {
            val sharingView = GlucoseSharingView(requireActivity())
            val bitmap = sharingView.generateBitmap("2,4", GlucoseLevel.LOW)
            Triple(
                "mockedEventHash",
                requireContext().getInternalRootPath(),
                bitmap
            ).passTo(pm.bitmapEventAction)
        }
        pm.getBitmapPathCommand.bindTo {
            requireContext().getFileUri(it).passTo(pm.shareEventAction)
        }
    }

    companion object {
        fun newInstance() = MainDiaryFragment()
    }
}
