package com.nullgr.android

import android.content.Context
import com.nullgr.android.data.di.ApiConstantsModule
import com.nullgr.android.data.di.ApiModule
import com.nullgr.android.data.di.DataSourceModule
import com.nullgr.android.data.di.InterceptorModule
import com.nullgr.android.data.di.MappersModule
import com.nullgr.android.data.di.NetworkModule
import com.nullgr.android.data.di.RepoModule
import com.nullgr.android.presentation.di.ActivityBuilder
import com.nullgr.android.presentation.di.AnalyticsModule
import com.nullgr.android.presentation.di.FragmentBuilder
import com.nullgr.android.presentation.di.NavigationModule
import com.nullgr.android.presentation.di.PmModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import timber.log.Timber
import javax.inject.Singleton

@Singleton
@Component(modules = [
    // common
    AndroidSupportInjectionModule::class,
    AppModule::class,
    // data
    ApiModule::class,
    ApiConstantsModule::class,
    NetworkModule::class,
    DataSourceModule::class,
    MappersModule::class,
    // domain
    RepoModule::class,
    // presentation
    PmModule::class,
    ActivityBuilder::class,
    FragmentBuilder::class,
    // navigation
    NavigationModule::class,
    // analytics
    AnalyticsModule::class
])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder

        fun appModule(module: AppModule): Builder

        fun apiConstantsModule(module: ApiConstantsModule): Builder

        fun interceptorModule(module: InterceptorModule): Builder

        fun analyticsModule(analyticsModule: AnalyticsModule): Builder

        fun build(): AppComponent
    }

    fun inject(application: App)

    fun context(): Context

    fun logTree(): Timber.Tree
}