package com.elta.android.domain.features.devices.model

import com.elta.android.domain.features.diary.events.model.Event
import java.util.Date

sealed class Command(val params: Any? = null) {

    object Reset: Command()
    object ToDfuMode: Command()
    class SetTime(params: Date): Command(params)
    class AddEvent(params: Event): Command(params)
    class ReadEvent(params: Int): Command(params)
    object GetDate: Command()
    object GetVersion: Command()
    object TurnOnAntiLossMode: Command()
    object TurnOffAntiLossMode: Command()
    object TurnOnFindMode: Command()
    object GetBatteryAndTemperature: Command()
    class SetPin(params: Int): Command(params)
}