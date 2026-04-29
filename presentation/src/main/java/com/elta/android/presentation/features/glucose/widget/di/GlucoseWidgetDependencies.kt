package com.elta.android.presentation.features.glucose.widget.di

import com.elta.android.domain.features.diary.home.interactor.GetHomeModelUseCase

/**
 * Runtime dependencies required by Glucose widget worker.
 *
 * Presentation module cannot access app component directly, so the Application implements this
 * interface and provides dependencies from Dagger.
 */
interface GlucoseWidgetDependencies {
    val getHomeModelUseCase: GetHomeModelUseCase
}

