package com.elta.android.presentation.features.profile.support.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemSupportActionBinding
import com.elta.android.presentation.databinding.ItemSupportHeaderBinding
import com.elta.android.presentation.databinding.ItemSupportVersionBinding
import com.elta.android.presentation.features.profile.support.ui.adapter.holder.SupportActionViewHolder
import com.elta.android.presentation.features.profile.support.ui.adapter.holder.SupportHeaderViewHolder
import com.elta.android.presentation.features.profile.support.ui.adapter.holder.SupportVersionViewHolder
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportActionItem
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportHeaderItem
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportVersionItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class SupportAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SupportHeaderItem::class.java.hashCode() -> SupportHeaderViewHolder(
                ItemSupportHeaderBinding.inflate(inflater, parent, false)
            )
            SupportActionItem::class.java.hashCode() -> SupportActionViewHolder(
                ItemSupportActionBinding.inflate(inflater, parent, false),
                bus
            )
            SupportVersionItem::class.java.hashCode() -> SupportVersionViewHolder(
                ItemSupportVersionBinding.inflate(inflater, parent, false),
                bus // Передаём bus в SupportVersionViewHolder
            )
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}