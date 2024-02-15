package com.elta.android.iiot

import android.app.Application
import ru.SDK.Test.BluetoothStatusCode
import ru.SDK.Test.DeviceCallBack
import ru.SDK.Test.DeviceService
import ru.SDK.Test.ELTAConnect
import ru.SDK.Test.PlatformStatusCode
import ru.SDK.Test.model.Observation
import ru.SDK.Test.model.ObservationError
import ru.SDK.Test.model.ObservationTemplate
import timber.log.Timber
import java.util.Date
import java.util.UUID

private const val LOG_TAG = "SDK_DeviceService_ELTA"

object IiotSdkDeviceService {

    fun init(application: Application, iiotSdkLogin: String, iiotSdkPassword: String) {
        try {
            DeviceService.init(
                application,
                iiotSdkLogin,
                iiotSdkPassword,
                deviceCallBack,
                BuildConfig.DEBUG
            )
        } catch (ex: Exception) {
            Timber.tag("LOG_TAG").e(ex)
        }

    }

    fun connect(pin: String, address: String) {
        DeviceService.connect(ELTAConnect::class.java, address, pin)
    }

    fun sendEvent(serial: String, model: String, date: Date, value: Double) {
        try {
            Timber.tag(LOG_TAG).i("RosTech SDK has been successfully called: serial = $serial, model = $model, date = $date, value = $value")
            val observation = ObservationTemplate.Glucometer(serial, model, date, value)
            Timber.tag(LOG_TAG).i("RosTech SDK has been successfully called: $observation")
            DeviceService.applyObservation(observation)
            
        } catch (ex: Exception) {
            Timber.tag(LOG_TAG).e(ex)
        }
    }

    private val deviceCallBack = object : DeviceCallBack {

        override fun onSendBundle(p0: java.util.HashMap<UUID, String>?) {
            Timber.tag(LOG_TAG).i("<Send Bundle> $p0")
        }

        override fun onSuccessMessage(p0: java.util.ArrayList<Observation>?) {
            Timber.tag(LOG_TAG).i("<Send Data> $p0")
        }

        override fun onErrorMessage(p0: ArrayList<ObservationError<Any, Any>>?) {
            Timber.tag(LOG_TAG).i("<ERROR Message> ${p0.toString()}")
        }

        override fun onExploreDevice(name: String?, value: String?, p2: Any?) {
            Timber.tag(LOG_TAG).i("<READ Event ($LOG_TAG)> -> $name=$value - $p2")
        }

        override fun onStatusDevice(p0: String?, statusCode: BluetoothStatusCode?) {
            Timber.tag(LOG_TAG).i("<Device Status> -> $p0 = ${statusCode?.name} ")
        }

        override fun onDisconnect(p0: String?, events: HashMap<String, Any>?) {
            Timber.tag(LOG_TAG).i("<DISCONNECT $LOG_TAG> ($p0), Read ${events?.count()} events")
        }

        override fun onException(p0: String?, exception: Exception?) {
            Timber.tag(LOG_TAG).e(exception, "<$LOG_TAG ERROR ($p0)> -> ${exception?.message}")
        }

        override fun onConnectToPlatform(p0: PlatformStatusCode?, p1: Int, p2: String?) {
            Timber.tag(LOG_TAG).e("<$LOG_TAG CONNECT_TO_PLATFORM ($p0)> -> $p1 $p2")
        }
    }
}