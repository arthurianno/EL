package com.elta.android.presentation.features.main.records.ui.adapter.holder

import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemRecordsHeaderBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.toGlucoseDashboardUiState
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardScreen
import com.nullgr.core.rx.RxBus

class ItemRecordsHeaderViewHolder(
    private val binding: ItemRecordsHeaderBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<RecordsHeaderItem>(binding.root) {

    init {
        binding.composeHeaderView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

    override fun bind(item: RecordsHeaderItem) {
        binding.composeHeaderView.setContent {
            GlucoseDashboardScreen(
                bus = bus,
                uiState = item.toGlucoseDashboardUiState(binding.root.context)
            )
        }
    }
}
