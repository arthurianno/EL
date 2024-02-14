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
            BuildConfig.DEBUG
        )
    }

    fun connect(pin: String, address: String) {
        DeviceService.connect(ELTAConnect::class.java, address, pin)
    }

    fun sendEvent(serial: String, model: String, event: Pair<String, Double>) {
        val (time, value) = event
        try {
            ELTAConnect.sendData(serial, model, time, value)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private val deviceCallBack = object : DeviceCallBack {
        override fun onExploreDevice(name: String?, value: String?, p2: Any?) {
        }

        override fun onStatusDevice(p0: String?, statusCode: BluetoothStatusCode?) {
            Timber.tag(LOG_TAG).i("$p0 $statusCode")
        }


        override fun onSendData(p0: String?, statusCode: PlatformStatusCode?) {
        }

        override fun onDisconnect(p0: String?, events: HashMap<String, Any>?) {
        }

        override fun onException(p0: String?, exception: Exception?) {
            Timber.tag(LOG_TAG).i(exception?.message.orEmpty())
        }
    }
}