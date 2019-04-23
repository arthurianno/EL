package com.elta.android.presentation.jobs

import androidx.work.ListenableWorker
import com.elta.android.presentation.jobs.factory.JobFactory
import com.elta.android.presentation.jobs.factory.WorkerKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface WorkersModule {

    @Binds
    @IntoMap
    @WorkerKey(ReminderWorker::class)
    fun bindReminderWorker(worker: ReminderWorker.Factory): JobFactory<out ListenableWorker>
}