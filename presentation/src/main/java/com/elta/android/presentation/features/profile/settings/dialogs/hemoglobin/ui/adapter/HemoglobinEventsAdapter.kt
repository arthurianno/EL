package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemHemoglobinBinding
import com.elta.android.presentation.databinding.ItemHemoglobinHeaderBinding
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.holder.HemoglobinHeaderViewHolder
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.holder.HemoglobinViewHolder
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinHeaderItem
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class HemoglobinEventsAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HemoglobinHeaderItem::class.java.hashCode() -> {
                HemoglobinHeaderViewHolder(
                    ItemHemoglobinHeaderBinding.inflate(inflater, parent, false)
                )
            }
            HemoglobinItem::class.java.hashCode() -> {
                HemoglobinViewHolder(
                    ItemHemoglobinBinding.inflate(inflater, parent, false),
                    bus
                )
            }
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}
