package com.elta.android.presentation.features.profile.main.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemProfileFunctionsBinding
import com.elta.android.presentation.databinding.ItemProfileHeaderBinding
import com.elta.android.presentation.databinding.ItemProfileIndicatorsBinding
import com.elta.android.presentation.features.profile.main.ui.adapter.holder.MainProfileAdditionalViewHolder
import com.elta.android.presentation.features.profile.main.ui.adapter.holder.MainProfileHeaderViewHolder
import com.elta.android.presentation.features.profile.main.ui.adapter.holder.MainProfileIndicatorViewHolder
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileHeaderItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class MainProfileAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MainProfileIndicatorItem::class.java.hashCode() -> MainProfileIndicatorViewHolder(
                ItemProfileIndicatorsBinding.inflate(inflater, parent, false),
                bus
            )
            MainProfileHeaderItem::class.java.hashCode() -> MainProfileHeaderViewHolder(
                ItemProfileHeaderBinding.inflate(inflater, parent, false)
            )
            MainProfileAdditionalItem::class.java.hashCode() -> MainProfileAdditionalViewHolder(
                ItemProfileFunctionsBinding.inflate(inflater, parent, false),
                bus
            )
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}
