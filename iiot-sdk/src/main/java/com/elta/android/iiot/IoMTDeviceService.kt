package com.elta.android.iiot

import android.app.Application
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import ru.SDK.Test.BluetoothStatusCode
import ru.SDK.Test.DeviceCallBack
import ru.SDK.Test.DeviceService
import ru.SDK.Test.ELTAConnect
import ru.SDK.Test.PlatformStatusCode
import ru.SDK.Test.model.Observation
import ru.SDK.Test.model.ObservationError
import ru.SDK.Test.model.ObservationTemplate
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.Date
import java.util.UUID

private const val LOG_TAG = "SDK_DeviceService_ELTA"

object IoMTDeviceService {

    private var _crashlyticsReport: WeakReference<CrashlyticsReport>? = null
    private val  crashlyticsReport: CrashlyticsReport? get() = _crashlyticsReport?.get()

    fun init(
        application: Application,
        iiotSdkLogin: String,
        iiotSdkPassword: String,
        logger: CrashlyticsReport
    ) {
        _crashlyticsReport = WeakReference(logger)

        crashlyticsReport?.log(("IoMT SDK initialization started"))
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
        crashlyticsReport?.log("IoMT SDK initialization finished")

    }

    fun connect(pin: String, address: String) {
        DeviceService.connect(ELTAConnect::class.java, address, pin)
    }

    fun sendEvents(events: List<IoMTEvent>) {
        crashlyticsReport?.log("IoMT SDK starts sending measurements, events size: ${events.size}")
        val observations = events.map {
            ObservationTemplate.Glucometer(UUID.fromString(it.id), it.serial, it.model, it.date, it.value)
        }

        DeviceService.applyObservation(observations)
        crashlyticsReport?.log("IoMT SDK finished sending measurements")
    }

    private val deviceCallBack = object : DeviceCallBack {

        override fun onSendBundle(p0: java.util.HashMap<UUID, String>?) {
            crashlyticsReport?.log("IoMT SDK: bundle send $p0")
        }

        override fun onSuccessMessage(p0: java.util.ArrayList<Observation>?) {
            crashlyticsReport?.log("IoMT SDK: observations success $p0")
        }

        override fun onErrorMessage(p0: ArrayList<ObservationError<Any, Any>>?) {
            if (p0.isNullOrEmpty()) return
            val messagesErrors = p0.map { it.message }.toString()
            crashlyticsReport?.writeException(IoMTException(messagesErrors))
        }

        override fun onExploreDevice(name: String?, value: String?, p2: Any?) {
            crashlyticsReport?.log("IoMT SDK: device explored $name")
        }

        override fun onSendFetalOnPlatform(p0: PlatformStatusCode?, p1: String?) {
            crashlyticsReport?.log("IoMT SDK: fetal send on platform $p0, $p1")
        }

        override fun onStatusDevice(p0: String?, statusCode: BluetoothStatusCode?) {
            crashlyticsReport?.log("IoMT SDK: on device status $p0, code: $statusCode")
        }

        override fun onDisconnect(p0: String?, events: HashMap<String, Any>?) {
            crashlyticsReport?.log("IoMT SDK: on disconnect: $p0, eventsSize: ${events?.size}")
        }

        override fun onException(p0: String?, exception: Exception?) {
            crashlyticsReport?.writeException(IoMTException(p0))
        }

        override fun onConnectToPlatform(p0: PlatformStatusCode?, p1: Int, p2: String?) {
            crashlyticsReport?.log("IoMT SDK: on connect to platform ($p0)> -> $p1 $p2")
        }
    }

    data class IoMTEvent(
        val id: String,
        val serial: String,
        val model: String,
        val date: Date,
        val value: Double
    )

    class IoMTException(message: String?) : Exception(message)
}