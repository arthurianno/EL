package com.elta.android.presentation.core.compose.widgets.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.R
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    @StringRes titleTextId: Int,
    @StringRes buttonTextId: Int = R.string.refresh_button,
    retryCallback: () -> Unit
) {
    GetLocalProperties { dimens, _, colors, _, types ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(dimens.bigDim),
                text = stringResource(id = titleTextId),
                style = types.body1,
                color = colors.shadeBlack1,
                textAlign = TextAlign.Center
            )

            RefreshButton(
                textId = buttonTextId,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                retryCallback()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewErrorScreen() {
    ErrorScreen(titleTextId = R.string.error_server_not_responding_snackbar_message) {}
}
