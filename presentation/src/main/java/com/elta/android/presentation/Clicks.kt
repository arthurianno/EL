package com.elta.android.presentation

import com.elta.android.domain.features.user.model.HealthAppType
import com.elta.android.presentation.core.bus.Click
import com.elta.android.presentation.features.devices.all.ui.adapter.items.ActiveDeviceItem
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserItem
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserWithSubtypeItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderItem
import com.elta.android.presentation.features.profile.support.model.SupportAction
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchResultItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.adapter.items.DeviceItem
import org.threeten.bp.LocalDate

sealed class Clicks : Click {

    data class ShopMakeRoute(val item: ShopItem) : Clicks()
    data class ShopMakeCall(val item: ShopItem) : Clicks()
    data class SearchResult(val item: SearchResultItem) : Clicks()
    data class AddUserEvent(val meta: Any) : Clicks()
    data class RecordClicked(val item: RecordItem) : Clicks()
    data class ChooserOptionClicked(val item: ChooserItem) : Clicks()
    data class ChooserWithSubtypesOptionClicked(val item: ChooserWithSubtypeItem) : Clicks()
    data class DeviceClicked(val item: DeviceItem) : Clicks()
    data class ProfileAdditionalClicked(val item: MainProfileAdditionalItem) : Clicks()
    data class ProfileIndicatorClicked(val item: MainProfileIndicatorItem.Type) : Clicks()
    data class ProfileSettingsItemClicked(val type: ProfileSettingsItem.Type) : Clicks()
    data class ProfileSettingsHealthAppItemClicked(val type: HealthAppType) : Clicks()
    data class ReminderItemClicked(val item: ReminderItem) : Clicks()
    data class ActiveDeviceItemClicked(val item: ActiveDeviceItem) : Clicks()
    data class DeleteHemoglobinEventClicked(val id: String) : Clicks()
    data class ObserverItemClicked(val item: ObserverItem) : Clicks()
    data class DateInStatisticsClicked(val date: LocalDate?) : Clicks()
    object PrimaryDeviceItemClicked : Clicks()
    object OpenBlueToothScreen : Clicks()
    data class SupportActionClicked(val action: SupportAction) : Clicks()
}
