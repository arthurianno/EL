package com.elta.android.data.features.devices.glucometer.command

import com.elta.android.data.features.devices.glucometer.toGlucometerDateTime
import com.elta.android.domain.features.diary.events.model.EventV2
import org.threeten.bp.ZonedDateTime
import java.nio.charset.Charset

sealed class Commands(val command: String) {

    fun getByteCommand(): ByteArray {
        return command.toByteArray(Charset.defaultCharset())
    }

    data object ToDfuMode : Commands(command = "boot")

    data object GetDate : Commands(command = "gettime")

    data object GetVersion : Commands(command = "version")

    data object GetBatteryAndTemperature : Commands(command = "battery")

    data object TurnOnFindMode : Commands(command = "find")

    data class SetTime(val date: ZonedDateTime) :
        Commands("settime.${date.toGlucometerDateTime()}")

    data class ReadEvent(val cell: Int) :
        Commands(command = "rd.${cell.toString().padStart(3, '0')}")

    data class SetPin(val pin: String) : Commands(command = "pin.$pin")

    data object Serial : Commands(command = "serial")

    data object Reset : Commands(command = "reset") //TODO: delete?

    data class AddEvent(val event: EventV2) : Commands(command = "blood.296044") //TODO: delete?

    data object TurnOnAntiLossMode : Commands(command = "lon") //TODO: delete?

    data object TurnOffAntiLossMode : Commands(command = "loff") //TODO: delete?
}
