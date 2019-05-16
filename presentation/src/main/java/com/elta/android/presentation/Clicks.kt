package com.elta.android.presentation

import com.elta.android.presentation.core.bus.Click
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSocialItem
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchResultItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.elta.android.presentation.features.sync.connect.ui.adapter.items.DeviceItem
import java.util.Date

sealed class Clicks : Click {

    data class ShopMakeRoute(val item: ShopItem) : Clicks()
    data class ShopMakeCall(val item: ShopItem) : Clicks()
    data class SearchResult(val item: SearchResultItem) : Clicks()
    data class AddUserEvent(val meta: Any) : Clicks()
    data class RecordClicked(val item: RecordItem) : Clicks()
    data class ChooserOptionClicked(val id: String) : Clicks()
    data class DeviceClicked(val item: DeviceItem) : Clicks()
    data class ProfileAdditionalClicked(val item: MainProfileAdditionalItem) : Clicks()
    data class ProfileIndicatorClicked(val item: MainProfileIndicatorItem.Type) : Clicks()
    data class ProfileSettingsItemClicked(val type: ProfileSettingsItem.Type) : Clicks()
    data class ProfileSettingsSocialItemClicked(val item: ProfileSettingsSocialItem) : Clicks()
    data class ReminderItemClicked(val item: ReminderItem) : Clicks()
    data class DeleteHemoglobinEventClicked(val id: String) : Clicks()
    data class ObserverItemClicked(val item: ObserverItem) : Clicks()
    data class DateInStatisticsClicked(val date: Date?) : Clicks()
}