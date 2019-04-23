package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinHeaderItem
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinItem
import com.elta.android.presentation.utils.NumberFormatter
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.date.CommonFormats
import com.nullgr.core.date.toStringWithFormat
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class HemoglobinEventsMapper @Inject constructor(
    private val resourceProvider: ResourceProvider
) : Mapper<Event, ListItem> {

    override fun mapFromObjects(sources: Collection<Event>): List<ListItem> {
        if (sources.isEmpty()) return emptyList()
        val resultList = arrayListOf<ListItem>()
        resultList.add(
            HemoglobinHeaderItem(resourceProvider.getString(R.string.profile_settings_hemoglobin_header))
        )
        resultList.addAll(sources.map { mapFromObject(it) })
        return resultList
    }

    override fun mapFromObject(source: Event) =
        HemoglobinItem(
            value = resourceProvider.getString(
                R.string.profile_settings_hemoglobin_percent_mask,
                NumberFormatter.format(source.value ?: 0.0)
            ),
            date = resourceProvider.getString(
                R.string.profile_settings_hemoglobin_date_mask,
                source.additionTime.toStringWithFormat(CommonFormats.FORMAT_SIMPLE_DATE)
            ),
            id = source.id
        )
}