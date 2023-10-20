package com.elta.android.presentation.features.main.events.chooser.ui.adapter.items

import com.elta.android.presentation.features.main.events.chooser.models.MedicamentChooser
import com.nullgr.core.adapter.items.ListItem

data class ChooserWithSubtypeItem(
    val id: String,
    val title: String,
    val iconId: Int?,
    val meta: Any,
    val medicament: MedicamentChooser?,
    val isSelectedType: Boolean,
) : ListItem {
    override fun getUniqueProperty() = id
}
