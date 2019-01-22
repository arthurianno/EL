package com.elta.android.presentation.di

import com.elta.android.presentation.core.pm.PmKey
import com.elta.android.presentation.core.pm.factory.GeneralPmFactory
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.features.app.pm.AppPm
import com.elta.android.presentation.features.greeting.pm.GreetingPm
import com.elta.android.presentation.features.onboaring.pm.OnBoardingPm
import com.elta.android.presentation.features.registration.activation.pm.ActivationPm
import com.elta.android.presentation.features.registration.flow.pm.RegistrationFlowPm
import com.elta.android.presentation.features.registration.main.pm.RegistrationMainPm
import com.elta.android.presentation.features.registration.policy.pm.RegistrationPrivacyPolicyPm
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import me.dmdev.rxpm.PresentationModel

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
}