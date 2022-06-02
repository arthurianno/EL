package com.elta.android.presentation.features.profile.settings.global.ui.adapter.items

import androidx.annotation.DrawableRes
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.nullgr.core.adapter.items.ListItem

data class ProfileSettingsSocialItem(
    @DrawableRes
    val networkIcon: Int,
    val title: String,
    val isLinked: Boolean,
    val type: SocialNetworkType
) : ListItem {

    override fun getUniqueProperty() = title

    override fun getChangePayload(other: ListItem): Any {
        if (other is ProfileSettingsSocialItem && isLinked != other.isLinked) {
            return Payload.NETWORK_LINKING_CHANGED
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        NETWORK_LINKING_CHANGED
    }
}
