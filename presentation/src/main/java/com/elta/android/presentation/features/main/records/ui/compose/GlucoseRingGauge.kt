package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R
import kotlin.math.min

data class GlucoseTrend(
    val direction: GlucoseTrendDirection,
    val valueText: String
)

private val LowerIndicatorsBottomInset = 10.dp
private val IndicatorPillHeight = 45.dp
// The percentage has a 23dp top offset and a 44dp line box at the base size.
private val TirVisualHeight = 67.dp

enum class GlucoseTrendDirection {
    UP,
    DOWN,
    STABLE
}

@Composable
fun GlucoseRingGauge(
    glucoseValue: String,
    glucoseUnit: String = "ммоль/л",
    deltaText: String = "—",
    glucoseTrend: GlucoseTrend? = null,
    tirPercentage: String = "49%",
    syncTimeText: String = "Нет измерений",
    breadUnitsText: String = "0,9 Ед.",
    insulinText: String = "0,1 ХЕ",
    state: GlucoseState = GlucoseState.NORMAL,
    isSyncing: Boolean = false,
    statusText: String = "",
    isStatusVisible: Boolean = false,
    ringSize: Dp,
    ringTopOffset: Dp,
    lowerControlsExtraOffset: Dp = 0.dp,
    onSyncClick: () -> Unit = {},
    onStatePillLongClick: (() -> Unit)? = null
) {
    val mainColor = GlucoseDashboardTheme.getMainTextColor(state)
    val syncIconTransition = rememberInfiniteTransition(label = "syncIconRotation")
    val syncIconRotation = syncIconTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing)
        ),
        label = "syncIconRotation"
    ).value.takeIf { isSyncing } ?: 0f

    // The dashboard calculates this from both the available width and the Figma height
    // breakpoints. Every visual part of the gauge uses this one scale.
    val scaleFactor = ringSize.value / 199f
    val centerBoxHeight = ringSize + ringTopOffset
    // On tall screens the unused part of the 60%-high header is added here, so the lower
    // controls retain their Figma bottom inset rather than floating above the gradient edge.
    val gaugeBoxHeight = glucoseGaugeBaseHeight(ringSize, ringTopOffset) + lowerControlsExtraOffset
    val discTopInset = glucoseGaugeDiscTopInset(ringSize)
    val discWidth = ringSize * (152f / 185f)
    val discHeight = ringSize * (150f / 185f)
    // Both lower pills and the visible TIR content end 15dp before the white section.
    // Their tops differ because the TIR label/value stack is taller than a pill.
    val pillTop = gaugeBoxHeight - LowerIndicatorsBottomInset - IndicatorPillHeight
    val tirTop = gaugeBoxHeight - LowerIndicatorsBottomInset - TirVisualHeight
    // The callout and the TIR component share this anchor. Keeping it in the same
    // coordinate space prevents the line ending beside the label on other screen sizes.
    val tirAnchorX = 52.dp * scaleFactor
    val tirAnchorY = tirTop + 10.dp
    val glucoseValueFontSize = fittedGlucoseValueFontSize(
        value = glucoseValue,
        ringSize = ringSize,
        discWidth = discWidth
    )
    val showSyncStatus = isStatusVisible && statusText.isNotBlank()
    val indicatorsAlpha = animateFloatAsState(
        targetValue = if (showSyncStatus) 0f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "indicatorsAlpha"
    ).value

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gauge, callout line, and bottom indicators need one coordinate space.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gaugeBoxHeight)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val centerX = size.width / 2f
                    val centerY = (ringTopOffset + ringSize / 2f).toPx()
                    val strokeWidth = (11.dp * scaleFactor).toPx()
                    val ringRadius = (ringSize / 2f).toPx() - strokeWidth / 2f

                    // Point directly on the outer circle contour and end at the actual TIR anchor.
                    val lineStartX = centerX - ringRadius * 0.578f
                    val lineStartY = centerY + ringRadius * 0.812f

                    drawLine(
                        color = Color.White.copy(alpha = 0.25f * indicatorsAlpha),
                        start = Offset(lineStartX, lineStartY),
                        end = Offset(tirAnchorX.toPx(), tirAnchorY.toPx()),
                        strokeWidth = (1.dp * scaleFactor).toPx()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(centerBoxHeight)
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    // Top-Left State Pill Badge ("Норма", "Высокий", "Низкий")
                    val stateText = when (state) {
                        GlucoseState.NORMAL -> "Норма"
                        GlucoseState.HIGH -> "Высокий"
                        GlucoseState.LOW -> "Низкий"
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            // "Норма" is close to the 85dp reference width, but longer
                            // statuses must retain the same Figma inner insets instead of
                            // being squeezed into a fixed container.
                            .height(29.dp)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(35.5.dp))
                            .background(GlucoseDashboardTheme.IndicatorPillBackground)
                            .then(
                                onStatePillLongClick?.let { onLongClick ->
                                    Modifier.pointerInput(onLongClick) {
                                        detectTapGestures(onLongPress = { onLongClick() })
                                    }
                                } ?: Modifier
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stateText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Outer Canvas for Arc Gauge Ring
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(ringSize)
                            .offset(y = ringTopOffset)
                    ) {
                        val strokeWidth = (11.dp * scaleFactor).toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radius = diameter / 2f

                        val tirValue = tirPercentage.replace("%", "").trim().toFloatOrNull() ?: 0f
                        // In the Figma reference 73% occupies ~263° (73% of a full circle),
                        // so the visual progress is calculated from 360°, not from the 260°
                        // gap used by the former hard-coded implementation.
                        val progressSweep = (360f * (tirValue / 100f)).coerceIn(0f, 360f)

                        // Thin outer background ring contour around the central disc
                        drawCircle(
                            color = Color.White.copy(alpha = 0.30f),
                            radius = radius,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = (0.75.dp * scaleFactor).toPx())
                        )

                        // Thick highlighted progress arc for TIR (starts at top 12 o'clock, goes clockwise)
                        if (progressSweep > 0f) {
                            drawArc(
                                color = Color.White,
                                startAngle = -90f,
                                sweepAngle = progressSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // The center panel comes from Figma.
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(width = discWidth, height = discHeight)
                            .offset(y = ringTopOffset + discTopInset),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_ring_gauge),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize()
                        )
                        // Keep each text baseline at a proportion of the inner disc. A centered
                        // Column makes the large value pull the unit and trend upward on compact
                        // devices, which no longer matches the intended gauge composition.
                        Box(modifier = Modifier.size(discWidth, discHeight)) {
                            Text(
                                text = glucoseValue,
                                fontSize = glucoseValueFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                color = mainColor,
                                letterSpacing = (-0.067f).em,
                                lineHeight = 48.sp,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = discHeight * 0.18f)
                            )
                            Text(
                                text = glucoseUnit,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = discHeight * 0.70f)
                            )
                            GlucoseTrendValue(
                                trend = glucoseTrend,
                                fallbackText = deltaText,
                                designScale = scaleFactor,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = discHeight * 0.82f)
                            )
                        }
                    }

                    // The icon is deliberately a little larger than the former 53 x 43dp asset.
                    // They are intentionally not laid out relative to the indicator row.
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 26.dp * scaleFactor, y = 192.dp * scaleFactor)
                            .width(166.dp * scaleFactor)
                            .clickable { onSyncClick() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_refresh_2),
                                contentDescription = "Sync",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(width = 56.dp * scaleFactor, height = 46.dp * scaleFactor)
                                    .rotate(syncIconRotation)
                            )
                        }
                        Text(
                            text = if (isSyncing) "Синхр..." else syncTimeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,

                            modifier = Modifier

                                .padding(top = 10.dp * scaleFactor)
                                .fillMaxWidth()
                        )
                    }
                }

                // Figma's lower group is an overlay with independent coordinates, rather than
                // an evenly distributed Row. This keeps TIR and both cards aligned on every width.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(gaugeBoxHeight)
                        .graphicsLayer(alpha = indicatorsAlpha)
                ) {
                    TirIndicator(
                        tirPercentage = tirPercentage,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 12.dp * scaleFactor, y = tirTop)
                            .width(80.dp * scaleFactor),
                        designScale = 1f
                    )
                    IndicatorPill(
                        title = "Хлебных ед.",
                        value = breadUnitsText,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                            // Figma: x=131 on a 380dp frame. The gauge content starts
                            // at x=16, so the local anchor is 115dp. Keeping this anchor
                            // independent from the right card preserves its designed gap.
                            .offset(x = 115.dp * scaleFactor, y = pillTop)
                            .width(109.dp * scaleFactor),
                        designScale = 1f
                    )
                    IndicatorPill(
                        title = "Инсулина",
                        value = insulinText,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = pillTop)
                            .width(109.dp * scaleFactor),
                        designScale = 1f
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        // The full-width status banner needs clearance below the sync timestamp.
                        .offset(y = gaugeBoxHeight - 57.dp + 10.dp * scaleFactor)
                        .fillMaxWidth()
                        .graphicsLayer(alpha = 1f - indicatorsAlpha)
                ) {
                    SyncStatusPillBanner(
                        statusText = statusText,
                        onClick = onSyncClick,
                        modifier = Modifier.height(57.dp * scaleFactor),
                        isEnabled = showSyncStatus
                    )
                }
            }
        }
    }
}

/**
 * Empty dashboard state shown before the first glucose measurement is received.
 *
 * The layout intentionally derives its circle and vertical gaps from the space remaining
 * in the header. This keeps the CTA above the chart on short devices while preserving the
 * larger composition from the reference on regular and tall phones.
 */
@Composable
fun NoMeasurementsGlucoseGauge(
    ringSize: Dp,
    availableHeight: Dp,
    state: GlucoseState,
    isSyncing: Boolean,
    statusText: String,
    isStatusVisible: Boolean,
    onSyncClick: () -> Unit
) {
    val emptyRingSize = min(ringSize.value, 200f).coerceAtLeast(120f).dp
    val compactScale = (emptyRingSize.value / 185f).coerceIn(0.76f, 1f)
    val discWidth = emptyRingSize * (152f / 185f)
    val discHeight = emptyRingSize * (150f / 185f)
    val mainColor = GlucoseDashboardTheme.getMainTextColor(state)
    val syncIconTransition = rememberInfiniteTransition(label = "emptySyncIconRotation")
    val syncIconRotation = syncIconTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing)
        ),
        label = "emptySyncIconRotation"
    ).value.takeIf { isSyncing } ?: 0f
    val actionText = if (isStatusVisible && statusText.isNotBlank()) {
        statusText
    } else {
        "Синхронизация с устройством"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(availableHeight)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(emptyRingSize),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.30f),
                        radius = size.minDimension / 2f - 0.75.dp.toPx() / 2f,
                        style = Stroke(width = 0.75.dp.toPx())
                    )
                }
                Box(
                    modifier = Modifier.size(width = discWidth, height = discHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_ring_gauge),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.matchParentSize()
                    )
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-8).dp * compactScale)
                            .size(width = 62.dp * compactScale, height = 8.dp * compactScale)
                    ) {
                        val strokeWidth = 8.dp.toPx() * compactScale
                        val dashLength = 24.dp.toPx() * compactScale
                        drawLine(
                            color = mainColor,
                            start = Offset(0f, size.height / 2f),
                            end = Offset(dashLength, size.height / 2f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = mainColor,
                            start = Offset(size.width - dashLength, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                    Text(
                        text = "ммоль/л",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = mainColor,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 23.dp * compactScale)
                    )
                }
            }

            Spacer(modifier = Modifier.height(13.dp * compactScale))
            Text(
                text = "Данных пока нет",
                fontSize = (20f * compactScale).sp,
                lineHeight = (24f * compactScale).sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp * compactScale))
            Text(
                text = "Добавьте показатели вручную через «+»\nили синхронизируйте их с устройством",
                fontSize = (14f * compactScale).sp,
                lineHeight = (16f * compactScale).sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 16.dp * compactScale)
                .height((45f * compactScale).dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color.White, RoundedCornerShape(24.dp))
                .clickable(enabled = !isSyncing, onClick = onSyncClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_refresh_2),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(30.dp * compactScale)
                    .rotate(syncIconRotation)
            )
            Spacer(modifier = Modifier.width(12.dp * compactScale))
            Text(
                text = actionText,
                fontSize = (15f * compactScale).sp,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Figma: the 199dp outer ring starts at y=126.24 and the disc at y=146.11. */
internal fun glucoseGaugeDiscTopInset(ringSize: Dp): Dp =
    (19.87f * (ringSize.value / 199f)).dp

@Composable
fun SyncStatusPillBanner(
    statusText: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF353B4B))
            .clickable(enabled = isEnabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "i",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = statusText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GlucoseTrendValue(
    trend: GlucoseTrend?,
    fallbackText: String,
    designScale: Float,
    modifier: Modifier = Modifier
) {
    val trendColor = Color(0xFFBBBFCA)

    if (trend == null) {
        Text(
            text = fallbackText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = trendColor,
            modifier = modifier
        )
        return
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (trend.direction != GlucoseTrendDirection.STABLE) {
            Canvas(modifier = Modifier.size(width = 14.dp * designScale, height = 8.dp * designScale)) {
                val path = Path().apply {
                    if (trend.direction == GlucoseTrendDirection.DOWN) {
                        moveTo(size.width / 2f, size.height)
                        lineTo(size.width, 0f)
                        lineTo(0f, 0f)
                    } else {
                        moveTo(size.width / 2f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                    }
                    close()
                }
                drawPath(path = path, color = trendColor)
            }
            Spacer(modifier = Modifier.width(4.dp * designScale))
        }
        Text(
            text = trend.valueText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = trendColor
        )
    }
}

@Composable
private fun TirIndicator(
    tirPercentage: String,
    modifier: Modifier = Modifier,
    designScale: Float
) {
    val isDash = tirPercentage == "—" || tirPercentage.isBlank()
    val density = LocalDensity.current
    val percentageFontSize = fittedTirValueFontSize(tirPercentage, designScale)

    CompositionLocalProvider(
        // This metric is part of the data visualisation rather than body text. Keep it
        // geometrically aligned with Figma when a device has a custom font-scale setting.
        LocalDensity provides Density(density.density, fontScale = 1f)
    ) {
    Box(
        modifier = modifier.height(TirVisualHeight * designScale),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "TIR",
            fontSize = 12.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (isDash) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 29.dp * designScale)
                    .size(width = 24.dp * designScale, height = 2.dp * designScale)
            ) {
                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = (1.25.dp * designScale).toPx(),
                    cap = StrokeCap.Round
                )
            }
        } else {
            Text(
                text = tirPercentage,
                fontSize = percentageFontSize.sp,
                lineHeight = (percentageFontSize + 6f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    // The TIR anchor is intentionally 80 dp wide to keep the leader line
                    // at the Figma coordinate. The numeric value itself may be wider (100%),
                    // so it is measured without that width constraint instead of being clipped.
                    .wrapContentWidth(unbounded = true)
                    .offset(y = 23.dp * designScale)
            )
        }
    }
    }
}

/** The 38sp Figma value is retained for short values; four-character values such as 100% fit. */
internal fun fittedTirValueFontSize(value: String, designScale: Float): Float {
    val baseSize = when {
        value.length <= 3 -> 38f
        value.length == 4 -> 34f
        else -> 30f
    }
    return baseSize * designScale
}

@Composable
private fun IndicatorPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    designScale: Float
) {
    Box(
        modifier = modifier
            .height(IndicatorPillHeight * designScale)
            .clip(RoundedCornerShape(40.dp * designScale))
            .background(GlucoseDashboardTheme.IndicatorPillBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp * designScale)
        )
        Text(
            text = value,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp * designScale)
        )
    }
}
