package com.elta.android.presentation.features.profile.support.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemSupportVersionBinding
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportVersionItem
import com.nullgr.core.rx.RxBus

class SupportVersionViewHolder(
    private val binding: ItemSupportVersionBinding,
    private val bus: RxBus // Добавляем RxBus для отправки событий
) : BaseListItemViewHolder<SupportVersionItem>(binding.root) {
    override fun bind(item: SupportVersionItem) {
        with(binding) {
            versionTitle.text = item.title
            versionText.text = item.version
            // Добавляем обработчик клика
            if (item.action != null) {
                root.setOnClickListener {
                    bus.click(Clicks.SupportActionClicked(item.action))
                }
            } else {
                root.setOnClickListener(null)
            }
        }
    }
}