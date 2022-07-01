package com.elta.android.presentation.features.profile.settings.global.ui.adapter.items

import androidx.annotation.DrawableRes
import com.elta.android.domain.features.user.model.HealthAppType
import com.nullgr.core.adapter.items.ListItem

data class ProfileSettingsHealthAppItem(
    @DrawableRes
    val icon: Int,
    val title: String,
    val isActive: Boolean,
    val type: HealthAppType
) : ListItem {

    override fun getUniqueProperty() = type

    override fun getChangePayload(other: ListItem): Any {
        if (other is ProfileSettingsHealthAppItem && isActive != other.isActive) {
            return Payload.HEALTH_APP_IS_ACTIVE_CHANGED
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        HEALTH_APP_IS_ACTIVE_CHANGED
    }
}
