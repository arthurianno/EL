package com.elta.android.data.features.diary.datasource

import com.elta.android.data.features.diary.dto.event.EventDto
import io.reactivex.Observable

interface DiaryDataSource {

    fun getEvents(): Observable<List<EventDto>>
}