package com.elta.android.presentation.features.main.events.chooser.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemChooserBinding
import com.elta.android.presentation.databinding.ItemChooserHeaderBinding
import com.elta.android.presentation.features.main.events.chooser.adapter.holder.ChooserHeaderViewHolder
import com.elta.android.presentation.features.main.events.chooser.adapter.holder.ChooserItemViewHolder
import com.elta.android.presentation.features.main.events.chooser.adapter.holder.ChooserWithSubtypeItemViewHolder
import com.elta.android.presentation.features.main.events.chooser.adapter.items.ChooserHeaderItem
import com.elta.android.presentation.features.main.events.chooser.adapter.items.ChooserItem
import com.elta.android.presentation.features.main.events.chooser.adapter.items.ChooserWithSubtypeItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class EventOptionsChooseAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ChooserHeaderItem::class.java.hashCode() -> {
                ChooserHeaderViewHolder(
                    ItemChooserHeaderBinding.inflate(inflater, parent, false)
                )
            }
            ChooserItem::class.java.hashCode() -> {
                ChooserItemViewHolder(
                    ItemChooserBinding.inflate(inflater, parent, false),
                    bus
                )
            }
            ChooserWithSubtypeItem::class.java.hashCode() -> {
                ChooserWithSubtypeItemViewHolder(
                    ItemChooserBinding.inflate(inflater, parent, false),
                    bus
                )
            }
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}
