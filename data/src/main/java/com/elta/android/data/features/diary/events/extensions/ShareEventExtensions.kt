package com.elta.android.data.features.diary.events.extensions

import com.elta.android.domain.features.diary.home.interactor.glucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseSharingInfo
import java.nio.charset.Charset
import java.util.UUID

const val EVENTS_DIR_NAME = "share_events"
const val EVENT_FILE_MASK = "event_"

fun GlucoseSharingInfo.getUniqueShareId(): String {
    val id = event.id
    val level = event.glucoseLevel(glucoseLevelSettings).toString()
    return UUID.nameUUIDFromBytes("$id-$level-$glucoseFormat".toByteArray(Charset.defaultCharset()))
        .toString()
}

fun buildFileName(sharingInfo: GlucoseSharingInfo) =
    "$EVENT_FILE_MASK${sharingInfo.getUniqueShareId()}"
