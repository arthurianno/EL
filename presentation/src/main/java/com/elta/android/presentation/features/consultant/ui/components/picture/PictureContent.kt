package com.elta.android.presentation.features.consultant.ui.components.picture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerLarge
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonCircle
import com.elta.android.presentation.core.compose.widgets.buttons.RoundedButton
import com.elta.android.presentation.features.consultant.model.PreviewState
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun PictureContent(
    state: PreviewState,
    onBackClick: () -> Unit,
    onSendClick: () -> Unit
) {
    GetLocalProperties { _, _, colors, _, _ ->
        Column(
            Modifier
                .fillMaxSize()
                .background(color = colors.black)
                .statusBarsPadding()
        ) {
            PictureTopBar(onBackClick)
            Picture(state)
            if (state.isFromCamera) PictureBottomBar(onSendClick)
            else VSpacerLarge()
        }
    }
}

@Composable
private fun PictureTopBar(onBackClick: () -> Unit) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        TopAppBar(
            title = {},
            backgroundColor = Color.Transparent,
            elevation = dimens.zero,
            navigationIcon = {
                ButtonCircle(
                    icon = R.drawable.ic_back,
                    tint = colors.white,
                    contentDescriptionId = R.string.content_description_back_button,
                    onClick = onBackClick
                )
            }
        )
    }
}

// todo: убрать плейсхолдер после всего, возможно добавить в превью
@Composable
private fun ColumnScope.Picture(state: PreviewState) {
    val data = state.uriPhoto ?: state.urlPhoto
    GetLocalProperties { dimens, _, colors, _, _ ->
        Box(
            modifier = Modifier
                .weight(1f)
                .background(color = colors.black)
                .padding(dimens.photoPreviewContentPadding)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(data)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                // fixme не забыть убрать плейсхолдер
                placeholder = BrushPainter(
                    Brush.linearGradient(
                        listOf(
                            Color(color = 0xFFFFFFFF),
                            Color(color = 0xF432432D),
                        )
                    )
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PictureBottomBar(onSendClick: () -> Unit) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(dimens.photoPreviewBottomBarContentPadding),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.consultant_send_photo),
                color = colors.white
            )
            HSpacerMedium()
            RoundedButton(
                icon = R.drawable.ic_send,
                background = colors.gGreenB,
                border = null,
                size = dimens.previewSendButtonSize,
                onClick = onSendClick
            )
        }
    }
}

@Preview
@Composable
private fun PreviewPictureContent() {
    Box {
        PictureContent(
            state = PreviewState(
                isPhotoPreview = true,
                isFromCamera = true,
                uriPhoto = null,
                urlPhoto = null
            ),
            onBackClick = {}
        ) {}
    }
}
