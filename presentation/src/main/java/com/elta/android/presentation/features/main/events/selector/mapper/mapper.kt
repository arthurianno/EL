package com.elta.android.presentation.features.main.events.selector.mapper

import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.presentation.features.main.events.selector.model.EventSelectorUi

fun List<Medicament>.toUi(idSelectedElement: Long? = null) = map {
    it.toUi(idSelectedElement)
}

fun Medicament.toUi(idSelectedElement: Long? = null, otherName: String? = null): EventSelectorUi =
    EventSelectorUi(
        id = id,
        name = otherName ?: name,
        hasHint = isOther,
        isVisible = true,
        isSelected = id == idSelectedElement,
        meta = this
    )
