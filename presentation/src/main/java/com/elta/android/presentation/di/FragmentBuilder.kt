package com.elta.android.presentation.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.onboaring.ui.OnBoardingFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
@Suppress("UnnecessaryAbstractClass", "TooManyFunctions")
abstract class FragmentBuilder {

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindOnBoardingFragment(): OnBoardingFragment
}