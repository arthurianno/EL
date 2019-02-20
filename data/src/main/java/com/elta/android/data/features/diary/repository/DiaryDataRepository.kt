package com.elta.android.data.features.diary.repository

import com.elta.android.data.features.diary.datasource.DiaryDataSource
import javax.inject.Inject

class DiaryDataRepository @Inject constructor(
    private val source: DiaryDataSource
)