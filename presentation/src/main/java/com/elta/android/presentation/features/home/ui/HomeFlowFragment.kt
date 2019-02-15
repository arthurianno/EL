package com.elta.android.presentation.features.home.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.features.home.pm.HomeFlowPm
import kotlinx.android.synthetic.main.fragment_home_flow.*

class HomeFlowFragment : BaseFlowFragment<HomeFlowPm>() {

    override val screenLayout: Int = R.layout.fragment_home_flow
    override val classToken: Class<HomeFlowPm> = HomeFlowPm::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeBottomNavigationView.select(R.id.mainMenuItemView)
        homePulseView.start()
        homeActionView.setOnClickListener {
            it.isSelected = !it.isSelected
        }
    }

    companion object {
        fun newInstance() = HomeFlowFragment()
    }
}
