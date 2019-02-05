package com.elta.android.presentation.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.auth.flow.ui.AuthFlowFragment
import com.elta.android.presentation.features.auth.login.ui.LoginFragment
import com.elta.android.presentation.features.auth.password.create.ui.AuthPasswordCreateFragment
import com.elta.android.presentation.features.auth.password.recovery.ui.AuthPasswordRecoveryFragment
import com.elta.android.presentation.features.greeting.ui.GreetingFlowFragment
import com.elta.android.presentation.features.onboaring.di.OnBoardingModule
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import com.elta.android.presentation.features.registration.activation.ui.ActivationFragment
import com.elta.android.presentation.features.registration.confirmation.ui.EmailConfirmationFragment
import com.elta.android.presentation.features.registration.flow.ui.RegistrationFlowFragment
import com.elta.android.presentation.features.registration.main.ui.RegistrationMainFragment
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.features.registration.social.ui.RegistrationSocialFragment
import com.elta.android.presentation.features.shops.flow.ui.ShopsFlowFragment
import com.elta.android.presentation.features.shops.start.ui.ShopsStartFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
@Suppress("UnnecessaryAbstractClass", "TooManyFunctions")
abstract class FragmentBuilder {

    @FragmentScope
    @ContributesAndroidInjector(modules = [OnBoardingModule::class])
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
    abstract fun bindRegistrationSocialFragment(): RegistrationSocialFragment

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
}