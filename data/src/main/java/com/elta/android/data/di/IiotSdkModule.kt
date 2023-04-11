package com.elta.android.data.di

import dagger.Module
import dagger.Provides
import ru.SDK.BLE.DeviceCallBack
import ru.SDK.BLE.DeviceService
import timber.log.Timber
import java.lang.Exception
import java.util.HashMap
import javax.inject.Singleton

private const val LOG_TAG = "SDK_DeviceService_ELTA"

@Module
class IiotSdkModule {

    @Singleton
    @Provides
    fun provideIiotSdkCallback(): DeviceCallBack = object : DeviceCallBack {
        override fun onExploreDevice(name: String?, value: Any?) {
            Timber.tag(LOG_TAG).i("<READ Event ($LOG_TAG)> -> $name=$value")
        }

        override fun onDisconnect(p0: HashMap<String, Any>?) {
            Timber.tag(LOG_TAG).i("<DISCONNECT $LOG_TAG> Read ${p0?.count()} events")
        }

        override fun onException(exception: Exception?) {
            Timber.tag(LOG_TAG).e(exception, "<$LOG_TAG ERROR> -> ${exception?.message}")
        }
    }

    @Provides
    @Singleton
    fun provideDeviceService(): DeviceService = DeviceService()
}
