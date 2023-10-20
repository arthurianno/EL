package com.elta.android.presentation.features.main.events.chooser.ui.builder

import com.elta.android.domain.features.diary.chooser.model.ChooserOptionModel
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinType
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.MedicamentChooser
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
            addAll(options.map { mapFromObject(it, configuration) })
        }
    }

    private fun mapFromObject(source: ChooserOptionModel, config: ChooserConfiguration): ListItem =
        when (source.meta) {
            is ActivityType -> mapAsActivityItem(source, config.id)
            is MedicamentInsulinType -> mapAsInsulinItem(source, config.medicament)
            is Tag -> mapAsTagItem(source)
            is Medicament -> mapAsInsulinNameItem(source, config.medicament?.medicamentId)
            else -> throw IllegalStateException("Unsupported type ${source::class.java}")
        }

    private fun mapAsInsulinNameItem(source: ChooserOptionModel, medicamentId: Int?): ListItem {
        val medicament = source.meta as Medicament
        return ChooserItem(
            id = source.id,
            title = medicament.name,
            iconId = null,
            meta = source.meta,
            isSelected = medicamentId == medicament.id
        )
    }

    private fun mapAsActivityItem(source: ChooserOptionModel, id: String?): ListItem {
        val activity = source.meta as ActivityType
        return ChooserItem(
            id = source.id,
            title = resourceProvider.getString(activity.toName()),
            iconId = activity.toIcon(),
            meta = activity,
            isSelected = activity.name == id
        )
    }

    private fun mapAsInsulinItem(source: ChooserOptionModel, medicament: MedicamentChooser?): ListItem {
        val insulinType = source.meta as MedicamentInsulinType
        return ChooserWithSubtypeItem(
            id = source.id,
            title = insulinType.name,
            iconId = null,
            meta = insulinType,
            medicament = medicament,
            isSelectedType = insulinType.id == medicament?.insulinId,
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
