package com.elta.android.presentation.features.sync.connect.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
internal fun ColumnScope.MainImage(
    @DrawableRes imageId: Int
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(dimens.bigDim),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageId),
                contentDescription = null
            )
        }
    }
}
