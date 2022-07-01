package com.elta.android.presentation.features.home.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemUserEventBinding
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class HomeBottomSheetAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == UserEventItem::class.java.hashCode()) {
            UserEventViewHolder(
                ItemUserEventBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                bus
            )
        } else {
            throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
}
