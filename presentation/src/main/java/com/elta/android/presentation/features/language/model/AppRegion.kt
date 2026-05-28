package com.elta.android.presentation.features.language.model

import com.elta.android.presentation.R

/**
 * Список регионов, доступных для выбора.
 *
 * [code] — строковый код, передаваемый на бэкенд.
 * [displayNameResId] — строковый ресурс для отображения в UI.
 * [isFirstLaunchOnly] — true, если регион показывается только на экране первого запуска,
 *                       но не в настройках профиля.
 */
enum class AppRegion(
    val code: String,
    val displayNameResId: Int,
    val isFirstLaunchOnly: Boolean = false
) {
    RUSSIA("RU", R.string.region_russia),
    KAZAKHSTAN("KZ", R.string.region_kazakhstan),
    INDIA("IN", R.string.region_india),
    UZBEKISTAN("UZ", R.string.region_uzbekistan),
    BELARUS("BY", R.string.region_belarus),
    TURKEY("TR", R.string.region_turkey, isFirstLaunchOnly = true),
    CHINA("CN", R.string.region_china, isFirstLaunchOnly = true),
    AFRICA("AF", R.string.region_africa, isFirstLaunchOnly = true);

    companion object {

        /** Регионы для экрана первого запуска */
        fun firstLaunchRegions(): List<AppRegion> = entries

        /** Регионы для экрана настроек профиля */
        fun settingsRegions(): List<AppRegion> = entries.filter { !it.isFirstLaunchOnly }

        fun fromCode(code: String?): AppRegion =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: RUSSIA
    }
}

val AppRegion.isProductDatabaseSupported: Boolean
    get() = this == AppRegion.RUSSIA ||
            this == AppRegion.KAZAKHSTAN ||
            this == AppRegion.UZBEKISTAN ||
            this == AppRegion.BELARUS

