package com.elta.android.presentation.di

import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.navigation.LocalCiceroneHolder
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NavigationModule {

    private var cicerone: Cicerone<Router> = Cicerone.create(FlowRouter(null))

    @Provides
    @Singleton
    fun provideRouter(): FlowRouter = cicerone.router as FlowRouter

    @Provides
    @Singleton
    fun provideNavigatorHolder(): NavigatorHolder = cicerone.getNavigatorHolder()

    @Provides
    @Singleton
    fun provideLocalCiceroneHolder(): LocalCiceroneHolder = LocalCiceroneHolder()
}
