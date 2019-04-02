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
import com.elta.android.presentation.features.diary.flow.pm.DiaryFlowPm
import com.elta.android.presentation.features.diary.main.pm.MainDiaryPm
import com.elta.android.presentation.features.greeting.pm.GreetingPm
import com.elta.android.presentation.features.home.pm.HomeFlowPm
import com.elta.android.presentation.features.main.events.chooser.pm.EventsOptionsChooserPm
import com.elta.android.presentation.features.main.events.create.pm.EventCreationPm
import com.elta.android.presentation.features.main.events.edit.pm.EditEventPm
import com.elta.android.presentation.features.main.flow.pm.MainFlowPm
import com.elta.android.presentation.features.main.records.pm.MainRecordsPm
import com.elta.android.presentation.features.onboaring.pm.OnBoardingPm
import com.elta.android.presentation.features.profile.flow.pm.ProfileFlowPm
import com.elta.android.presentation.features.profile.main.pm.MainProfilePm
import com.elta.android.presentation.features.profile.settings.global.pm.ProfileSettingsPm
import com.elta.android.presentation.features.profile.settings.dialogs.diabetes.pm.DiabetesSettingDialogPm
import com.elta.android.presentation.features.registration.activation.pm.ActivationPm
import com.elta.android.presentation.features.registration.confirmation.pm.EmailConfirmationPm
import com.elta.android.presentation.features.registration.flow.pm.RegistrationFlowPm
import com.elta.android.presentation.features.registration.main.pm.RegistrationMainPm
import com.elta.android.presentation.features.registration.policy.pm.RegistrationPrivacyPolicyPm
import com.elta.android.presentation.features.registration.social.pm.RegistrationSocialPm
import com.elta.android.presentation.features.shops.flow.pm.ShopsFlowPm
import com.elta.android.presentation.features.shops.map.pm.ShopsMapPm
import com.elta.android.presentation.features.shops.start.pm.ShopsStartPm
import com.elta.android.presentation.features.sync.flow.pm.SyncFlowPm
import com.elta.android.presentation.features.sync.start.pm.SyncStartPm
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import me.dmdev.rxpm.PresentationModel

@Suppress("TooManyFunctions")
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

    // SYNC FLOW
    @Binds
    @IntoMap
    @PmKey(SyncFlowPm::class)
    abstract fun bindSyncFlowPm(pm: SyncFlowPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(SyncStartPm::class)
    abstract fun bindSyncStartPm(pm: SyncStartPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(BluetoothPm::class)
    abstract fun bindBluetoothPm(pm: BluetoothPm): PresentationModel

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
    @PmKey(ProfileSettingsPm::class)
    abstract fun bindProfileSettingsPm(pm: ProfileSettingsPm): PresentationModel

    @Binds
    @IntoMap
    @PmKey(DiabetesSettingDialogPm::class)
    abstract fun bindDiabetesSettingDialogPm(pm: DiabetesSettingDialogPm): PresentationModel
}