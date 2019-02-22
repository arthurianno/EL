package com.elta.android.presentation

import android.content.Context
import android.support.v4.app.Fragment
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.presentation.features.auth.flow.ui.AuthFlowFragment
import com.elta.android.presentation.features.auth.login.ui.LoginFragment
import com.elta.android.presentation.features.auth.password.create.ui.AuthPasswordCreateFragment
import com.elta.android.presentation.features.auth.password.recovery.ui.AuthPasswordRecoveryFragment
import com.elta.android.presentation.features.greeting.ui.GreetingFlowFragment
import com.elta.android.presentation.features.home.ui.HomeFlowFragment
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.ui.EventsOptionsChooserFragment
import com.elta.android.presentation.features.main.flow.ui.MainFlowFragment
import com.elta.android.presentation.features.main.records.ui.MainRecordsFragment
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import com.elta.android.presentation.features.registration.activation.ui.ActivationFragment
import com.elta.android.presentation.features.registration.confirmation.ui.EmailConfirmationFragment
import com.elta.android.presentation.features.registration.flow.ui.RegistrationFlowFragment
import com.elta.android.presentation.features.registration.main.ui.RegistrationMainFragment
import com.elta.android.presentation.features.registration.social.ui.RegistrationSocialFragment
import com.elta.android.presentation.features.shops.flow.ui.ShopsFlowFragment
import com.elta.android.presentation.features.shops.map.ui.ShopsMapFragment
import com.elta.android.presentation.features.shops.start.ui.ShopsStartFragment
import com.elta.android.presentation.utils.navigationIntent
import com.nullgr.core.intents.callIntent
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

    data class RegistrationSocial(val network: SocialNetwork) : SupportAppScreen() {
        override fun getFragment(): Fragment = RegistrationSocialFragment.newInstance(network)
    }

    object ActivateProfile : SupportAppScreen() {
        override fun getFragment(): Fragment = ActivationFragment.newInstance()
    }

    data class EmailConfirmation(val token: String) : SupportAppScreen() {
        override fun getFragment(): Fragment = EmailConfirmationFragment.newInstance(token)
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

    data class PasswordCreate(val token: String) : SupportAppScreen() {
        override fun getFragment(): Fragment = AuthPasswordCreateFragment.newInstance(token)
    }

    // SHOPS FLOW
    object ShopsFlow : SupportAppScreen() {
        override fun getFragment() = ShopsFlowFragment.newInstance()
    }

    object ShopsStart : SupportAppScreen() {
        override fun getFragment() = ShopsStartFragment.newInstance()
    }

    object ShopsMap : SupportAppScreen() {
        override fun getFragment() = ShopsMapFragment.newInstance()
    }

    class CallScreen(private val phoneNumber: String) : SupportAppScreen() {
        override fun getActivityIntent(context: Context?) =
            callIntent(phoneNumber)
    }

    class NavigationScreen(
        private val lat: Double,
        private val lng: Double,
        private val address: String
    ) : SupportAppScreen() {
        override fun getActivityIntent(context: Context?) =
            navigationIntent(lat, lng, address)
    }

    // HOME FLOW
    object HomeFlow : SupportAppScreen() {
        override fun getFragment() = HomeFlowFragment.newInstance()
    }

    // TABS
    object MainTab : SupportAppScreen() {
        override fun getFragment() = MainFlowFragment.newInstance()
    }

    // MAIN FLOW
    object MainRecordsScreen : SupportAppScreen() {
        override fun getFragment() = MainRecordsFragment.newInstance()
    }

    data class EventsChooserScreen(val config: ChooserConfiguration) : SupportAppScreen() {
        override fun getFragment() = EventsOptionsChooserFragment.newInstance(config)
    }
}