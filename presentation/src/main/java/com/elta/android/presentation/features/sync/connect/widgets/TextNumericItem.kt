package com.elta.android.presentation.features.sync.connect.widgets

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
internal fun TextNumericItem(
    @StringRes number: Int,
    @StringRes text: Int
) {
    GetLocalProperties { _, _, colors, _, types ->
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = number),
                style = types.body1,
                color = colors.shadeBlack0
            )
            HSpacerVerySmall()
            Text(
                text = stringResource(id = text),
                style = types.body1,
                color = colors.shadeBlack0,
            )
        }
    }
}
