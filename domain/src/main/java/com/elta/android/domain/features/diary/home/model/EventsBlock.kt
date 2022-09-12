package com.elta.android.domain.features.diary.home.model

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.tags.model.Tag

data class EventsBlock(
    val tag: Tag?,
    val events: List<Event>
)
