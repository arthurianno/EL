package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.mapper

import com.elta.android.common.utils.CommonFormats
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinHeaderItem
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinItem
import com.elta.android.presentation.utils.NumberFormatter
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class HemoglobinItemsBuilder @Inject constructor(private val resourceProvider: ResourceProvider) {

    fun buildItems(events: Collection<Event>): List<ListItem> {
        if (events.isEmpty()) return emptyList()
        val resultList = arrayListOf<ListItem>()
        resultList.add(HemoglobinHeaderItem(resourceProvider.getString(R.string.profile_settings_hemoglobin_header)))
        resultList.addAll(events.map { it.toItem() })
        return resultList
    }

    private fun Event.toItem() =
        HemoglobinItem(
            value = resourceProvider.getString(
                R.string.profile_settings_hemoglobin_percent_mask,
                NumberFormatter.format(value ?: 0.0)
            ),
            date = resourceProvider.getString(
                R.string.profile_settings_hemoglobin_date_mask,
                additionTime.toStringWithFormat(CommonFormats.FORMAT_SIMPLE_DATE)
            ),
            id = id
        )
}