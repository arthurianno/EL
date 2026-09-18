package com.elta.android.presentation.features.auth.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

/** Общая компоновка авторизации; системные отступы учитываются здесь. */
@Composable
internal fun AuthScreenLayout(
    title: String,
    subtitle: String,
    @DrawableRes illustrationRes: Int,
    compactIllustrationSize: DpSize,
    modifier: Modifier = Modifier,
    illustrationUrl: String? = null,
    topBar: @Composable RowScope.() -> Unit,
    form: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val scrollState = rememberScrollState()
    val targetSize = if (isKeyboardVisible) compactIllustrationSize else DpSize(375.dp, 360.dp)
    val illustrationWidth by animateDpAsState(
        targetValue = targetSize.width,
        animationSpec = tween(200),
        label = "authIllustrationWidth"
    )
    val illustrationHeight by animateDpAsState(
        targetValue = targetSize.height,
        animationSpec = tween(200),
        label = "authIllustrationHeight"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.white)
            .safeDrawingPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            content = topBar
        )

        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val viewportHeight = maxHeight
            val titlePadding = when {
                maxWidth < 340.dp -> 16.dp
                maxWidth < 360.dp -> 20.dp
                else -> 27.dp
            }

            Layout(
                modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                content = {
                    Column(Modifier.fillMaxWidth().padding(horizontal = titlePadding)) {
                        Spacer(Modifier.height(22.dp))
                        Text(title, style = types.h0, color = colors.blackBlue)
                        Spacer(Modifier.height(8.dp))
                        Text(subtitle, style = types.subtitle2, color = colors.shadeBlack0)
                    }
                    AsyncImage(
                        model = illustrationUrl,
                        fallback = painterResource(illustrationRes),
                        error = painterResource(illustrationRes),
                        placeholder = painterResource(illustrationRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
                        content = form
                    )
                }
            ) { measurables, constraints ->
                // Сначала измеряем текст и форму: ошибки и fontScale меняют их высоту.
                val contentConstraints = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
                val heading = measurables[0].measure(contentConstraints)
                val formPlaceable = measurables[2].measure(contentConstraints)
                val metrics = calculateAuthLayoutMetrics(
                    viewportHeight = viewportHeight.roundToPx(),
                    contentWidth = constraints.maxWidth,
                    headingHeight = heading.height,
                    formHeight = formPlaceable.height,
                    preferredIllustrationSize = IntSize(
                        illustrationWidth.roundToPx(), illustrationHeight.roundToPx()
                    ),
                    minimumIllustrationHeight = 72.dp.roundToPx()
                )
                val illustration = measurables[1].measure(
                    Constraints.fixed(metrics.illustrationSize.width, metrics.illustrationSize.height)
                )
                // Ни текст, ни форма не обрезаются: при нехватке места увеличивается scroll-контент.
                layout(constraints.maxWidth, constraints.constrainHeight(metrics.contentHeight)) {
                    heading.placeRelative(0, 0)
                    illustration.placeRelative((constraints.maxWidth - illustration.width) / 2, heading.height)
                    formPlaceable.placeRelative(0, metrics.formY)
                }
            }
        }
    }
}
