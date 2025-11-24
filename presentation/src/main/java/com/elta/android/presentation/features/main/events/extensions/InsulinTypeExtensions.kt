package com.elta.android.presentation.features.main.events.extensions

import com.elta.android.domain.features.diary.medicines.model.MedicamentInsulinType
import com.elta.android.domain.features.diary.medicines.model.MIXED
import com.elta.android.domain.features.diary.medicines.model.PROLONGED
import com.elta.android.domain.features.diary.medicines.model.SHORT
import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider

/**
 * Extension function to get localized insulin type name from resources
 * based on the insulin type code.
 */
fun MedicamentInsulinType.getLocalizedName(resourceProvider: ResourceProvider): String {
    return when (code) {
        SHORT -> resourceProvider.getString(R.string.insulin_type_short_ultrashort)
        PROLONGED -> resourceProvider.getString(R.string.insulin_type_prolong)
        MIXED -> resourceProvider.getString(R.string.insulin_type_mixed)
        else -> name // Fallback to the stored name if code is not recognized
    }
}

