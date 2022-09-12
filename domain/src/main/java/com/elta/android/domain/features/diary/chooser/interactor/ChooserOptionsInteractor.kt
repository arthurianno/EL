package com.elta.android.domain.features.diary.chooser.interactor

import com.elta.android.domain.features.diary.chooser.model.ChooserOptionModel
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.insulin.InsulinDrugNameRepository
import com.elta.android.domain.features.diary.tags.interactor.TagsComparator
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import io.reactivex.Observable

internal fun buildChooserOptions(
    param: GetChooserOptionsUseCase.Params,
    tagsRepository: TagsRepository,
    insulinRepo: InsulinDrugNameRepository
) = when {
    param.chooserType == ChooserType.GROUP_TAGS ->
        tagsRepository.getTags().map(::mapTags)
    param.chooserType == ChooserType.VARIANTS_WITH_SUBTYPE && param.eventType == EventType.INSULIN ->
        Observable.just(InsulinType.values()).map(::mapInsulinTypes)
    param.chooserType == ChooserType.VARIANTS && param.eventType == EventType.ACTIVITY ->
        Observable.just(ActivityType.values()).map(::mapActivityTypes)
    param.chooserType == ChooserType.VARIANTS && param.eventType == EventType.INSULIN ->
        param.insulinType?.let {
            insulinRepo.getDrugNamesByInsulinType(it).map(::mapInsulinDrugNames)
        }
            ?: error("Doesn't have type in parameters")
    else ->
        throw IllegalStateException("Unresolved case for eventType:${param.eventType} and chooserType ${param.chooserType}")
}

internal fun mapTags(list: List<Tag>): List<ChooserOptionModel> =
    list.sortedWith(TagsComparator).map { ChooserOptionModel(it.id, it) }

internal fun mapInsulinTypes(types: Array<InsulinType>): List<ChooserOptionModel> =
    types.map {
        ChooserOptionModel(
            id = it.toString(),
            meta = it
        )
    }.toMutableList()

internal fun mapActivityTypes(types: Array<ActivityType>): List<ChooserOptionModel> =
    types.map {
        ChooserOptionModel(
            id = it.toString(),
            meta = it
        )
    }.toMutableList()

internal fun mapInsulinDrugNames(list: List<String>): List<ChooserOptionModel> =
    list.map {
        ChooserOptionModel(
            id = it,
            meta = it
        )
    }
