package com.elta.android.domain.features.diary.chooser.interactor

import com.elta.android.domain.features.diary.chooser.model.ChooserOptionModel
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinType
import com.elta.android.domain.features.diary.insulin.MedicinesRepository
import com.elta.android.domain.features.diary.tags.interactor.TagsComparator
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import io.reactivex.Observable

internal fun buildChooserOptions(
    param: GetChooserOptionsUseCase.Params,
    tagsRepository: TagsRepository,
    medicinesRepository: MedicinesRepository
) = when {

    param.chooserType == ChooserType.GROUP_TAGS ->
        tagsRepository.getTags().map(::mapTags)

    param.chooserType == ChooserType.VARIANTS_WITH_SUBTYPE && param.eventType == EventType.INSULIN ->
        medicinesRepository.getInsulinTypes().map { it.mapInsulinTypes() }

    param.chooserType == ChooserType.VARIANTS && param.eventType == EventType.ACTIVITY ->
        Observable.just(ActivityType.values()).map(::mapActivityTypes)

    param.chooserType == ChooserType.VARIANTS && param.eventType == EventType.INSULIN ->
        param.medicamentInsulinType?.let {
            medicinesRepository.getMedicines(it).map { medicineInsulinTypes ->
                medicineInsulinTypes.toChooserOption()
            }
        }
            ?: error("Doesn't have type in parameters")
    else ->
        throw IllegalStateException("Unresolved case for eventType:${param.eventType} and chooserType ${param.chooserType}")
}

internal fun mapTags(list: List<Tag>): List<ChooserOptionModel> =
    list.sortedWith(TagsComparator).map { ChooserOptionModel(it.id, it) }

internal fun List<MedicamentInsulinType>.mapInsulinTypes(): List<ChooserOptionModel> {
    val list = listOf(MedicamentInsulinType.allMedicament()) + this
    return list.map {
        ChooserOptionModel(
            id = it.toString(),
            meta = it
        )
    }
}


internal fun mapActivityTypes(types: Array<ActivityType>): List<ChooserOptionModel> =
    types.map {
        ChooserOptionModel(
            id = it.toString(),
            meta = it
        )
    }

internal fun List<Medicament>.toChooserOption(): List<ChooserOptionModel> =
    map {
        ChooserOptionModel(
            id = it.id.toString(),
            meta = it
        )
    }
