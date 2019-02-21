package com.elta.android.data.features.diary.datasource

import com.elta.android.data.features.diary.dto.event.EventDto
import com.elta.android.data.features.diary.dto.tag.TagDto
import io.reactivex.Observable
import java.util.Date

interface DiaryDataSource {

    fun getEvents(): Observable<List<EventDto>>

    fun getEvents(start: Date, end: Date): Observable<List<EventDto>>

    fun getTags(): Observable<List<TagDto>>
}