package com.elta.android.presentation.features.profile.settings.global.ui.adapter.holder

import android.widget.RadioButton
import com.elta.android.domain.features.appsettings.model.BackendVariant
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileSettingsRadioButtonBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingRadioButtonItem
import com.nullgr.core.rx.RxBus

class ProfileSettingRadioButtonViewHolder(
    private val binding: ItemProfileSettingsRadioButtonBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<ProfileSettingRadioButtonItem>(binding.root) {
    override fun bind(item: ProfileSettingRadioButtonItem) {
        with(binding) {
            val backendVariantButtons = BackendVariant.values()
                .map { backendVariant ->
                    getRadioButton(
                        name = backendVariant.name,
                        isCheckedButton = item.type == backendVariant
                    )
                }

            backendVariantButtons.forEach { button ->
                radioGroup.addView(button)
            }

            radioGroup.setOnCheckedChangeListener { _, checkedButtonId ->
                val checkedItem =
                    backendVariantButtons.find { it.id == checkedButtonId }
                        ?.text.toString()
                bus.click(Clicks.ChangeBackendVariant(BackendVariant.valueOf(checkedItem)))
            }
        }
    }

    private fun getRadioButton(name: String, isCheckedButton: Boolean): RadioButton =
        RadioButton(binding.root.context).apply {
            text = name
            isChecked = isCheckedButton
        }
}
