package com.elta.android

import android.content.Context
import com.elta.android.data.di.ApiConstantsModule
import com.elta.android.data.di.ApiModule
import com.elta.android.data.di.BleToolsModule
import com.elta.android.data.di.CacheModule
import com.elta.android.data.di.ClipboardModule
import com.elta.android.data.di.CoroutineModule
import com.elta.android.data.di.DataSourceModule
import com.elta.android.data.di.FatSecretModule
import com.elta.android.data.di.GlucometerImplModule
import com.elta.android.data.di.GlucometerModule
import com.elta.android.data.di.InterceptorModule
import com.elta.android.data.di.LocalSyncModule
import com.elta.android.data.di.MappersModule
import com.elta.android.data.di.MediaModule
import com.elta.android.data.di.MigrationModule
import com.elta.android.data.di.NetworkModule
import com.elta.android.data.di.NetworkRequesterModule
import com.elta.android.data.di.NewsModule
import com.elta.android.data.di.RemoteModule
import com.elta.android.data.di.RepoModule
import com.elta.android.data.di.RoomModule
import com.elta.android.data.di.ServiceModule
import com.elta.android.data.di.StorageModule
import com.elta.android.data.di.TokenModule
import com.elta.android.data.di.WebimModule
import com.elta.android.injector.MessagingServiceBuilder
import com.elta.android.presentation.di.ActivityBuilder
import com.elta.android.presentation.di.AnalyticModule
import com.elta.android.presentation.di.FragmentBuilder
import com.elta.android.presentation.di.NavigationModule
import com.elta.android.presentation.di.NotificationModule
import com.elta.android.presentation.di.PmModule
import com.elta.android.presentation.di.ReceiverBuilder
import com.elta.android.presentation.di.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import timber.log.Timber
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        // common
        AndroidSupportInjectionModule::class,
        AppModule::class,
        MessagingServiceBuilder::class,
        // data
        ApiModule::class,
        ApiConstantsModule::class,
        NetworkModule::class,
        TokenModule::class,
        DataSourceModule::class,
        MappersModule::class,
        MigrationModule::class,
        CacheModule::class,
        StorageModule::class,
        RemoteModule::class,
        BleToolsModule::class,
        GlucometerModule::class,
        GlucometerImplModule::class,
        LocalSyncModule::class,
        FatSecretModule::class,
        WebimModule::class,
        ServiceModule::class,
        ClipboardModule::class,
        NetworkRequesterModule::class,
        // domain
        RepoModule::class,
        RoomModule::class,
        // presentation
        PmModule::class,
        NewsModule::class,
        ViewModelModule::class,
        ActivityBuilder::class,
        FragmentBuilder::class,
        NotificationModule::class,
        ReceiverBuilder::class,
        MediaModule::class,
        // navigation
        NavigationModule::class,
        // analytics
        AnalyticModule::class,
        // Coroutines
        CoroutineModule::class
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder

        fun appModule(module: AppModule): Builder

        fun apiConstantsModule(module: ApiConstantsModule): Builder

        fun interceptorModule(module: InterceptorModule): Builder

        fun analyticsModule(analyticsModule: AnalyticModule): Builder

        fun build(): AppComponent
    }

    fun inject(application: App)

    fun context(): Context

    fun logTree(): Timber.Tree
}
