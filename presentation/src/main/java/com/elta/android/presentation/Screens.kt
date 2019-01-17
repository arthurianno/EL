package com.elta.android.presentation

import android.support.v4.app.Fragment
import com.elta.android.presentation.features.greeting.ui.GreetingFlowFragment
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

object Screens {

    object OnBoardingFlow : SupportAppScreen() {
        override fun getFragment(): Fragment = OnBoardingFragment.newInstance()
    }

    object GreetingFlow : SupportAppScreen() {
        override fun getFragment(): Fragment = GreetingFlowFragment.newInstance()
    }

    object RegistrationFlow: SupportAppScreen() {
        // TODO: create real registration fragment here
        override fun getFragment(): Fragment = OnBoardingFragment.newInstance()
    }

    object AuthFlow: SupportAppScreen() {
        // TODO: create real auth fragment here
        override fun getFragment(): Fragment = OnBoardingFragment.newInstance()
    }
}