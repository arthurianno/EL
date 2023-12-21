package com.elta.android.presentation.features.main.events.base.mapper

import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.presentation.features.main.events.chooser.models.InsulinMedicamentChooser
import com.elta.android.presentation.features.main.events.chooser.models.MedicamentChooser
import com.elta.android.presentation.widgets.selector.model.SelectorOption


internal fun SelectorOption?.toChooserInsulin(): InsulinMedicamentChooser? {
    return if (!this?.meta.toString().contains(NULL)
    ) {
        val insulinMedicament = this?.meta as InsulinMedicament
        return InsulinMedicamentChooser(
            medicamentId = insulinMedicament.id,
            medicamentName = insulinMedicament.name,
            insulinCode = insulinMedicament.insulinType.code,
            insulinName = insulinMedicament.insulinType.name,
            insulinId = insulinMedicament.insulinType.id,
        )
    } else null
}

internal fun SelectorOption?.toChooserMedicament(): MedicamentChooser? {
    return when {
        (this?.meta.toString() != NULL) -> {
            val medicament = if (this?.meta is Medicament) {
                this.meta
            } else {
                (this?.meta as Pair<*, *>).first as Medicament
            }
            MedicamentChooser(
                id = medicament.id,
                name = medicament.name,
                isDeleted = medicament.isDeleted,
                isOther = medicament.isOther,
                touchedAt = medicament.touchedAt
            )
        }

        else -> null
    }
}

private const val NULL = "null"
