package com.elta.android.presentation.features.observers.all.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemObserverHeaderBinding
import com.elta.android.presentation.databinding.ItemProfileAdditionalSettingsBinding
import com.elta.android.presentation.features.observers.all.ui.adapter.holder.ObserverHeaderViewHolder
import com.elta.android.presentation.features.observers.all.ui.adapter.holder.ObserverItemViewHolder
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverHeaderItem
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class ObserverAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ObserverItem::class.java.hashCode() -> {
                ObserverItemViewHolder(
                    ItemProfileAdditionalSettingsBinding.inflate(inflater, parent, false),
                    bus
                )
            }
            ObserverHeaderItem::class.java.hashCode() -> {
                ObserverHeaderViewHolder(
                    ItemObserverHeaderBinding.inflate(inflater, parent, false)
                )
            }
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}
