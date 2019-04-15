package com.elta.android.presentation

import android.content.Context
import android.support.v4.app.Fragment
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.features.auth.flow.ui.AuthFlowFragment
import com.elta.android.presentation.features.auth.login.ui.LoginFragment
import com.elta.android.presentation.features.auth.password.create.ui.AuthPasswordCreateFragment
import com.elta.android.presentation.features.auth.password.recovery.ui.AuthPasswordRecoveryFragment
import com.elta.android.presentation.features.bluetooth.ui.BluetoothFragment
import com.elta.android.presentation.features.diary.flow.ui.DiaryFlowFragment
import com.elta.android.presentation.features.diary.main.ui.MainDiaryFragment
import com.elta.android.presentation.features.greeting.ui.GreetingFlowFragment
import com.elta.android.presentation.features.home.ui.HomeFlowFragment
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.ui.EventsOptionsChooserFragment
import com.elta.android.presentation.features.main.events.create.ui.EventCreationFragment
import com.elta.android.presentation.features.main.events.edit.ui.EditEventFragment
import com.elta.android.presentation.features.main.flow.ui.MainFlowFragment
import com.elta.android.presentation.features.main.records.ui.MainRecordsFragment
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import com.elta.android.presentation.features.profile.flow.ui.ProfileFlowFragment
import com.elta.android.presentation.features.profile.main.ui.MainProfileFragment
import com.elta.android.presentation.features.profile.settings.global.ui.ProfileSettingsFragment
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.RemindersFragment
import com.elta.android.presentation.features.registration.activation.ui.ActivationFragment
import com.elta.android.presentation.features.registration.confirmation.ui.EmailConfirmationFragment
import com.elta.android.presentation.features.registration.flow.ui.RegistrationFlowFragment
import com.elta.android.presentation.features.registration.main.ui.RegistrationMainFragment
import com.elta.android.presentation.features.registration.social.ui.RegistrationSocialFragment
import com.elta.android.presentation.features.shops.flow.ui.ShopsFlowFragment
import com.elta.android.presentation.features.shops.map.ui.ShopsMapFragment
import com.elta.android.presentation.features.shops.start.ui.ShopsStartFragment
import com.elta.android.presentation.features.sync.flow.ui.SyncFlowFragment
import com.elta.android.presentation.features.sync.start.ui.SyncStartFragment
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

    data class RegistrationSocial(val network: SocialNetworkType) : SupportAppScreen() {
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

    object DiaryTab : SupportAppScreen() {
        override fun getFragment() = DiaryFlowFragment.newInstance()
    }

    object ProfileTab : SupportAppScreen() {
        override fun getFragment() = ProfileFlowFragment.newInstance()
    }

    // MAIN FLOW
    object MainRecordsScreen : SupportAppScreen() {
        override fun getFragment() = MainRecordsFragment.newInstance()
    }

    data class EventsCreationScreen(val eventType: EventType) : SupportAppScreen() {
        override fun getFragment() = EventCreationFragment.newInstance(eventType)
    }

    data class EditEventScreen(val eventId: String, val eventType: EventType) : SupportAppScreen() {
        override fun getFragment() = EditEventFragment.newInstance(eventId, eventType)
    }

    data class EventsChooserScreen(val config: ChooserConfiguration) : SupportAppScreen() {
        override fun getFragment() = EventsOptionsChooserFragment.newInstance(config)
    }

    // SYNC FLOW
    object SyncFlow : SupportAppScreen() {
        override fun getFragment() = SyncFlowFragment.newInstance()
    }

    object SyncStart : SupportAppScreen() {
        override fun getFragment() = SyncStartFragment.newInstance()
    }

    object BluetoothScreen : SupportAppScreen() {
        override fun getFragment(): Fragment = BluetoothFragment.newInstance()
    }

    // DIARY FLOW
    object MainDiaryScreen : SupportAppScreen() {
        override fun getFragment() = MainDiaryFragment.newInstance()
    }

    // PROFILE FLOW
    object MainProfileScreen : SupportAppScreen() {
        override fun getFragment() = MainProfileFragment.newInstance()
    }

    object ProfileSettings : SupportAppScreen() {
        override fun getFragment() = ProfileSettingsFragment.newInstance()
    }

    object Reminders : SupportAppScreen() {
        override fun getFragment() = RemindersFragment.newInstance()
    }
}