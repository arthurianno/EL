package com.elta.android.presentation.features.home.ui

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.features.home.pm.HomeFlowPm
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import kotlinx.android.synthetic.main.fragment_home_flow.*
import kotlinx.android.synthetic.main.layout_home_bottom_sheet.*
import javax.inject.Inject

class HomeFlowFragment : BaseFlowFragment<HomeFlowPm>() {

    override val screenLayout: Int = R.layout.fragment_home_flow
    override val classToken: Class<HomeFlowPm> = HomeFlowPm::class.java

    @Inject
    lateinit var adapter: DynamicAdapter

    @Inject
    lateinit var bus: RxBus

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBottomSheetItemsView()
        homeBottomNavigationView.select(R.id.mainMenuItemView)
        homeActionView.setOnClickListener {
            if (!it.isSelected) {
                homeBottomSheetView.show()
            } else {
                homeBottomSheetView.hide()
            }
        }
    }

    override fun onBindPresentationModel(pm: HomeFlowPm) {
        super.onBindPresentationModel(pm)
        pm.bottomSheetItems.bindTo { items -> adapter.updateData(items) }
        pm.closeBottomSheetCommand.bindTo { homeBottomSheetView.hide() }
        pm.pulseCommand.bindTo {
            if (it) {
                homePulseView.show()
                homePulseView.start()
            } else {
                homePulseView.stop()
                homePulseView.hide()
            }
        }
        homeBottomSheetView.visibilityChanges().bindTo { visible ->
            homeActionView.isSelected = visible
            bus.event(Events.HomeBottomSheetStateChanged(visible))
        }
    }

    override fun handleBack() {
        if (!homeBottomSheetView.handleBack()) {
            super.handleBack()
        }
    }

    private fun initBottomSheetItemsView() {
        bottomSheetItemsView.layoutManager = LinearLayoutManager(activity)
        bottomSheetItemsView.adapter = adapter
    }

    companion object {
        fun newInstance() = HomeFlowFragment()
    }
}
