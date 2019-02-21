package com.elta.android.data.features.diary.events.repository

import com.elta.android.data.features.diary.events.datasource.EventsDataSource
import javax.inject.Inject

class EventsDataRepository @Inject constructor(
    private val source: EventsDataSource
)