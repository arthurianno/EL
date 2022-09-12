package com.elta.android.presentation.features.main.events.chooser.ui.builder

import com.elta.android.domain.features.diary.chooser.model.ChooserOptionModel
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserHeaderItem
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserItem
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserWithSubtypeItem
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class ChooserOptionsItemsBuilder @Inject constructor(
    private val resourceProvider: ResourceProvider
) {

    fun buildItems(
        configuration: ChooserConfiguration,
        options: List<ChooserOptionModel>
    ): List<ListItem> {
        return mutableListOf<ListItem>().apply {
            add(ChooserHeaderItem(configuration.toHeaderTitle()))
            addAll(options.map { mapFromObject(it) })
        }
    }

    private fun mapFromObject(source: ChooserOptionModel): ListItem =
        when (source.meta) {
            is ActivityType -> mapAsActivityItem(source)
            is InsulinType -> mapAsInsulinItem(source)
            is Tag -> mapAsTagItem(source)
            is String -> mapAsInsulinNameItem(source)
            else -> throw IllegalStateException("Unsupported type ${source::class.java}")
        }

    private fun mapAsInsulinNameItem(source: ChooserOptionModel): ListItem {
        val meta = source.meta as String
        return ChooserItem(
            id = source.id,
            title = source.id,
            iconId = null,
            meta = meta
        )
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
                (chooserType == ChooserType.VARIANTS || chooserType == ChooserType.VARIANTS_WITH_SUBTYPE) && eventType == EventType.INSULIN ->
                    R.string.events_options_chooser_header_variants_insulin
                chooserType == ChooserType.VARIANTS && eventType == EventType.ACTIVITY ->
                    R.string.events_options_chooser_header_variants_activity
                else -> R.string.events_options_chooser_header_tags
            }
        )
}
