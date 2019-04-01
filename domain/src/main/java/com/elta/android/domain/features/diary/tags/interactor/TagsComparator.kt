package com.elta.android.domain.features.diary.tags.interactor

import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.diary.tags.model.TagImage

/**
 * Compares two [Tag] object by theirs [TagImage].
 * Order defined in class.
 *
 * Note: Supports only default tags, if custom tags will be added to project add new comparator.
 */
object TagsComparator : Comparator<Tag> {

    private val order = listOf(
        TagImage.BREAKFAST,
        TagImage.LUNCH,
        TagImage.SNACK,
        TagImage.DINNER,
        TagImage.TRAINING,
        TagImage.WORK,
        TagImage.LEISURE,
        TagImage.SPECIALEVENT,
        TagImage.NIGHT
    )

    override fun compare(o1: Tag, o2: Tag): Int =
        order.indexOf(o1.image).compareTo(order.indexOf(o2.image))

}