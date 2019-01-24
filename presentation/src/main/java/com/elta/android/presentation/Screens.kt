package com.elta.android.presentation

import android.support.v4.app.Fragment
import com.elta.android.presentation.features.auth.flow.ui.AuthFlowFragment
import com.elta.android.presentation.features.auth.login.ui.LoginFragment
import com.elta.android.presentation.features.auth.password.create.ui.AuthPasswordCreateFragment
import com.elta.android.presentation.features.auth.password.recovery.ui.AuthPasswordRecoveryFragment
import com.elta.android.presentation.features.greeting.ui.GreetingFlowFragment
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import com.elta.android.presentation.features.registration.activation.ui.ActivationFragment
import com.elta.android.presentation.features.registration.flow.ui.RegistrationFlowFragment
import com.elta.android.presentation.features.registration.main.ui.RegistrationMainFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

object Screens {

    object OnBoardingFlow : SupportAppScreen() {
        override fun getFragment(): Fragment = OnBoardingFragment.newInstance()
    }

    object GreetingFlow : SupportAppScreen() {
        override fun getFragment(): Fragment = GreetingFlowFragment.newInstance()
    }

    // REGISTRATION FLOW
    object RegistrationFlow : SupportAppScreen() {
        override fun getFragment(): Fragment = RegistrationFlowFragment.newInstance()
    }

    object RegistrationMain : SupportAppScreen() {
        override fun getFragment(): Fragment = RegistrationMainFragment.newInstance()
    }

    object ActivateProfile : SupportAppScreen() {
        override fun getFragment(): Fragment = ActivationFragment.newInstance()
    }

    // AUTH FLOW
    object AuthFlow : SupportAppScreen() {
        override fun getFragment(): Fragment = AuthFlowFragment.newInstance()
    }

    object Login : SupportAppScreen() {
        override fun getFragment(): Fragment = LoginFragment.newInstance()
    }

    object PasswordRecovery : SupportAppScreen() {
        override fun getFragment(): Fragment = AuthPasswordRecoveryFragment.newInstance()
    }

    object PasswordCreate : SupportAppScreen() {
        override fun getFragment(): Fragment = AuthPasswordCreateFragment.newInstance()
    }
}