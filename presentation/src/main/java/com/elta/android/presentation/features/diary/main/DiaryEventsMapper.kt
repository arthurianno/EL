package com.elta.android.presentation.features.diary.main

import com.elta.android.common.mapper.Mapper
import com.elta.android.domain.features.diary.home.model.EventsBlock
import com.elta.android.presentation.features.main.records.mapper.BaseRecordsMapper
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class DiaryEventsMapper @Inject constructor(
    resources: ResourceProvider
) : BaseRecordsMapper(resources), Mapper<EventsBlock, ListItem> {

    var expand: Boolean = false

    override fun mapFromObject(source: EventsBlock) = source.group(expand)
}