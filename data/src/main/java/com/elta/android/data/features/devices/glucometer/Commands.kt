package com.elta.android.data.features.devices.glucometer

import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.diary.events.model.Event
import org.threeten.bp.ZonedDateTime

object Commands {
    object Reset : GlucometerCommand {
        override fun toGlucometerString(): String = "reset"
    }

    object ToDfuMode : GlucometerCommand {
        override fun toGlucometerString(): String = "boot"
    }

    object GetDate : GlucometerCommand {
        override fun toGlucometerString(): String = "time"
    }

    object GetVersion : GlucometerCommand {
        override fun toGlucometerString(): String = "ver"
    }

    object GetBatteryAndTemperature : GlucometerCommand {
        override fun toGlucometerString(): String = "bat"
    }

    object TurnOnAntiLossMode : GlucometerCommand {
        override fun toGlucometerString(): String = "lon"
    }

    object TurnOffAntiLossMode : GlucometerCommand {
        override fun toGlucometerString(): String = "loff"
    }

    object TurnOnFindMode : GlucometerCommand {
        override fun toGlucometerString(): String = "find"
    }

    data class SetTime(val date: ZonedDateTime) : GlucometerCommand {
        override fun toGlucometerString(): String = "settime.${date.toStringWithFormat("yyMMddHHmmss")}"
    }

    data class AddEvent(val event: Event) : GlucometerCommand {
        override fun toGlucometerString(): String = "blood.296044"
    }

    data class ReadEvent(val cell: Int) : GlucometerCommand {
        @Suppress("MagicNumber")
        override fun toGlucometerString(): String = "rd.${cell.toString().padStart(3, '0')}"
    }

    data class SetPin(val pin: String) : GlucometerCommand {
        override fun toGlucometerString(): String = "pin.$pin"
    }
}