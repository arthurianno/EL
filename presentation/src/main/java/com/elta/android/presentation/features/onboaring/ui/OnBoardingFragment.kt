package com.elta.android.presentation.features.onboaring.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.onboaring.pm.OnBoardingPm
import com.elta.android.presentation.utils.animateText
import com.elta.android.presentation.utils.fadeVisibility
import com.elta.android.presentation.utils.pageScrolled
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import io.reactivex.functions.Consumer
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

    override fun onBindPresentationModel(pm: OnBoardingPm) {
        super.onBindPresentationModel(pm)
        pm.currentPageState.bindTo(::bindUiForPage)
        pm.previousPageAvailableCommand.bindTo(previewPageButtonView.available())
        pm.nextPageAvailableCommand.bindTo(nextPageButtonView.available())
        itemsView?.pageScrolled()?.bindTo(pm.pageChangedAction)

        previewPageButtonView.clicks().bindTo(pm.previousPageAction)
        nextPageButtonView.clicks().bindTo(pm.nextPageAction)
        menuButtonView.clicks().bindTo(pm.nextPageAction)
    }

    private fun bindUiForPage(pageIndex: Int) {
        getPageHeader(pageIndex)?.let { onBoardingHeaderTextView.animateText(it) }
        itemsView?.smoothScrollToPosition(pageIndex)
    }

    private fun getPageHeader(pageIndex: Int): String? {
        return when (pageIndex) {
            FIRST_PAGE -> getString(R.string.on_boarding_header_user_sex)
            SECOND_PAGE -> getString(R.string.on_boarding_header_user_weight)
            THIRD_PAGE -> getString(R.string.on_boarding_header_user_diabetes_type)
            else -> null
        }
    }

    private fun TextView.available(): Consumer<Boolean> {
        return Consumer {
            this.fadeVisibility(it, View.INVISIBLE)
            when (it) {
                true -> this.isEnabled = true
                else -> postDelayed({ this.isEnabled = false }, DISABLE_DELAY)
            }
        }
    }

    companion object {
        fun newInstance() = OnBoardingFragment()
        private const val FIRST_PAGE = 0
        private const val SECOND_PAGE = 1
        private const val THIRD_PAGE = 2
        private const val DISABLE_DELAY = 300L
    }
}
