package com.elta.android.presentation.features.onboaring.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.onboaring.pm.OnBoardingPm
import com.nullgr.core.ui.extensions.hide
import kotlinx.android.synthetic.main.fragment_onboarding.*
import kotlinx.android.synthetic.main.layout_auth_toolbar.*

class OnBoardingFragment : BaseListFragment<OnBoardingPm>() {

    override val screenLayout: Int = R.layout.fragment_onboarding
    override val classToken: Class<OnBoardingPm> = OnBoardingPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    private val snapHelper = PagerSnapHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.hide()
        menuButtonView.text = getString(R.string.on_boarding_toolbar_menu_button)
        itemsView?.let {
            snapHelper.attachToRecyclerView(it)
            indicatorsView.attachToRecyclerView(it)
        }
    }

    override fun provideLayoutManager(context: Context?): RecyclerView.LayoutManager =
        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

    companion object {
        fun newInstance() = OnBoardingFragment()
    }
}
