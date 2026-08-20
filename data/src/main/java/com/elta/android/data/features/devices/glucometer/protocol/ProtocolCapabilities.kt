package com.elta.android.data.features.devices.glucometer.protocol

import com.elta.android.data.features.devices.dto.VersionDto

internal data class ProtocolCapabilities(
    val supportsGetMem: Boolean,
    val supportsGetZone: Boolean,
    val supportsSetZone: Boolean,
    val supportsGetError: Boolean
) {
    companion object {
        val Legacy = ProtocolCapabilities(
            supportsGetMem = false,
            supportsGetZone = false,
            supportsSetZone = false,
            supportsGetError = false
        )
    }
}

internal object ProtocolCapabilitiesResolver {
    fun resolve(version: VersionDto, deviceName: String?): ProtocolCapabilities {
        val softwareVersion = VersionNumber.parse(version.software) ?: return ProtocolCapabilities.Legacy
        val model = ProtocolModel.fromDeviceName(deviceName)
        val supportsExtendedCommands = when (model) {
            ProtocolModel.SatelliteOnline -> softwareVersion >= ONLINE_MIN_VERSION
            ProtocolModel.SatelliteExpress -> softwareVersion >= EXPRESS_MIN_VERSION
            ProtocolModel.SatelliteVoice -> softwareVersion >= VOICE_MIN_VERSION
            ProtocolModel.Unknown -> softwareVersion >= VOICE_MIN_VERSION
        }

        return if (supportsExtendedCommands) {
            ProtocolCapabilities(
                supportsGetMem = true,
                supportsGetZone = true,
                supportsSetZone = true,
                supportsGetError = true
            )
        } else {
            ProtocolCapabilities.Legacy
        }
    }
}


private enum class ProtocolModel {
    SatelliteOnline,
    SatelliteExpress,
    SatelliteVoice,
    Unknown;

    companion object {
        fun fromDeviceName(deviceName: String?): ProtocolModel {
            val normalizedName = deviceName.orEmpty()
            return when {
                normalizedName.startsWith(SATELLITE_ONLINE_PREFIX, ignoreCase = true) -> SatelliteOnline
                normalizedName.startsWith(SATELLITE_EXPRESS_PREFIX, ignoreCase = true) -> SatelliteExpress
                normalizedName.startsWith(SATELLITE_VOICE_PREFIX, ignoreCase = true) -> SatelliteVoice
                else -> Unknown
            }
        }
    }
}

private data class VersionNumber(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<VersionNumber> {
    override fun compareTo(other: VersionNumber): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        return patch.compareTo(other.patch)
    }

    companion object {
        fun parse(source: String?): VersionNumber? {
            if (source.isNullOrBlank()) return null
            val match = VERSION_REGEX.find(source) ?: return null
            return VersionNumber(
                major = match.groupValues[1].toInt(),
                minor = match.groupValues[2].toInt(),
                patch = match.groupValues[3].toInt()
            )
        }
    }
}

private val VERSION_REGEX = Regex("""(\d+)\.(\d+)\.(\d+)""")

private val ONLINE_MIN_VERSION = VersionNumber(4, 1, 7)
private val EXPRESS_MIN_VERSION = VersionNumber(2, 0, 0)
private val VOICE_MIN_VERSION = VersionNumber(5, 0, 0)

private const val SATELLITE_ONLINE_PREFIX = "SatelliteOnline"
private const val SATELLITE_EXPRESS_PREFIX = "SatelliteExpress"
private const val SATELLITE_VOICE_PREFIX = "SatelliteVoice"
