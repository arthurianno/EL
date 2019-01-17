package com.elta.android.presentation.features.onboaring.ui;

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.onboaring.pm.OnBoardingPm

class OnBoardingFragment : BaseFragment<OnBoardingPm>() {

    override val screenLayout: Int = R.layout.fragment_onboarding
    override val classToken: Class<OnBoardingPm> = OnBoardingPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    companion object {
        fun newInstance(): OnBoardingFragment {
            return OnBoardingFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
