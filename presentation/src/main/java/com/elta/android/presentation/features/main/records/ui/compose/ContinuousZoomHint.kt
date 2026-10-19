package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R

@Composable
internal fun ContinuousZoomHint(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_chart_zoom),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.glucose_chart_zoom_hint),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_statistics_settings_close),
                contentDescription = stringResource(R.string.glucose_chart_zoom_hint_dismiss),
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
