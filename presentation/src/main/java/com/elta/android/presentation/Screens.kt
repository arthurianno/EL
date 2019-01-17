package com.elta.android.presentation

import android.support.v4.app.Fragment
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import com.elta.android.presentation.features.registration.flow.ui.RegistrationFlowFragment
import com.elta.android.presentation.features.registration.main.ui.RegistrationMainFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

object Screens {

    object RegistrationFlow : SupportAppScreen() {
        override fun getFragment() = RegistrationFlowFragment()
    }

    object RegistrationMain : SupportAppScreen() {
        override fun getFragment() = RegistrationMainFragment.newInstance()
    }

    object OnBoardingFlow : SupportAppScreen() {
        override fun getFragment(): Fragment = OnBoardingFragment.newInstance()
    }
}