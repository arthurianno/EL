package com.elta.android.domain.features.diary.chooser.model

/**
 * This class provides logic dividing for choosing screen.
 * What combinations it supports.
 * 1) For all types of events GROUP_TAGS can be used. (Тег для группировки)
 * 2) VARIANTS can be only used with such event types like : ACTIVITY and INSULIN
 * 3) VARIANTS_WITH_SUBTYPE can be used for events with hard system
 */
enum class ChooserType {
    GROUP_TAGS, VARIANTS, VARIANTS_WITH_SUBTYPE
}
