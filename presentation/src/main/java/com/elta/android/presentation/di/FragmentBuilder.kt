package com.elta.android.presentation.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.auth.flow.ui.AuthFlowFragment
import com.elta.android.presentation.features.auth.login.ui.LoginFragment
import com.elta.android.presentation.features.auth.password.create.ui.AuthPasswordCreateFragment
import com.elta.android.presentation.features.auth.password.recovery.ui.AuthPasswordRecoveryFragment
import com.elta.android.presentation.features.greeting.ui.GreetingFlowFragment
import com.elta.android.presentation.features.home.di.HomeFlowModule
import com.elta.android.presentation.features.home.ui.HomeFlowFragment
import com.elta.android.presentation.features.main.events.chooser.di.EventsOptionsChooserModule
import com.elta.android.presentation.features.main.events.chooser.ui.EventsOptionsChooserFragment
import com.elta.android.presentation.features.main.events.create.ui.EventCreationFragment
import com.elta.android.presentation.features.main.events.edit.ui.EditEventFragment
import com.elta.android.presentation.features.main.flow.ui.MainFlowFragment
import com.elta.android.presentation.features.main.records.di.MainRecordsModule
import com.elta.android.presentation.features.main.records.ui.MainRecordsFragment
import com.elta.android.presentation.features.onboaring.di.OnBoardingModule
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import com.elta.android.presentation.features.profile.flow.ui.ProfileFlowFragment
import com.elta.android.presentation.features.profile.main.di.MainProfileModule
import com.elta.android.presentation.features.profile.main.ui.MainProfileFragment
import com.elta.android.presentation.features.registration.activation.ui.ActivationFragment
import com.elta.android.presentation.features.registration.confirmation.ui.EmailConfirmationFragment
import com.elta.android.presentation.features.registration.flow.ui.RegistrationFlowFragment
import com.elta.android.presentation.features.registration.main.ui.RegistrationMainFragment
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.features.registration.social.ui.RegistrationSocialFragment
import com.elta.android.presentation.features.shops.flow.ui.ShopsFlowFragment
import com.elta.android.presentation.features.shops.map.di.ShopsMapModule
import com.elta.android.presentation.features.shops.map.ui.ShopsMapFragment
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

    @FragmentScope
    @ContributesAndroidInjector(modules = [ShopsMapModule::class])
    abstract fun bindShopsMapFragment(): ShopsMapFragment

    // HOME FLOW
    @FragmentScope
    @ContributesAndroidInjector(modules = [HomeFlowModule::class])
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
    @ContributesAndroidInjector(modules = [EventsOptionsChooserModule::class])
    abstract fun bindEventsOptionsChooserFragment(): EventsOptionsChooserFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindEditEventFragment(): EditEventFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindProfileFlowFragment(): ProfileFlowFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [MainProfileModule::class])
    abstract fun bindMainProfileFragment(): MainProfileFragment
}