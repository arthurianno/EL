package com.elta.android.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration
import androidx.fragment.app.Fragment
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.emias.model.Emias
import com.elta.android.domain.features.emias.model.EmiasStatus
import com.elta.android.domain.features.sale_points.model.Type
import com.elta.android.presentation.core.navigation.support.SupportAppScreen
import com.elta.android.presentation.features.auth.flow.ui.AuthFlowFragment
import com.elta.android.presentation.features.auth.login.ui.LoginFragment
import com.elta.android.presentation.features.auth.login.ui.LoginFragmentVariantA
import com.elta.android.presentation.features.auth.password.create.ui.AuthPasswordCreateFragment
import com.elta.android.presentation.features.auth.password.recovery.ui.AuthPasswordRecoveryFragment
import com.elta.android.presentation.features.calcutator.custom.CreateCustomProductFragment
import com.elta.android.presentation.features.calcutator.custom.CustomProductsFragment
import com.elta.android.presentation.features.calcutator.products.CalculatorFragment
import com.elta.android.presentation.features.calcutator.products.DishDetailFragment
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.consultant.ui.ConsultantFragment
import com.elta.android.presentation.features.devices.all.ui.DevicesFragment
import com.elta.android.presentation.features.devices.firmware.ui.FirmwareFragment
import com.elta.android.presentation.features.devices.firmware.ui.FirmwareFragmentVariantA
import com.elta.android.presentation.features.devices.info.ui.DeviceInfoFragment
import com.elta.android.presentation.features.devices.search.GlucometerSearchFragment
import com.elta.android.presentation.features.diary.flow.ui.DiaryFlowFragment
import com.elta.android.presentation.features.diary.main.ui.MainDiaryFragment
import com.elta.android.presentation.features.feedback.ui.FeedbackFragment
import com.elta.android.presentation.features.greeting.ui.GreetingFlowFragment
import com.elta.android.presentation.features.home.ui.HomeFlowFragment
import com.elta.android.presentation.features.home.ui.HomeFlowFragmentVariantA
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.ui.EventsOptionsChooserFragment
import com.elta.android.presentation.features.main.events.create.ui.EventCreationFragment
import com.elta.android.presentation.features.main.events.edit.ui.EditEventFragment
import com.elta.android.presentation.features.main.events.glucose.ui.GlucoseEventFragment
import com.elta.android.presentation.features.main.events.selector.ui.EventSelectorFragment
import com.elta.android.presentation.features.main.flow.ui.MainFlowFragment
import com.elta.android.presentation.features.main.records.ui.MainRecordsFragment
import com.elta.android.presentation.features.observers.all.ui.ObserversFragment
import com.elta.android.presentation.features.observers.edit.ui.EditObserverFragment
import com.elta.android.presentation.features.observers.invite.ui.InviteObserverFragment
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import com.elta.android.presentation.features.profile.flow.ui.ProfileFlowFragment
import com.elta.android.presentation.features.profile.main.ui.MainProfileFragment
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui.GlucoseSettingFragment
import com.elta.android.presentation.features.profile.settings.emias.ui.EmiasProfileFragment
import com.elta.android.presentation.features.profile.settings.gender.ui.ProfileSetGenderFragment
import com.elta.android.presentation.features.profile.settings.global.ui.ProfileSettingsFragment
import com.elta.android.presentation.features.profile.settings.glucoseformat.GlucoseFormatFragment
import com.elta.android.presentation.features.profile.settings.name.ui.ProfileSetNameFragment
import com.elta.android.presentation.features.profile.settings.password.ui.ProfileChangePasswordFragment
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.RemindersFragment
import com.elta.android.presentation.features.profile.settings.reminders.create.ui.CreateRemindFragment
import com.elta.android.presentation.features.profile.settings.reminders.edit.ui.EditRemindFragment
import com.elta.android.presentation.features.profile.support.ui.SupportFragment
import com.elta.android.presentation.features.registration.activation.ui.ActivationFragment
import com.elta.android.presentation.features.registration.confirmation.ui.EmailConfirmationFragment
import com.elta.android.presentation.features.registration.flow.ui.RegistrationFlowFragment
import com.elta.android.presentation.features.registration.main.ui.RegistrationMainFragment
import com.elta.android.presentation.features.registration.main.variantA.ui.RegistrationMainFragmentVariantA
import com.elta.android.presentation.features.shops.flow.ui.ShopsFlowFragment
import com.elta.android.presentation.features.shops.map.ui.ShopsMapFragment
import com.elta.android.presentation.features.shops.start.ui.ShopsStartFragment
import com.elta.android.presentation.features.statistic.flow.ui.StatisticFlowFragment
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.features.statistic.period.ui.PeriodFragment
import com.elta.android.presentation.features.sync.connect.ConnectHelpFragment
import com.elta.android.presentation.features.sync.connect.ConnectStartFragment
import com.elta.android.presentation.features.sync.connect.ConnectTypeFragment
import com.elta.android.presentation.features.sync.connect.ConnectingFragment
import com.elta.android.presentation.features.sync.connect.ConnectingFragmentVariantA
import com.elta.android.presentation.features.sync.connect.HowToConnectFragment
import com.elta.android.presentation.features.sync.connect.HowToConnectFragmentVariantA
import com.elta.android.presentation.features.sync.connect.ScannerDmcFragment
import com.elta.android.presentation.features.sync.connect.ScannerDmcFragmentVariantA
import com.elta.android.presentation.features.sync.connect.onboarding.ui.FromOnBoardingConnectDeviceByPinFragment
import com.elta.android.presentation.features.sync.connect.onboarding.ui.FromOnBoardingConnectDeviceByPinFragmentVariantA
import com.elta.android.presentation.features.sync.connect.other.ui.FromOtherConnectDeviceByPinFragment
import com.elta.android.presentation.features.sync.connect.other.ui.FromOtherConnectDeviceByPinFragmentVariantA
import com.elta.android.presentation.features.sync.flow.onboarding.ui.FromOnBoardingSyncFlowFragment
import com.elta.android.presentation.features.sync.flow.other.ui.FromOtherSyncFlowFragment
import com.elta.android.presentation.features.sync.start.onboarding.ui.FromOnBoardingSyncStartFragment
import com.elta.android.presentation.features.sync.start.other.ui.FromOtherSyncStartFragment
import com.elta.android.presentation.features.version.mandatory.ui.MandatoryUpdateFragment
import com.elta.android.presentation.utils.mp4ActionIntent
import com.elta.android.presentation.utils.pdfActionIntent
import com.elta.android.presentation.utils.shareIntent
import com.nullgr.core.intents.callIntent
import com.nullgr.core.intents.emailIntent
import com.nullgr.core.intents.telegramIntent
import com.nullgr.core.intents.viberIntent
import com.nullgr.core.intents.webIntent
import com.nullgr.core.intents.whatsAppIntent
import org.threeten.bp.LocalDate

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

    object RegistrationMainVariantA : SupportAppScreen() {
        override fun getFragment(): Fragment = RegistrationMainFragmentVariantA.newInstance()
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

    // fixme Variant A : recovery_account
    object LoginVariantA : SupportAppScreen() {
        override fun getFragment(): Fragment = LoginFragmentVariantA.newInstance()
    }

    object PasswordRecovery : SupportAppScreen() {
        override fun getFragment(): Fragment = AuthPasswordRecoveryFragment.newInstance()
    }

    data class PasswordCreate(val token: String) : SupportAppScreen() {
        override fun getFragment(): Fragment = AuthPasswordCreateFragment.newInstance(token)
    }

    // SHOPS FLOW
    // todo: SalepointHide
    @Deprecated("Скрываем функциал карт до лучших времён")
    object ShopsFlow : SupportAppScreen() {
        override fun getFragment() = ShopsFlowFragment.newInstance()
    }

    // todo: SalepointHide
    @Deprecated("Скрываем функциал карт до лучших времён")
    object ShopsStart : SupportAppScreen() {
        override fun getFragment() = ShopsStartFragment.newInstance()
    }

    // todo: SalepointHide
    @Deprecated("Скрываем функциал карт до лучших времён")
    data class ShopsMap(val isOnBoarding: Boolean = false) : SupportAppScreen() {
        override fun getFragment() = ShopsMapFragment.newInstance(
            Type.SALE,
            isOnboarding = isOnBoarding
        )
    }

    // todo: SalepointHide
    @Deprecated("Скрываем функциал карт до лучших времён")
    object ServiceCentersMap : SupportAppScreen() {
        override fun getFragment() = ShopsMapFragment.newInstance(Type.SERVICE)
    }

    class CallScreen(private val phoneNumber: String) : SupportAppScreen() {
        override fun getActivityIntent(context: Context) =
            callIntent(phoneNumber)
    }

    // HOME FLOW
    object HomeFlow : SupportAppScreen() {
        override fun getFragment() = HomeFlowFragment.newInstance()
    }

    // fixme Variant A : improved_enabling_location
    object HomeFlowVariantA : SupportAppScreen() {
        override fun getFragment() = HomeFlowFragmentVariantA.newInstance()
    }

    // TABS
    object MainTab : SupportAppScreen() {
        override fun getFragment() = MainFlowFragment.newInstance()
    }

    object StatisticTab : SupportAppScreen() {
        override fun getFragment() = StatisticFlowFragment.newInstance()
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
        override fun getFragment() = when {
            eventType is EventType.Glucose && eventType.inputType == GlucoseInputType.AUTO -> {
                GlucoseEventFragment.newInstance(eventId)
            }

            else -> EditEventFragment.newInstance(eventId, eventType)
        }
    }

    data class EventsChooserScreen(val config: ChooserConfiguration) : SupportAppScreen() {
        override fun getFragment() = EventsOptionsChooserFragment.newInstance(config)
    }

    data class EventSelectorScreen(val config: ChooserConfiguration) : SupportAppScreen() {
        override fun getFragment() = EventSelectorFragment.newInstance(config)
    }

    data class ShareEventScreen(val uri: Uri, val title: String) : SupportAppScreen() {
        override fun getActivityIntent(context: Context) = shareIntent(uri, title)
    }

    // SYNC FLOW
    object FromOtherSyncFlow : SupportAppScreen() {
        override fun getFragment() = FromOtherSyncFlowFragment.newInstance()
    }

    object FromOnBoardingSyncFlow : SupportAppScreen() {
        override fun getFragment() = FromOnBoardingSyncFlowFragment.newInstance()
    }

    object FromOtherSyncStart : SupportAppScreen() {
        override fun getFragment() = FromOtherSyncStartFragment.newInstance()
    }

    object FromOnBoardingSyncStart : SupportAppScreen() {
        override fun getFragment() = FromOnBoardingSyncStartFragment.newInstance()
    }

    object FromOnBoardingConnectDeviceByPin : SupportAppScreen() {
        override fun getFragment(): Fragment =
            FromOnBoardingConnectDeviceByPinFragment.newInstance()
    }

    object FromOnBoardingConnectDeviceByPinVariantA : SupportAppScreen() {
        override fun getFragment(): Fragment =
            FromOnBoardingConnectDeviceByPinFragmentVariantA.newInstance()
    }

    object FromOtherConnectDeviceByPin : SupportAppScreen() {
        override fun getFragment(): Fragment = FromOtherConnectDeviceByPinFragment.newInstance()
    }

    object FromOtherConnectDeviceByPinVariantA : SupportAppScreen() {
        override fun getFragment(): Fragment = FromOtherConnectDeviceByPinFragmentVariantA.newInstance()
    }

    data class ConnectStartScreen(val isOnBoarding: Boolean) : SupportAppScreen() {
        override fun getFragment() = ConnectStartFragment.newInstance(isOnBoarding)
    }

    data class ConnectTypeScreen(val isOnBoarding: Boolean) : SupportAppScreen() {
        override fun getFragment() = ConnectTypeFragment.newInstance(isOnBoarding)
    }

    object ConnectHelpScreen : SupportAppScreen() {
        override fun getFragment() = ConnectHelpFragment()
    }

    data class HowToConnectScreen(val isOnBoarding: Boolean) : SupportAppScreen() {
        override fun getFragment() = HowToConnectFragment.newInstance(isOnBoarding)
    }

    data class HowToConnectScreenVariantA(val isOnBoarding: Boolean) : SupportAppScreen() {
        override fun getFragment() = HowToConnectFragmentVariantA.newInstance(isOnBoarding)
    }

    @ExperimentalCameraProviderConfiguration
    data class ScannerDmcScreen(val isOnBoarding: Boolean) : SupportAppScreen() {
        override fun getFragment() = ScannerDmcFragment.newInstance(isOnBoarding)
    }

    @ExperimentalCameraProviderConfiguration
    data class ScannerDmcScreenVariantA(val isOnBoarding: Boolean) : SupportAppScreen() {
        override fun getFragment() = ScannerDmcFragmentVariantA.newInstance(isOnBoarding)
    }

    data class ConnectingScreen(
        val isOnBoarding: Boolean,
        val pin: String,
        val name: String
    ) : SupportAppScreen() {
        override fun getFragment() = ConnectingFragment.newInstance(isOnBoarding, pin, name)
    }

    data class ConnectingScreenVariantA(
        val isOnBoarding: Boolean,
        val pin: String,
        val name: String
    ) : SupportAppScreen() {
        override fun getFragment() = ConnectingFragmentVariantA.newInstance(isOnBoarding, pin, name)
    }

    // DIARY FLOW
    object MainDiaryScreen : SupportAppScreen() {
        override fun getFragment() = MainDiaryFragment.newInstance()
    }

    // PROFILE FLOW
    object MainProfileScreen : SupportAppScreen() {
        override fun getFragment() = MainProfileFragment.newInstance()
    }

    object GlucoseSettingScreen : SupportAppScreen() {
        override fun getFragment(): Fragment = GlucoseSettingFragment.newInstance()
    }

    object ProfileSettings : SupportAppScreen() {
        override fun getFragment() = ProfileSettingsFragment.newInstance()
    }

    object Observers : SupportAppScreen() {
        override fun getFragment() = ObserversFragment.newInstance()
    }

    data class EditObserver(val id: String) : SupportAppScreen() {
        override fun getFragment() = EditObserverFragment.newInstance(id)
    }

    object InviteObserver : SupportAppScreen() {
        override fun getFragment() = InviteObserverFragment.newInstance()
    }

    object Reminders : SupportAppScreen() {
        override fun getFragment() = RemindersFragment()
    }

    object GlucoseFormat : SupportAppScreen() {
        override fun getFragment() = GlucoseFormatFragment()
    }

    object Devices : SupportAppScreen() {
        override fun getFragment() = DevicesFragment.newInstance()
    }

    class DeviceSearch(val address: String) : SupportAppScreen() {
        override fun getFragment() = GlucometerSearchFragment.newInstance(address)
    }

    data class DeviceInfo(val name: String, val address: String) : SupportAppScreen() {
        override fun getFragment() = DeviceInfoFragment.newInstance(name, address)
    }

    data class UpdateFirmware(val address: String) : SupportAppScreen() {
        override fun getFragment() = FirmwareFragment.newInstance(address)
    }

    // fixme Variant A : improved_enabling_location

    data class UpdateFirmwareVariantA(val address: String) : SupportAppScreen() {
        override fun getFragment() = FirmwareFragmentVariantA.newInstance(address)
    }

    object CreateRemind : SupportAppScreen() {
        override fun getFragment() = CreateRemindFragment.newInstance()
    }

    data class EditRemind(val reminderId: String) : SupportAppScreen() {
        override fun getFragment() = EditRemindFragment.newInstance(reminderId)
    }

    object SetName : SupportAppScreen() {
        override fun getFragment() = ProfileSetNameFragment.newInstance()
    }

    object ChangePassword : SupportAppScreen() {
        override fun getFragment() = ProfileChangePasswordFragment.newInstance()
    }

    object SetGender : SupportAppScreen() {
        override fun getFragment() = ProfileSetGenderFragment.newInstance()
    }

    data class EmiasProfile(
        val linkedStatus: EmiasStatus,
        val emias: Emias?,
        val birthDateFromProfile: LocalDate?
    ) : SupportAppScreen() {
        override fun getFragment() = EmiasProfileFragment.newInstance(
            linkedStatus = linkedStatus,
            emias = emias,
            birthDateFromProfile = birthDateFromProfile
        )
    }

    object Support : SupportAppScreen() {
        override fun getFragment() = SupportFragment.newInstance()
    }

    data class ConsultantScreen(val userId: String, val userName: String) : SupportAppScreen() {
        override fun getFragment(): Fragment =
            ConsultantFragment.newInstance(userId = userId, userName = userName)
    }

    data class ViewVideoScreen(val uri: Uri) : SupportAppScreen() {
        override fun getActivityIntent(context: Context): Intent =
            mp4ActionIntent(uri, context)
    }

    data class EmailScreen(
        val email: String,
        val subject: String? = null,
        val body: String? = null
    ) : SupportAppScreen() {
        override fun getActivityIntent(context: Context): Intent =
            emailIntent(to = email, subject = subject, body = body)
    }

    data class WhatsAppScreen(val number: String) : SupportAppScreen() {
        override fun getActivityIntent(context: Context): Intent =
            whatsAppIntent(number)
    }

    data class TelegramScreen(val number: String) : SupportAppScreen() {
        override fun getActivityIntent(context: Context): Intent =
            telegramIntent(number)
    }

    data class ViberScreen(val number: String) : SupportAppScreen() {
        override fun getActivityIntent(context: Context): Intent =
            viberIntent(number)
    }

    // STATISTICS FLOW
    data class PeriodScreen(val period: Period) : SupportAppScreen() {
        override fun getFragment() = PeriodFragment.newInstance(period)
        override val screenKey: String
            get() {
                val superKey = super.screenKey
                return "$superKey-${period.name}"
            }
    }

    data class ViewPdfScreen(val uri: Uri) : SupportAppScreen() {
        override fun getActivityIntent(context: Context): Intent =
            pdfActionIntent(uri, context)
    }

    // FEEDBACK
    object Feedback : SupportAppScreen() {
        override fun getFragment() = FeedbackFragment.newInstance()
    }

    object PlayMarketScreen : SupportAppScreen() {
        override fun getActivityIntent(context: Context) =
            webIntent("market://details?id=com.elta.android")
    }

    data class CalculatorScreen(val calculatorFlow: CalculatorFlow) : SupportAppScreen() {
        override fun getFragment() = CalculatorFragment.newInstance(calculatorFlow)
    }

    data class CustomProductsScreen(val calculatorFlow: CalculatorFlow) : SupportAppScreen() {
        override fun getFragment() = CustomProductsFragment.newInstance(calculatorFlow)
    }

    data class CreateCustomProductScreen(
        val dish: DishUiEntity? = null,
        val productName: String? = null,
        val calculatorFlow: CalculatorFlow,
    ) : SupportAppScreen() {
        override fun getFragment() =
            CreateCustomProductFragment.newInstance(dish, productName, calculatorFlow)
    }

    data class AddDishScreen(val dish: DishUiEntity, val calculatorFlow: CalculatorFlow) :
        SupportAppScreen() {
        override fun getFragment() = DishDetailFragment.newInstance(dish, calculatorFlow)
    }

    // UPDATE APP FLOW
    object ForcedUpdateScreen : SupportAppScreen() {
        override fun getFragment() = MandatoryUpdateFragment.newInstance()
    }
}
