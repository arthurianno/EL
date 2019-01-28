package com.elta.android.presentation.di

import com.elta.android.presentation.core.pm.PmKey
import com.elta.android.presentation.core.pm.factory.GeneralPmFactory
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.features.app.pm.AppPm
import com.elta.android.presentation.features.auth.flow.pm.AuthFlowPm
import com.elta.android.presentation.features.auth.login.pm.LoginPm
import com.elta.android.presentation.features.auth.password.create.pm.AuthPasswordCreatePm
import com.elta.android.presentation.features.auth.password.recovery.pm.AuthPasswordRecoveryPm
import com.elta.android.presentation.features.registration.confirmation.pm.EmailConfirmationPm
import com.elta.android.presentation.features.greeting.pm.GreetingPm
import com.elta.android.presentation.features.onboaring.pm.OnBoardingPm
import com.elta.android.presentation.features.registration.activation.pm.ActivationPm
import com.elta.android.presentation.features.registration.flow.pm.RegistrationFlowPm
import com.elta.android.presentation.features.registration.main.pm.RegistrationMainPm
import com.elta.android.presentation.features.registration.policy.pm.RegistrationPrivacyPolicyPm
import com.elta.android.presentation.features.registration.social.pm.RegistrationSocialPm
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
}