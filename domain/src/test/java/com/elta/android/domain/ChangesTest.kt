package com.elta.android.domain

import com.elta.android.common.utils.atEndOfDay
import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.events.model.isChanged
import org.junit.Test
import org.threeten.bp.ZonedDateTime

class ChangesTest {

    @Test
    fun isChanged_SameData_False() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        assert(
            !event.isChanged(
                value = event.value,
                kind = event.kind,
                name = event.name,
                duration = event.duration,
                date = event.additionTime,
                tagId = event.tagId,
                medicament = event.medicament,
                activity = event.activityType,
                note = event.note
            )
        )
    }

    @Test
    fun isChanged_ValueChanged_True() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        assert(
            event.isChanged(
                value = 1000.0,
                kind = event.kind,
                name = event.name,
                duration = event.duration,
                date = event.additionTime,
                tagId = event.tagId,
                medicament = event.medicament,
                activity = event.activityType,
                note = event.note
            )
        )
    }

    @Test
    fun isChanged_KindChanged_True() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        assert(
            event.isChanged(
                value = event.value,
                kind = null,
                name = event.name,
                duration = event.duration,
                date = event.additionTime,
                tagId = event.tagId,
                medicament = event.medicament,
                activity = event.activityType,
                note = event.note
            )
        )
    }

    @Test
    fun isChanged_NameChanged_True() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        assert(
            event.isChanged(
                value = event.value,
                kind = event.kind,
                name = null,
                duration = event.duration,
                date = event.additionTime,
                tagId = event.tagId,
                medicament = event.medicament,
                activity = event.activityType,
                note = event.note
            )
        )
    }

    @Test
    fun isChanged_DurationChanged_True() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        assert(
            event.isChanged(
                value = event.value,
                kind = event.kind,
                name = event.name,
                duration = null,
                date = event.additionTime,
                tagId = event.tagId,
                medicament = event.medicament,
                activity = event.activityType,
                note = event.note
            )
        )
    }

    @Test
    fun isChanged_DateChanged_True() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        assert(
            event.isChanged(
                value = event.value,
                kind = event.kind,
                name = event.name,
                duration = event.duration,
                date = ZonedDateTime.now().atEndOfDay(),
                tagId = event.tagId,
                medicament = event.medicament,
                activity = event.activityType,
                note = event.note
            )
        )
    }

    @Test
    fun isChanged_TagIdChanged_True() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        assert(
            event.isChanged(
                value = event.value,
                kind = event.kind,
                name = event.name,
                duration = event.duration,
                date = event.additionTime,
                tagId = "new tag id",
                medicament = event.medicament,
                activity = event.activityType,
                note = event.note
            )
        )
    }

    @Test
    fun isChanged_InsulinChanged_True() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        assert(
            event.isChanged(
                value = event.value,
                kind = event.kind,
                name = event.name,
                duration = event.duration,
                date = event.additionTime,
                tagId = event.tagId,
                medicament = event.medicament,
                activity = event.activityType,
                note = event.note
            )
        )
    }

    @Test
    fun isChanged_ActivityChanged_True() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        assert(
            event.isChanged(
                value = event.value,
                kind = event.kind,
                name = event.name,
                duration = event.duration,
                date = event.additionTime,
                tagId = event.tagId,
                medicament = event.medicament,
                activity = ActivityType.ANOTHER,
                note = event.note
            )
        )
    }

    @Test
    fun isChanged_NoteChanged_True() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        assert(
            event.isChanged(
                value = event.value,
                kind = event.kind,
                name = event.name,
                duration = event.duration,
                date = event.additionTime,
                tagId = event.tagId,
                medicament = event.medicament,
                activity = event.activityType,
                note = null
            )
        )
    }
}
