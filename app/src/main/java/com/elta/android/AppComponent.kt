package com.elta.android

import android.content.Context
import com.elta.android.data.di.ApiConstantsModule
import com.elta.android.data.di.ApiModule
import com.elta.android.data.di.CacheModule
import com.elta.android.data.di.DataSourceModule
import com.elta.android.data.di.InterceptorModule
import com.elta.android.data.di.MappersModule
import com.elta.android.data.di.NetworkModule
import com.elta.android.data.di.RepoModule
import com.elta.android.data.di.StorageModule
import com.elta.android.data.di.TokenModule
import com.elta.android.presentation.di.ActivityBuilder
import com.elta.android.presentation.di.AnalyticsModule
import com.elta.android.presentation.di.FragmentBuilder
import com.elta.android.presentation.di.NavigationModule
import com.elta.android.presentation.di.PmModule
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
    TokenModule::class,
    DataSourceModule::class,
    MappersModule::class,
    CacheModule::class,
    StorageModule::class,
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