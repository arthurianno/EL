package com.elta.android.presentation.core.compose.widgets.paging

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.common.RefreshButton
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun LoadingNextPage() {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Box(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(dimens.contentPadding)
                    .then(Modifier.size(28.dp))
                    .align(Alignment.Center),
                color = colors.shadeBlack1
            )
        }
    }
}

@Composable
fun ErrorNextPage(retryCallback: () -> Unit) {
    GetLocalProperties { dimens, _, colors, shapes, types ->
        Row(
            modifier = Modifier
                .background(colors.paleGrayDark, shapes.textField)
                .padding(
                    start = dimens.halfBigDim,
                    top = dimens.contentPadding,
                    end = dimens.smallDim,
                    bottom = dimens.contentPadding
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stringResource(id = R.string.error_not_connection),
                style = types.body1
            )

            RefreshButton(
                textId = R.string.repeat_button,
                modifier = Modifier
                    .background(colors.white, shapes.textField)
                    .align(Alignment.CenterVertically)
                    .background(colors.white),
            ) {
                retryCallback()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewLoadingNextPage() {
    LoadingNextPage()
}