package com.elta.android.presentation.features.main.events.chooser.models

/**
 * This class provides logic dividing for choosing screen.
 * What combinations it supports.
 * 1) For all types of events GROUP_TAGS can be used. (Тег для группировки)
 * 2) VARIANTS can be only used with such event types like : ACTIVITY and INSULIN
 */
enum class ChooserType {
    GROUP_TAGS, VARIANTS
}