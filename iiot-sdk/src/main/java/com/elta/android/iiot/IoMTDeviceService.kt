package com.elta.android.iiot

import android.app.Application
import com.elta.android.common.errors.GlucometerConnectionException
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import ru.SDK.Test.BluetoothStatusCode
import ru.SDK.Test.DeviceCallBack
import ru.SDK.Test.DeviceService
import ru.SDK.Test.ELTAConnect
import ru.SDK.Test.PlatformStatusCode
import ru.SDK.Test.model.Observation
import ru.SDK.Test.model.ObservationError
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.UUID

object IoMTDeviceService {

    private var _crashlyticsReport: WeakReference<CrashlyticsReport>? = null
    private val crashlyticsReport: CrashlyticsReport?
        get() = _crashlyticsReport?.get()

    private var onExceptionCallback: ((Exception) -> Unit)? = null
    private var onDisconnectCallback: (() -> Unit)? = null

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

    fun connect(pin: String, address: String, email: String) {
        DeviceService.connect(ELTAConnect::class.java, address, pin, email)
    }

    fun setListeners(onDisconnect: (() -> Unit)?, onException: ((Exception) -> Unit)?) {
        onExceptionCallback = onException
        onDisconnectCallback = onDisconnect
    }

    fun sendLogs() {
        DeviceService.sendLogs()
    }

    fun clearLogs() {
        DeviceService.clearLogs()
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

        override fun onExploreDevice(name: String?, attribute: String?, value: Any?) {
            crashlyticsReport?.log("IoMT SDK: device explored $name  attribute: $attribute, value: $value")
        }

        override fun onSendFetalOnPlatform(status: PlatformStatusCode?, value: String?) {
            crashlyticsReport?.log("IoMT SDK: fetal send on platform $status, $value")
        }

        override fun onStatusDevice(mac: String?, statusCode: BluetoothStatusCode?) {
            crashlyticsReport?.log("IoMT SDK: on device status $mac, code: $statusCode")
            when (statusCode) {
                BluetoothStatusCode.ConnectDisconnect -> onDisconnectCallback?.invoke()
                BluetoothStatusCode.ConnectFail ->
                    onExceptionCallback?.invoke(GlucometerConnectionException(mac.orEmpty()))

                else -> {}
            }
        }

        @Deprecated("Нерабочий метод")
        override fun onDisconnect(mac: String?, events: HashMap<String, Any>?) {
            crashlyticsReport?.log("IoMT SDK: on disconnect: $mac, eventsSize: ${events?.size}")
        }

        override fun onException(mac: String?, exception: Exception?) {
            crashlyticsReport?.writeException(IoMTException(mac))
            exception?.let { onExceptionCallback?.invoke(it) }
            setListeners(null, null)
        }

        override fun onConnectToPlatform(status: PlatformStatusCode?, message: String?) {
            crashlyticsReport?.log("IoMT SDK: on connect to platform ($status)> -> $message")
            setListeners(null, null)
        }

        override fun onConnectToPlatform(
            status: PlatformStatusCode?,
            httpCode: Int,
            message: String?
        ) {
            crashlyticsReport?.log("IoMT SDK: on connect to platform ($status)> -> $httpCode $message")
            setListeners(null, null)
        }
    }

    class IoMTException(message: String?) : Exception(message)
}