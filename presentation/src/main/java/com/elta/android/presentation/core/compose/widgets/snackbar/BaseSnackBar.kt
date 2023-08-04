package com.elta.android.presentation.core.compose.widgets.snackbar

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.eltaColors

@Composable
fun BaseSnackBar(
    @StringRes textId: Int,
    background: Color = eltaColors.shadeBlue3
) {
    GetLocalProperties { dimens, _, colors, _, types ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.snackBarHeight)
                .background(color = background)
        ) {
            Text(
                text = stringResource(id = textId),
                color = colors.white,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = types.caption1
            )
        }
    }
}
