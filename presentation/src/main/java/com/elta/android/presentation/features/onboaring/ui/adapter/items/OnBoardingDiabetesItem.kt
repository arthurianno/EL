package com.elta.android.presentation.features.onboaring.ui.adapter.items

import com.elta.android.domain.features.user.model.Diabetes

enum class DiabetesChoice(val diabetes: Diabetes?) {
    FIRST(Diabetes.FIRST),
    SECOND(Diabetes.SECOND),
    SECOND_TABLETS(Diabetes.SECOND_TABLETS),
    UNKNOWN(null),
    NONE(null)
}

data class OnBoardingDiabetesItem(
    override val title: String,
    val types: List<Diabetes>
) : OnBoardingItem {

    override val data: Any?
        get() = choice

    var choice: DiabetesChoice? = null

    var type: Diabetes?
        get() = choice?.diabetes
        set(value) {
            choice = DiabetesChoice.entries.firstOrNull { it.diabetes != null && it.diabetes == value }
        }
}
