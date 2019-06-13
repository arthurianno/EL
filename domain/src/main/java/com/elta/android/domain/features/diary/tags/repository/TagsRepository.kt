package com.elta.android.domain.features.diary.tags.repository

import com.elta.android.domain.features.diary.tags.model.Tag
import io.reactivex.Completable
import io.reactivex.Observable

interface TagsRepository {

    fun getTags(): Observable<List<Tag>>

    fun sync(): Completable
}