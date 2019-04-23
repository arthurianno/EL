package com.elta.android.presentation.jobs.factory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

interface JobFactory<T : ListenableWorker> {
    fun create(appContext: Context, params: WorkerParameters): T
}