package com.elta.android.data.features.diary.events.extensions

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.home.interactor.glucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import java.nio.charset.Charset
import java.util.UUID

const val EVENTS_DIR_NAME = "share_events"
const val EVENT_FILE_MASK = "event_"

fun Event.getUniqueShareId(levelSettings: GlucoseLevelSettings): String {
    val id = this.id
    val level = this.glucoseLevel(levelSettings).toString()
    return UUID.nameUUIDFromBytes("$id-$level".toByteArray(Charset.defaultCharset())).toString()
}

fun buildFileName(event: Event, levelSettings: GlucoseLevelSettings) =
    "$EVENT_FILE_MASK${event.getUniqueShareId(levelSettings)}"