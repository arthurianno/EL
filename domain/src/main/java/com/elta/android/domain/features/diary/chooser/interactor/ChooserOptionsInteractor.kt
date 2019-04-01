package com.elta.android.domain.features.diary.chooser.interactor

import com.elta.android.domain.features.diary.chooser.model.ChooserOptionModel
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.tags.interactor.TagsComparator
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import io.reactivex.Observable

internal fun buildChooserOptions(
    eventType: EventType,
    chooserType: ChooserType,
    tagsRepository: TagsRepository
) = when {
    chooserType == ChooserType.GROUP_TAGS ->
        tagsRepository.getTags().map(::mapTags)
    chooserType == ChooserType.VARIANTS && eventType == EventType.INSULIN ->
        Observable.just(InsulinType.values()).map(::mapInsulinTypes)
    chooserType == ChooserType.VARIANTS && eventType == EventType.ACTIVITY ->
        Observable.just(ActivityType.values()).map(::mapActivityTypes)
    else ->
        throw IllegalStateException("Unresolved case for eventType:$eventType and chooserType $chooserType")
}

internal fun mapTags(list: List<Tag>): List<ChooserOptionModel> =
    list.sortedWith(TagsComparator).map { ChooserOptionModel(it.id, it) }

internal fun mapInsulinTypes(types: Array<InsulinType>): List<ChooserOptionModel> =
    types.map { ChooserOptionModel(it.toString(), it) }.toMutableList()

internal fun mapActivityTypes(types: Array<ActivityType>): List<ChooserOptionModel> =
    types.map { ChooserOptionModel(it.toString(), it) }.toMutableList()
