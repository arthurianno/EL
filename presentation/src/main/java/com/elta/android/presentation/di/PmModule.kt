package com.elta.android.presentation.di

import com.elta.android.presentation.core.pm.PmKey
import com.elta.android.presentation.core.pm.factory.GeneralPmFactory
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.features.app.pm.AppPm
import com.elta.android.presentation.features.auth.flow.pm.AuthFlowPm
import com.elta.android.presentation.features.auth.login.pm.LoginPm
import com.elta.android.presentation.features.auth.password.create.pm.AuthPasswordCreatePm
import com.elta.android.presentation.features.auth.password.recovery.pm.AuthPasswordRecoveryPm
import com.elta.android.presentation.features.bluetooth.pm.BluetoothPm
import com.elta.android.presentation.features.devices.all.pm.DevicesPm
import com.elta.android.presentation.features.devices.firmware.pm.FirmwarePm
import com.elta.android.presentation.features.devices.info.pm.DeviceInfoPm
import com.elta.android.presentation.features.diary.flow.pm.DiaryFlowPm
import com.elta.android.presentation.features.diary.main.pm.MainDiaryPm
import com.elta.android.presentation.features.feedback.pm.FeedbackPm
import com.elta.android.presentation.features.greeting.pm.GreetingPm
import com.elta.android.presentation.features.home.pm.HomeFlowPm
import com.elta.android.presentation.features.main.events.chooser.pm.EventsOptionsChooserPm
import com.elta.android.presentation.features.main.events.create.pm.EventCreationPm
import com.elta.android.presentation.features.main.events.edit.pm.EditEventPm
import com.elta.android.presentation.features.main.events.glucose.pm.GlucoseEventPm
import com.elta.android.presentation.features.main.flow.pm.MainFlowPm
import com.elta.android.presentation.features.main.records.pm.MainRecordsPm
import com.elta.android.presentation.features.observers.all.pm.ObserversPm
import com.elta.android.presentation.features.observers.edit.pm.EditObserverPm
import com.elta.android.presentation.features.observers.invite.pm.InviteObserverPm
import com.elta.android.presentation.features.onboaring.pm.OnBoardingPm
import com.elta.android.presentation.features.profile.flow.pm.ProfileFlowPm
import com.elta.android.presentation.features.profile.main.pm.MainProfilePm
import com.elta.android.presentation.features.profile.settings.dialogs.diabetes.pm.DiabetesSettingDialogPm
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.pm.GlucoseRangeDialogPm
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.pm.HemoglobinSettingsPm
import com.elta.android.presentation.features.profile.settings.gender.pm.ProfileSetGenderPm
import com.elta.android.presentation.features.profile.settings.global.pm.ProfileSettingsPm
import com.elta.android.presentation.features.profile.settings.name.pm.ProfileSetNamePm
import com.elta.android.presentation.features.profile.settings.password.pm.ProfileChangePasswordPm
import com.elta.android.presentation.features.profile.settings.reminders.all.pm.RemindersPm
import com.elta.android.presentation.features.profile.settings.reminders.create.pm.CreateRemindPm
import com.elta.android.presentation.features.profile.settings.reminders.edit.pm.EditRemindPm
import com.elta.android.presentation.features.profile.support.pm.SupportPm
import com.elta.android.presentation.features.registration.activation.pm.ActivationPm
import com.elta.android.presentation.features.registration.confirmation.pm.EmailConfirmationPm
import com.elta.android.presentation.features.registration.flow.pm.RegistrationFlowPm
import com.elta.android.presentation.features.registration.main.pm.RegistrationMainPm
import com.elta.android.presentation.features.registration.policy.pm.RegistrationPrivacyPolicyPm
import com.elta.android.presentation.features.registration.social.pm.RegistrationSocialPm
import com.elta.android.presentation.features.shops.flow.pm.ShopsFlowPm
import com.elta.android.presentation.features.shops.map.pm.ShopsMapPm
import com.elta.android.presentation.features.shops.start.pm.ShopsStartPm
import com.elta.android.presentation.features.statistic.flow.pm.StatisticFlowPm
import com.elta.android.presentation.features.statistic.period.pm.PeriodPm
import com.elta.android.presentation.features.sync.connect.onboarding.pm.FromOnBoardingConnectDevicePm
import com.elta.android.presentation.features.sync.connect.other.pm.FromOtherConnectDevicePm
import com.elta.android.presentation.features.sync.flow.onboarding.pm.FromOnBoardingSyncFlowPm
import com.elta.android.presentation.features.sync.flow.other.pm.FromOtherSyncFlowPm
import com.elta.android.presentation.features.sync.pin.pm.PinDialogPm
import com.elta.android.presentation.features.sync.start.onboarding.pm.FromOnBoardingSyncStartPm
import com.elta.android.presentation.features.sync.start.other.pm.FromOtherSyncStartPm
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import me.dmdev.rxpm.PresentationModel

@Suppress("TooManyFunctions", "UnnecessaryAbstractClass")
@Module
abstract class PmModule {

    @Binds
    abstract fun viewModelFactory(factory: GeneralPmFactory): PmFactory

    @Binds
    @IntoMap
    @PmKey(AppPm::class)
    abstract fun bindAppPm(pm: AppPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(OnBoardingPm::class)
    abstract fun bindOnBoardingPm(pm: OnBoardingPm): PresentationModel

    // REGISTRATION FLOW
    @Binds
    @IntoMap
    @PmKey(RegistrationFlowPm::class)
    abstract fun bindRegistrationFlowPm(pm: RegistrationFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(RegistrationMainPm::class)
    abstract fun bindRegistrationMainPm(pm: RegistrationMainPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(RegistrationSocialPm::class)
    abstract fun bindRegistrationSocialPm(pm: RegistrationSocialPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(GreetingPm::class)
    abstract fun bindGreetingPm(pm: GreetingPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(RegistrationPrivacyPolicyPm::class)
    abstract fun bindRegistrationPrivacyPolicyPm(pm: RegistrationPrivacyPolicyPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(ActivationPm::class)
    abstract fun bindActivationPm(pm: ActivationPm): PresentationModel

    // AUTH FLOW
    @Binds
    @IntoMap
    @PmKey(AuthFlowPm::class)
    abstract fun bindAuthFlowPm(pm: AuthFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(LoginPm::class)
    abstract fun bindLoginPm(pm: LoginPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(AuthPasswordRecoveryPm::class)
    abstract fun bindAuthPasswordRecoveryPm(pm: AuthPasswordRecoveryPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(AuthPasswordCreatePm::class)
    abstract fun bindAuthPasswordCreatePm(pm: AuthPasswordCreatePm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(EmailConfirmationPm::class)
    abstract fun bindEmailConfirmationPm(pm: EmailConfirmationPm): PresentationModel

    // SHOPS FLOW
    @Binds
    @IntoMap
    @PmKey(ShopsFlowPm::class)
    abstract fun bindShopsFlowPm(pm: ShopsFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(ShopsStartPm::class)
    abstract fun bindShopsStartPm(pm: ShopsStartPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(ShopsMapPm::class)
    abstract fun bindShopsMapPm(pm: ShopsMapPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(HomeFlowPm::class)
    abstract fun bindHomeFlowPm(pm: HomeFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(MainFlowPm::class)
    abstract fun bindMainFlowPm(pm: MainFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(MainRecordsPm::class)
    abstract fun bindMainRecordsPm(pm: MainRecordsPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(EventCreationPm::class)
    abstract fun bindEventCreationPm(pm: EventCreationPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(EventsOptionsChooserPm::class)
    abstract fun bindEventsOptionsChooserPm(pm: EventsOptionsChooserPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(EditEventPm::class)
    abstract fun bindEditEventPm(pm: EditEventPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(GlucoseEventPm::class)
    abstract fun bindGlucoseEventPm(pm: GlucoseEventPm): PresentationModel

    // SYNC FLOW
    @Binds
    @IntoMap
    @PmKey(FromOnBoardingSyncFlowPm::class)
    abstract fun bindFromOnBoardingSyncFlowPm(pm: FromOnBoardingSyncFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(FromOtherSyncFlowPm::class)
    abstract fun bindFromOtherSyncFlowPm(pm: FromOtherSyncFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(FromOnBoardingSyncStartPm::class)
    abstract fun bindFromOnBoardingSyncStartPm(pm: FromOnBoardingSyncStartPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(FromOtherSyncStartPm::class)
    abstract fun bindFromOtherSyncStartPm(pm: FromOtherSyncStartPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(BluetoothPm::class)
    abstract fun bindBluetoothPm(pm: BluetoothPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(PinDialogPm::class)
    abstract fun bindPinDialogPm(pm: PinDialogPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(FromOnBoardingConnectDevicePm::class)
    abstract fun bindFromOnBoardingConnectDevicePm(pm: FromOnBoardingConnectDevicePm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(FromOtherConnectDevicePm::class)
    abstract fun bindFromOtherConnectDevicePm(pm: FromOtherConnectDevicePm): PresentationModel

    // DIARY FLOW
    @Binds
    @IntoMap
    @PmKey(DiaryFlowPm::class)
    abstract fun bindDiaryFlowPm(pm: DiaryFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(MainDiaryPm::class)
    abstract fun bindMainDiaryPm(pm: MainDiaryPm): PresentationModel

    // PROFILE FLOW
    @Binds
    @IntoMap
    @PmKey(ProfileFlowPm::class)
    abstract fun bindProfileFlowPm(pm: ProfileFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(MainProfilePm::class)
    abstract fun bindMainProfilePm(pm: MainProfilePm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(ObserversPm::class)
    abstract fun bindObserversPm(pm: ObserversPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(InviteObserverPm::class)
    abstract fun bindInviteObserverPm(pm: InviteObserverPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(EditObserverPm::class)
    abstract fun bindEditObserverPm(pm: EditObserverPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(ProfileSettingsPm::class)
    abstract fun bindProfileSettingsPm(pm: ProfileSettingsPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(DiabetesSettingDialogPm::class)
    abstract fun bindDiabetesSettingDialogPm(pm: DiabetesSettingDialogPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(HemoglobinSettingsPm::class)
    abstract fun bindHemoglobinSettingsPm(pm: HemoglobinSettingsPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(GlucoseRangeDialogPm::class)
    abstract fun bindGlucoseRangeDialogPm(pm: GlucoseRangeDialogPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(RemindersPm::class)
    abstract fun bindRemindersPm(pm: RemindersPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(DevicesPm::class)
    abstract fun bindDevicesPm(pm: DevicesPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(DeviceInfoPm::class)
    abstract fun bindDeviceInfoPm(pm: DeviceInfoPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(FirmwarePm::class)
    abstract fun bindFirmwarePm(pm: FirmwarePm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(CreateRemindPm::class)
    abstract fun bindCreateRemindPm(pm: CreateRemindPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(EditRemindPm::class)
    abstract fun bindEditRemindPm(pm: EditRemindPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(ProfileSetNamePm::class)
    abstract fun bindProfileSetNamePm(pm: ProfileSetNamePm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(ProfileChangePasswordPm::class)
    abstract fun bindProfileChangePasswordPm(pm: ProfileChangePasswordPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(ProfileSetGenderPm::class)
    abstract fun bindProfileSetGenderPm(pm: ProfileSetGenderPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(SupportPm::class)
    abstract fun bindSupportPm(pm: SupportPm): PresentationModel

    // STATISTICS FLOW
    @Binds
    @IntoMap
    @PmKey(StatisticFlowPm::class)
    abstract fun bindStatisticFlowPm(pm: StatisticFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(PeriodPm::class)
    abstract fun bindPeriodPm(pm: PeriodPm): PresentationModel

    // FEEDBACK FLOW
    @Binds
    @IntoMap
    @PmKey(FeedbackPm::class)
    abstract fun bindFeedbackPm(pm: FeedbackPm): PresentationModel
}