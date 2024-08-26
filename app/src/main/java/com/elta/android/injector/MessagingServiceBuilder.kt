package com.elta.android.injector

import com.elta.android.EltaMessageService
import com.elta.android.common.di.scope.ServiceScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface MessagingServiceBuilder {
    @ContributesAndroidInjector
    @ServiceScope
    fun fcmService() : EltaMessageService
}
