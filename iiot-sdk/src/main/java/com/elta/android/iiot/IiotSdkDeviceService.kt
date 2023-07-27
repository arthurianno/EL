package com.elta.android.iiot

import android.app.Application
import ru.SDK.Test.BluetoothStatusCode
import ru.SDK.Test.DeviceCallBack
import ru.SDK.Test.DeviceService
import ru.SDK.Test.ELTAConnect
import ru.SDK.Test.PlatformStatusCode
import timber.log.Timber

private const val LOG_TAG = "SDK_DeviceService_ELTA"

object IiotSdkDeviceService {

    fun init(application: Application, iiotSdkLogin: String, iiotSdkPassword: String) {
        DeviceService.init(
            application,
            iiotSdkLogin,
            iiotSdkPassword,
            deviceCallBack,
            true //?
        )
    }

    fun connect(pin: String, address: String) {
        DeviceService.connect(ELTAConnect::class.java, address, pin)
    }

    private val deviceCallBack = object : DeviceCallBack {
        override fun onExploreDevice(name: String?, value: String?, p2: Any?) {
            Timber.tag(LOG_TAG).i("<READ Event ($LOG_TAG)> -> $name=$value - $p2")
        }

        override fun onStatusDevice(p0: String?, statusCode: BluetoothStatusCode?) {
            Timber.tag(LOG_TAG).i("<Device Status> -> $p0 = ${statusCode?.name} ")
        }

        override fun onSendData(p0: String?, statusCode: PlatformStatusCode?) {
            Timber.tag(LOG_TAG).i("<Send Data> $p0, status = ${statusCode?.name}")
        }

        override fun onDisconnect(p0: String?, events: HashMap<String, Any>?) {
            Timber.tag(LOG_TAG).i("<DISCONNECT $LOG_TAG> ($p0), Read ${events?.count()} events")
        }

        override fun onException(p0: String?, exception: Exception?) {
            Timber.tag(LOG_TAG).e(exception, "<$LOG_TAG ERROR ($p0)> -> ${exception?.message}")
        }
    }
}