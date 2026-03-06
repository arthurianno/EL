package com.elta.android.domain.features.devices.model

fun matchesTargetGlucometerName(
    deviceName: String?,
    targetName: String?
): Boolean {
    if (targetName.isNullOrBlank()) return true
    if (deviceName.isNullOrBlank()) return false
    if (deviceName.equals(targetName, ignoreCase = true)) return true

    val target = ParsedGlucometerName.parse(targetName) ?: return false
    val device = ParsedGlucometerName.parse(deviceName) ?: return false
    if (target.suffix != device.suffix) return false

    return when {
        target.model == device.model -> true
        target.model.isOnlineExpressFamily() && device.model.isOnlineExpressFamily() -> true
        else -> false
    }
}

private data class ParsedGlucometerName(
    val model: Model,
    val suffix: String
) {
    companion object {
        fun parse(name: String): ParsedGlucometerName? {
            val model = Model.fromName(name) ?: return null
            if (name.length < SUFFIX_LENGTH) return null
            val suffix = name.takeLast(SUFFIX_LENGTH)
            if (!suffix.all(Char::isDigit)) return null
            return ParsedGlucometerName(model = model, suffix = suffix)
        }
    }
}

private enum class Model {
    SatelliteOnline,
    SatelliteExpress,
    SatelliteVoice;

    fun isOnlineExpressFamily(): Boolean =
        this == SatelliteOnline || this == SatelliteExpress

    companion object {
        fun fromName(name: String): Model? {
            return when {
                name.startsWith(SATELLITE_ONLINE_PREFIX, ignoreCase = true) -> SatelliteOnline
                name.startsWith(SATELLITE_EXPRESS_PREFIX, ignoreCase = true) -> SatelliteExpress
                name.startsWith(SATELLITE_VOICE_PREFIX, ignoreCase = true) -> SatelliteVoice
                else -> null
            }
        }
    }
}

private const val SUFFIX_LENGTH = 4
private const val SATELLITE_ONLINE_PREFIX = "SatelliteOnline"
private const val SATELLITE_EXPRESS_PREFIX = "SatelliteExpress"
private const val SATELLITE_VOICE_PREFIX = "SatelliteVoice"
