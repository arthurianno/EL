package com.elta.android.data.features.diary.datasource

import com.elta.android.data.features.diary.api.DiaryApi
import com.elta.android.data.features.diary.dto.event.EventDto
import io.reactivex.Observable
import javax.inject.Inject

class DiaryRemoteDataSource @Inject constructor(
    private val api: DiaryApi
) : DiaryDataSource {

    override fun getEvents(): Observable<List<EventDto>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}