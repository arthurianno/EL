package com.elta.android.presentation.di

import com.elta.android.common.di.scope.ReceiverScope
import com.elta.android.presentation.features.profile.settings.reminders.utils.BootReceiver
import com.elta.android.presentation.features.profile.settings.reminders.utils.ReminderNotificationReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ReceiverBuilder {

    @ReceiverScope
    @ContributesAndroidInjector
    abstract fun bindReminderBootReceiver(): BootReceiver

    @ReceiverScope
    @ContributesAndroidInjector
    abstract fun bindReminderNotificationReceiver(): ReminderNotificationReceiver
}