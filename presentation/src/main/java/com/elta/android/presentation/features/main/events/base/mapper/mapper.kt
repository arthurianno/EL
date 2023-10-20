package com.elta.android.presentation.features.main.events.base.mapper

import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.presentation.features.main.events.chooser.models.MedicamentChooser
import com.elta.android.presentation.widgets.selector.model.SelectorOption


internal fun SelectorOption?.toChooserInsulin(): MedicamentChooser? {
    return if (!this?.meta.toString().contains("null")
    ) {
        val medicament = this?.meta as Medicament
        return MedicamentChooser(
            medicamentId = medicament.id,
            medicamentName = medicament.name,
            insulinCode = medicament.insulinType.code,
            insulinName = medicament.insulinType.name,
            insulinId = medicament.insulinType.id,
        )
    } else null
}
