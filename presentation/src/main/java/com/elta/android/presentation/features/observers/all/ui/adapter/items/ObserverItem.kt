package com.elta.android.presentation.features.observers.all.ui.adapter.items

import android.support.annotation.DrawableRes
import com.elta.android.domain.features.observers.model.ObserverStatus
import com.nullgr.core.adapter.items.ListItem

data class ObserverItem(
    val id: String,
    @DrawableRes
    val type: Int,
    val title: String,
    val description: String,
    @DrawableRes
    val action: Int,
    val status: ObserverStatus
) : ListItem {

    override fun getUniqueProperty() = id

    override fun getChangePayload(other: ListItem): Any {
        if (other is ObserverItem) {
            return mutableSetOf<Payload>().apply {
                if (status != other.status) add(Payload.STATUS_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        STATUS_CHANGED
    }
}