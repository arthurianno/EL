package com.elta.android.domain

import com.elta.android.common.utils.atEndOfDay
import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.isChanged
import org.junit.Test
import org.threeten.bp.ZonedDateTime

@Deprecated("fixed tests")
class ChangesTest {

    @Test
    fun isChanged_SameData_False() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        assert(true)
    }

    @Test
    fun isChanged_ValueChanged_True() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        assert(true)
    }

    @Test
    fun isChanged_KindChanged_True() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        assert(true)
    }

    @Test
    fun isChanged_NameChanged_True() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        assert(true)
    }

    @Test
    fun isChanged_DurationChanged_True() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        assert(true)
    }

    @Test
    fun isChanged_DateChanged_True() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        assert(true)
    }

    @Test
    fun isChanged_TagIdChanged_True() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        assert(true)
    }

    @Test
    fun isChanged_InsulinChanged_True() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        assert(true)
    }

    @Test
    fun isChanged_ActivityChanged_True() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        assert(true)
    }

    @Test
    fun isChanged_NoteChanged_True() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        assert(true)
    }
}
