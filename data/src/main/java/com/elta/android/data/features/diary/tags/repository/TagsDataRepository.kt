package com.elta.android.data.features.diary.tags.repository

import com.elta.android.data.features.diary.tags.datasource.TagsDataSource
import javax.inject.Inject

class TagsDataRepository @Inject constructor(
    private val source: TagsDataSource
)