package com.elta.android.presentation.core.compose.widgets.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun RefreshButton(
    @StringRes textId: Int = R.string.refresh_button,
    modifier: Modifier = Modifier,
    retryCallback: () -> Unit,
) {
    GetLocalProperties { dimens, _, colors, shapes, types ->
        Box(
            modifier = Modifier
                .clip(shapes.textField)
                .background(colors.paleGray)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .then(modifier)
                .padding(vertical = dimens.smallDim, horizontal = dimens.bigDim)
                .clickableWithNoRipple { retryCallback() }

        ) {
            Text(
                text = stringResource(textId),
                style = types.title3,
                color = colors.blackBlue,
            )
        }
    }
}

@Preview
@Composable
fun PreviewRefreshButton() {
    RefreshButton {}
}
