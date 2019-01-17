package com.elta.android.presentation.di

import com.elta.android.presentation.core.navigation.LocalCiceroneHolder
import com.elta.android.presentation.core.navigation.UiThreadRouter
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Singleton

@Module
class NavigationModule {

    private var cicerone: Cicerone<Router> = Cicerone.create(UiThreadRouter())

    @Provides
    @Singleton
    fun provideRouter(): Router = cicerone.router

    @Provides
    @Singleton
    fun provideNavigatorHolder(): NavigatorHolder = cicerone.navigatorHolder

    @Provides
    @Singleton
    fun provideLocalCiceroneHolder(): LocalCiceroneHolder = LocalCiceroneHolder()
}