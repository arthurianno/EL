package com.elta.android.presentation.features.devices.info.ui.adapter.holder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.elta.android.presentation.core.compose.widgets.HSpacer
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemComposeBinding
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceActionItem
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.GetLocalProperties

class DeviceActionViewHolder(
    private val binding: ItemComposeBinding
) : BaseListItemViewHolder<DeviceActionItem>(binding.root) {
    override fun bind(item: DeviceActionItem) {
        binding.root.setContent {
            EltaTheme {
                ComposeContent(item)
            }
        }
    }

    @Composable
    private fun ComposeContent(item: DeviceActionItem) {
        GetLocalProperties { dimens, _, colors, _, _ ->
            Row(
                modifier = Modifier
                    .clickable(onClick = { item.onClick() })
                    .padding(dimens.searchDeviceItemPadding)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Icon(
                    painter = painterResource(id = item.startIcon),
                    contentDescription = null,
                    tint = colors.shadeBlack1
                )
                HSpacer(width = dimens.halfMediumDim)
                Text(text = stringResource(id = item.title), modifier = Modifier.weight(1f))
                HSpacer(width = dimens.halfMediumDim)
                Icon(
                    painter = painterResource(id = item.actionIcon),
                    contentDescription = null,
                    tint = colors.shadeBlack1
                )
            }
        }
    }
}
