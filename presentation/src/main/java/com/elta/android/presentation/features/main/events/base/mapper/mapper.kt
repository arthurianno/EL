package com.elta.android.presentation.features.main.events.base.mapper

import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.presentation.features.main.events.chooser.models.ChooserInsulin
import com.elta.android.presentation.widgets.selector.model.SelectorOption

private const val START_OF_DRUG = "drug="
private const val END_OF_VALUE = ","
private const val START_OF_TYPE = "type="
private const val END_OF_TYPE = ")"
private const val START_OF_PREVIOUS_NAME = "previousName="

internal fun SelectorOption?.toChooserInsulin(): ChooserInsulin? {
    return if (!this?.meta.toString().contains("null")
    ) {
        ChooserInsulin(
            previousName = this?.meta.toString().toPreviousName(),
            drug = this?.meta.toString().toDrugName(),
            type = this?.meta.toString().toInsulinType()
        )
    } else null
}

private fun String.toPreviousName() =
    this.substringAfter(START_OF_PREVIOUS_NAME).substringBefore(END_OF_VALUE)

private fun String.toDrugName() =
    this.substringAfter(START_OF_DRUG).substringBefore(END_OF_VALUE)

private fun String.toInsulinType() = InsulinType.valueOf(
    this.substringAfter(START_OF_TYPE).substringBefore(END_OF_TYPE)
)
