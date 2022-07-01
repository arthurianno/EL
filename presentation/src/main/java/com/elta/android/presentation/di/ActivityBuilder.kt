package com.elta.android.presentation.di

import com.elta.android.common.di.scope.ActivityScope
import com.elta.android.presentation.features.app.ui.AppActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindAppActivity(): AppActivity
}
