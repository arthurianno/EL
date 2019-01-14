package com.nullgr.android.presentation.di

import com.nullgr.android.common.di.scope.ActivityScope
import com.nullgr.android.presentation.features.app.ui.AppActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindAppActivity(): AppActivity
}