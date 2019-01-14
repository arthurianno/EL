package com.nullgr.android.presentation.di

import com.nullgr.android.common.di.scope.FragmentScope
import com.nullgr.android.presentation.features.onboaring.ui.OnBoardingFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
@Suppress("UnnecessaryAbstractClass", "TooManyFunctions")
abstract class FragmentBuilder {

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindOnBoardingFragment(): OnBoardingFragment
}