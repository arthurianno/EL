package com.elta.android.presentation.features.diary.main.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentMainDiaryBinding
import com.elta.android.presentation.features.diary.main.pm.MainDiaryPm
import com.elta.android.presentation.features.diary.main.ui.adapter.MainDiaryAdapter
import com.elta.android.presentation.features.diary.main.ui.adapter.OutlineItemDecoration
import com.elta.android.presentation.utils.showDatePickerDialog
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.ui.extensions.toggleVisibilityState
import javax.inject.Inject
import me.dmdev.rxpm.bindTo
import org.threeten.bp.LocalDate

class MainDiaryFragment @Inject constructor() :
    BaseRecyclerViewFragment<MainDiaryPm, FragmentMainDiaryBinding>(FragmentMainDiaryBinding::inflate) {
    companion object {
        fun newInstance() = MainDiaryFragment()
    }

    @Inject
    lateinit var mainDiaryAdapter: MainDiaryAdapter

    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { mainDiaryAdapter }

    override val screenLayout: Int = R.layout.fragment_main_diary
    override val classToken: Class<MainDiaryPm> = MainDiaryPm::class.java

    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentStatusBarConfigProvider

    override val backgroundColor = R.color.pale_gray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsView?.addItemDecoration(OutlineItemDecoration(requireContext()))
        (itemsView?.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        presentationModel.datePickerDateState.bindTo(binding.datePickerView.date())
    }

    override fun onBindPresentationModel(pm: MainDiaryPm) {
        super.onBindPresentationModel(pm)
        binding.datePickerView.dateChanged().bindTo(pm.dateSelectedAction)
        binding.selectDateButtonView.clicks().bindTo(pm.selectDateInDialogAction)
        pm.monthTitleState.bindTo(binding.selectedMonthTitleView.text())
        pm.showDatePickerDialogCommand.bindTo { originalDate ->
            activity.showDatePickerDialog(originalDate, maxDate = LocalDate.now()) {
                pm.dateInDialogSelectedAction.consumer.accept(it)
            }
        }
        pm.todayButtonVisibilityState.bindTo(binding.todayButtonView.visibility())
        binding.todayButtonView.clicks().bindTo(pm.todayClickedAction)
        pm.items.bindTo {
            itemsView?.toggleVisibilityState(
                state = it.isNotEmpty(),
                defaultFalseState = View.INVISIBLE
            )
        }
    }
}
