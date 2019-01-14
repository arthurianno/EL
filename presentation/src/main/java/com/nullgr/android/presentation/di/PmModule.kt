package com.nullgr.android.presentation.di

import com.nullgr.android.presentation.core.pm.PmKey
import com.nullgr.android.presentation.core.pm.factory.GeneralPmFactory
import com.nullgr.android.presentation.core.pm.factory.PmFactory
import com.nullgr.android.presentation.features.app.pm.AppPm
import com.nullgr.android.presentation.features.onboaring.pm.OnBoardingPm
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
}