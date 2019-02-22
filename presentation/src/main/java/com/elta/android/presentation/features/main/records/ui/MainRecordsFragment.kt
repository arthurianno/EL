package com.elta.android.presentation.features.main.records.ui

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.features.main.records.pm.MainRecordsPm
import com.elta.android.presentation.features.main.records.ui.status_bar.MainScreenLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.records.ui.status_bar.MainScreenTransparentStatusBarConfigProvider
import com.elta.android.presentation.widgets.MainScreenMarginItemDecoration
import kotlinx.android.synthetic.main.fragment_main_records.*

class MainRecordsFragment : BaseListFragment<MainRecordsPm>() {

    override val screenLayout: Int = R.layout.fragment_main_records
    override val classToken: Class<MainRecordsPm> = MainRecordsPm::class.java
    override val statusBarConfigProvider = MainScreenTransparentStatusBarConfigProvider
    override val backgroundColor: Int = R.color.pale_gray

    private val secondaryProvider = MainScreenLightStatusBarConfigProvider
    private var itemsViewScrollOffset = 0
    private val headerOffset by lazy { resources.getDimensionPixelSize(R.dimen.main_records_offset) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsView?.addItemDecoration(
            MainScreenMarginItemDecoration(
                checkNotNull(context),
                R.dimen.home_between_margin,
                R.dimen.home_between_margin,
                R.dimen.home_between_margin,
                R.dimen.overlap_first_item_margin
            )
        )

        itemsView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                itemsViewScrollOffset += dy
                setUpStatusBarConfigForOffset()
            }
        })
    }

    override fun onBindPresentationModel(pm: MainRecordsPm) {
        super.onBindPresentationModel(pm)
        pm.mainScreenState.bind(mainScreenStateView, compositeUnbind)
    }

    override fun initStatusBarConfig() {
        setUpStatusBarConfigForOffset()
    }

    private fun setUpStatusBarConfigForOffset() {
        if (itemsViewScrollOffset >= headerOffset) secondaryProvider.applyStatusBarConfig()
        else statusBarConfigProvider.applyStatusBarConfig()
    }

    companion object {
        fun newInstance() = MainRecordsFragment()
    }
}
