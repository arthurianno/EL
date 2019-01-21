package com.elta.android.presentation

import android.support.v4.app.Fragment
import com.elta.android.presentation.features.greeting.ui.GreetingFlowFragment
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import com.elta.android.presentation.features.registration.flow.ui.RegistrationFlowFragment
import com.elta.android.presentation.features.registration.main.ui.RegistrationMainFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

@Suppress("ForbiddenComment")
object Screens {

    object OnBoardingFlow : SupportAppScreen() {
        override fun getFragment(): Fragment = OnBoardingFragment.newInstance()
    }

    object GreetingFlow : SupportAppScreen() {
        override fun getFragment(): Fragment = GreetingFlowFragment.newInstance()
    }

    object RegistrationFlow : SupportAppScreen() {
        override fun getFragment() = RegistrationFlowFragment.newInstance()
    }

    object RegistrationMain : SupportAppScreen() {
        override fun getFragment() = RegistrationMainFragment.newInstance()
    }

    object AuthFlow : SupportAppScreen() {
        // TODO: create real auth fragment here
        override fun getFragment(): Fragment = OnBoardingFragment.newInstance()
    }
}