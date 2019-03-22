package com.elta.android.presentation.features.profile.main.ui.builder

import android.support.annotation.StringRes
import com.elta.android.domain.features.user.model.MyObservers
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.WhereBuy
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileHeaderAdditionalItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.toString
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class MainProfileOptionsItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {

    fun buildItems(profile: Profile) = arrayListOf<ListItem>().apply {
        add(createIndicatorItem(profile))
        add(MainProfileHeaderAdditionalItem)
        addAll(createMainProfileAdditionalItems())
    }

    private fun createIndicatorItem(profile: Profile): MainProfileIndicatorItem {
        with(profile) {
            val glucoseLevelMin = createGlucoseLevelText(glucoseLevelSettings?.normal?.start)
            val glucoseLevelMax = createGlucoseLevelText(glucoseLevelSettings?.normal?.end)
            val diabetesType = diabetes?.toString(resources)
                ?: resources.getString(R.string.profile_main_empty_value)
            val weight = createIndicatorText(weight, R.string.profile_weight_value)
            val hemoglobin = createIndicatorText(hba1cLevel, R.string.profile_hba1c_value)
            return MainProfileIndicatorItem(glucoseLevelMin, glucoseLevelMax, diabetesType,
                weight, hemoglobin)
        }
    }

    private fun createMainProfileAdditionalItems(): List<ListItem> {
        val items = mutableListOf<ListItem>()
        items.add(MainProfileAdditionalItem(R.string.profile_my_watchers, R.string.profile_management_and_settings,
            R.drawable.ic_doctor, MyObservers)
        )
        items.add(MainProfileAdditionalItem(R.string.profile_where_purchase_products, R.string.profile_map_of_stores,
            R.drawable.ic_map_pin_card, WhereBuy)
        )
        return items
    }

    private fun createGlucoseLevelText(item: Any?) =
        NumberFormatter.numberFormat.format(item)
            ?: resources.getString(R.string.profile_main_empty_value)

    private fun createIndicatorText(item: Any?, @StringRes string: Int) =
        item?.let { resources.getString(string, NumberFormatter.numberFormat.format(item)) }
            ?: resources.getString(R.string.profile_main_empty_value)
}