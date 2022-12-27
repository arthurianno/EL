package com.elta.android.presentation.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.auth.flow.ui.AuthFlowFragment
import com.elta.android.presentation.features.auth.login.ui.LoginFragment
import com.elta.android.presentation.features.auth.password.create.ui.AuthPasswordCreateFragment
import com.elta.android.presentation.features.auth.password.recovery.ui.AuthPasswordRecoveryFragment
import com.elta.android.presentation.features.bluetooth.ui.BluetoothFragment
import com.elta.android.presentation.features.calcutator.CalculatorFragment
import com.elta.android.presentation.features.calcutator.DishDetailFragment
import com.elta.android.presentation.features.consultant.ConsultantFragment
import com.elta.android.presentation.features.devices.all.ui.DevicesFragment
import com.elta.android.presentation.features.devices.firmware.ui.FirmwareFragment
import com.elta.android.presentation.features.devices.info.ui.DeviceInfoFragment
import com.elta.android.presentation.features.diary.flow.ui.DiaryFlowFragment
import com.elta.android.presentation.features.diary.main.di.MainDiaryModule
import com.elta.android.presentation.features.diary.main.ui.MainDiaryFragment
import com.elta.android.presentation.features.feedback.ui.FeedbackFragment
import com.elta.android.presentation.features.greeting.ui.GreetingFlowFragment
import com.elta.android.presentation.features.home.ui.HomeFlowFragment
import com.elta.android.presentation.features.main.events.chooser.ui.EventsOptionsChooserFragment
import com.elta.android.presentation.features.main.events.create.ui.EventCreationFragment
import com.elta.android.presentation.features.main.events.edit.ui.EditEventFragment
import com.elta.android.presentation.features.main.events.glucose.ui.GlucoseEventFragment
import com.elta.android.presentation.features.main.flow.ui.MainFlowFragment
import com.elta.android.presentation.features.main.records.di.MainRecordsModule
import com.elta.android.presentation.features.main.records.ui.MainRecordsFragment
import com.elta.android.presentation.features.observers.all.ui.ObserversFragment
import com.elta.android.presentation.features.observers.edit.ui.EditObserverFragment
import com.elta.android.presentation.features.observers.invite.ui.InviteObserverFragment
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import com.elta.android.presentation.features.profile.flow.ui.ProfileFlowFragment
import com.elta.android.presentation.features.profile.main.ui.MainProfileFragment
import com.elta.android.presentation.features.profile.settings.dialogs.diabetes.ui.DiabetesSettingDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui.GlucoseRangeDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.HemoglobinSettingsFragment
import com.elta.android.presentation.features.profile.settings.gender.ui.ProfileSetGenderFragment
import com.elta.android.presentation.features.profile.settings.global.ui.ProfileSettingsFragment
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
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.features.shops.flow.ui.ShopsFlowFragment
import com.elta.android.presentation.features.shops.map.ui.ShopsMapFragment
import com.elta.android.presentation.features.shops.start.ui.ShopsStartFragment
import com.elta.android.presentation.features.statistic.flow.ui.StatisticFlowFragment
import com.elta.android.presentation.features.statistic.period.di.PeriodModule
import com.elta.android.presentation.features.statistic.period.ui.PeriodFragment
import com.elta.android.presentation.features.statistic.report.ui.ReportPeriodChooserFragment
import com.elta.android.presentation.features.sync.connect.onboarding.ui.FromOnBoardingConnectDeviceFragment
import com.elta.android.presentation.features.sync.connect.other.ui.FromOtherConnectDeviceFragment
import com.elta.android.presentation.features.sync.flow.onboarding.ui.FromOnBoardingSyncFlowFragment
import com.elta.android.presentation.features.sync.flow.other.ui.FromOtherSyncFlowFragment
import com.elta.android.presentation.features.sync.pin.ui.PinDialogFragment
import com.elta.android.presentation.features.sync.start.onboarding.ui.FromOnBoardingSyncStartFragment
import com.elta.android.presentation.features.sync.start.other.ui.FromOtherSyncStartFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
@Suppress("UnnecessaryAbstractClass", "TooManyFunctions")
abstract class FragmentBuilder {

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindOnBoardingFragment(): OnBoardingFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindGreetingFragment(): GreetingFlowFragment

    // REGISTRATION FLOW
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindRegistrationFlowFragment(): RegistrationFlowFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindRegistrationMainFragment(): RegistrationMainFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindRegistrationPrivacyPolicyFragment(): RegistrationPrivacyPolicyFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindActivationFragment(): ActivationFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindEmailConfirmationFragment(): EmailConfirmationFragment

    // AUTH FLOW
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindAuthFlowFragment(): AuthFlowFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindLoginFragment(): LoginFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindAuthPasswordRecoveryFragment(): AuthPasswordRecoveryFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindAuthPasswordCreateFragment(): AuthPasswordCreateFragment

    // SHOPS FLOW
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindShopsFlowFragment(): ShopsFlowFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindShopsStartFragment(): ShopsStartFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindShopsMapFragment(): ShopsMapFragment

    // HOME FLOW
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindHomeFlowFragment(): HomeFlowFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindMainFlowFragment(): MainFlowFragment

    // MAIN FLOW
    @FragmentScope
    @ContributesAndroidInjector(modules = [MainRecordsModule::class])
    abstract fun bindMainRecordsFragment(): MainRecordsFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindEventCreationFragment(): EventCreationFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindEventsOptionsChooserFragment(): EventsOptionsChooserFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindEditEventFragment(): EditEventFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindGlucoseEventFragment(): GlucoseEventFragment

    // SYNC FLOW
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindFromOnBoardingSyncFlowFragment(): FromOnBoardingSyncFlowFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindFromOtherSyncFlowFragment(): FromOtherSyncFlowFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindFromOnBoardingSyncStartFragment(): FromOnBoardingSyncStartFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindFromOtherSyncStartFragment(): FromOtherSyncStartFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindBluetoothFragment(): BluetoothFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindPinDialogFragment(): PinDialogFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindFromOnBoardingConnectDeviceFragment(): FromOnBoardingConnectDeviceFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindFromOtherConnectDeviceFragment(): FromOtherConnectDeviceFragment

    // DIARY FLOW
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindDiaryFlowFragment(): DiaryFlowFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [MainDiaryModule::class])
    abstract fun bindMainDiaryFragment(): MainDiaryFragment

    // PROFILE FLOW
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindProfileFlowFragment(): ProfileFlowFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindMainProfileFragment(): MainProfileFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindObserversFragment(): ObserversFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindProfileSettingsFragment(): ProfileSettingsFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindDiabetesSettingDialogFragment(): DiabetesSettingDialogFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindHemoglobinSettingsFragment(): HemoglobinSettingsFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindGlucoseRangeDialogFragment(): GlucoseRangeDialogFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindDevicesFragment(): DevicesFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindDeviceInfoFragment(): DeviceInfoFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindFirmwareFragment(): FirmwareFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindRemindersFragment(): RemindersFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindCreateRemindFragment(): CreateRemindFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindEditRemindFragment(): EditRemindFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindInviteObserverFragment(): InviteObserverFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindEditObserverFragment(): EditObserverFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindProfileSetNameFragment(): ProfileSetNameFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindProfileChangePasswordFragment(): ProfileChangePasswordFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindProfileSetGenderFragment(): ProfileSetGenderFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindSupportFragment(): SupportFragment

    // STATISTICS FLOW
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindStatisticFlowFragment(): StatisticFlowFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [PeriodModule::class])
    abstract fun bindPeriodFragment(): PeriodFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindReportPeriodChooserFragment(): ReportPeriodChooserFragment

    // FEEDBACK FLOW
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindFeedbackFragment(): FeedbackFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindCalculatorFragment(): CalculatorFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindAddDishFragment(): DishDetailFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindConsultantFragment(): ConsultantFragment
}
