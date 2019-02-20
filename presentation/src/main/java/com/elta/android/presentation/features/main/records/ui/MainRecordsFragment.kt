package com.elta.android.presentation.features.main.records.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.features.main.records.pm.MainRecordsPm
import com.elta.android.presentation.widgets.MarginItemDecoration2
import kotlinx.android.synthetic.main.fragment_main_records.*

class MainRecordsFragment : BaseListFragment<MainRecordsPm>() {

    override val screenLayout: Int = R.layout.fragment_main_records
    override val classToken: Class<MainRecordsPm> = MainRecordsPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentStatusBarConfigProvider
    override val backgroundColor: Int = R.color.pale_gray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsView?.addItemDecoration(
            MarginItemDecoration2(
                checkNotNull(context),
                R.dimen.home_between_margin,
                R.dimen.home_between_margin,
                R.dimen.home_between_margin
            )
        )
    }

    override fun onBindPresentationModel(pm: MainRecordsPm) {
        super.onBindPresentationModel(pm)
        pm.mainScreenState.bind(mainScreenStateView, compositeUnbind)
    }

    companion object {
        fun newInstance() = MainRecordsFragment()
    }
}
