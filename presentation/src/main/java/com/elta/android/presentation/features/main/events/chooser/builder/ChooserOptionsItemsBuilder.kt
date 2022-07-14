package com.elta.android.presentation.features.main.events.chooser.builder

import com.elta.android.domain.features.diary.chooser.model.ChooserOptionModel
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.events.chooser.adapter.items.ChooserHeaderItem
import com.elta.android.presentation.features.main.events.chooser.adapter.items.ChooserItem
import com.elta.android.presentation.features.main.events.chooser.adapter.items.ChooserWithSubtypeItem
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class ChooserOptionsItemsBuilder @Inject constructor(
    private val resourceProvider: ResourceProvider
) {

    private val activitiesComparator = ActivitiesComparator(resourceProvider)

    fun buildItems(
        configuration: ChooserConfiguration,
        options: List<ChooserOptionModel>
    ): MutableList<ListItem> {
        return arrayListOf<ListItem>().apply {
            add(ChooserHeaderItem(configuration.toHeaderTitle()))
            if (configuration.eventType == EventType.ACTIVITY) {
                addAll(options.map { mapFromObject(it) }.sortedWith(activitiesComparator))
            } else {
                addAll(options.map { mapFromObject(it) })
            }
        }
    }

    private fun mapFromObject(source: ChooserOptionModel): ListItem =
        when (source.meta) {
            is ActivityType -> mapAsActivityItem(source)
            is InsulinType -> mapAsInsulinItem(source)
            is Tag -> mapAsTagItem(source)
            else -> throw IllegalStateException("Unsupported type ${source::class.java}")
        }

    private fun mapAsActivityItem(source: ChooserOptionModel): ListItem {
        val meta = source.meta as ActivityType
        return ChooserItem(
            id = source.id,
            title = resourceProvider.getString(meta.toName()),
            iconId = meta.toIcon(),
            meta = meta
        )
    }

    private fun mapAsInsulinItem(source: ChooserOptionModel): ListItem {
        val meta = source.meta as InsulinType
        return ChooserWithSubtypeItem(
            id = source.id,
            title = resourceProvider.getString(meta.toName()),
            iconId = null,
            meta = meta
        )
    }

    private fun mapAsTagItem(source: ChooserOptionModel): ListItem {
        val meta = source.meta as Tag
        return ChooserItem(
            id = source.id,
            title = meta.name,
            iconId = meta.toIcon(),
            meta = meta
        )
    }

    private fun ChooserConfiguration.toHeaderTitle(): String =
        resourceProvider.getString(
            when {
                chooserType == ChooserType.VARIANTS && eventType == EventType.INSULIN ->
                    R.string.events_options_chooser_header_variants_insulin
                chooserType == ChooserType.VARIANTS && eventType == EventType.ACTIVITY ->
                    R.string.events_options_chooser_header_variants_activity
                else -> R.string.events_options_chooser_header_tags
            }
        )

    private class ActivitiesComparator(resources: ResourceProvider) : Comparator<ListItem> {

        private val order = listOf(
            resources.getString(ActivityType.RUNNING.toName()),
            resources.getString(ActivityType.WALKING.toName()),
            resources.getString(ActivityType.SWIMMING.toName()),
            resources.getString(ActivityType.FITNESS.toName()),
            resources.getString(ActivityType.CYCLING.toName()),
            resources.getString(ActivityType.HOUSEKEEPING.toName())
        )

        override fun compare(o1: ListItem, o2: ListItem): Int {
            val a1 = o1 as ChooserItem
            val a2 = o2 as ChooserItem

            val index1 = order.indexOf(a1.title)
            val index2 = order.indexOf(a2.title)

            return when {
                index1 < 0 && index2 < 0 -> a1.title.compareTo(a2.title)
                index1 < 0 -> 1
                index2 < 0 -> -1
                else -> index1.compareTo(index2)
            }
        }
    }
}
