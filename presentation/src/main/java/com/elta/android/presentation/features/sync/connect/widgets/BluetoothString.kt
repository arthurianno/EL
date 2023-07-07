package com.elta.android.presentation.features.sync.connect.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
internal fun BluetoothString() {
    GetLocalProperties { _, _, colors, _, types ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.paleGray, shape = RoundedCornerShape(4.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .padding(start = 8.dp),
                painter = painterResource(id = R.drawable.ic_bluetooth),
                contentDescription = stringResource(id = R.string.content_description_bluetooth_icon)
            )
            Text(
                text = stringResource(id = R.string.sync_how_to_connect_bluetooth_text),
                style = types.body1,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
