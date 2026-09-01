package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
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

data class GlucoseTrend(
    val direction: GlucoseTrendDirection,
    val valueText: String
)

private val IndicatorPillHeight = 45.dp
// The percentage has a 23dp top offset and a 44dp line box at the base size.
private val TirVisualHeight = 67.dp

private val TirTitleOffset = 8.dp
private val TirPercentageOffset = 24.dp
private val SyncRowHeight = 45.dp
private val MetricValueBottomInset = 1.5.dp
private val MetricToSyncSpacing = 8.dp
// This affects only the visual gauge group (arc, disc, and callout), not the
// layout anchors for metrics and sync. Keeping it proportional preserves the
// Figma vertical rhythm on compact and tall devices alike.
private const val RingVisualLiftFraction = 0.05f
private const val MetricsReferenceWidth = 343f
private const val TirStart = 12f / MetricsReferenceWidth
private const val TirWidth = 80f / MetricsReferenceWidth
// The leader finishes in the empty space to the right of the TIR label.
private const val TirCalloutEnd = 88f / MetricsReferenceWidth
private const val BreadUnitsStart = 115f / MetricsReferenceWidth
private const val InsulinStart = 234f / MetricsReferenceWidth
private const val IndicatorWidth = 109f / MetricsReferenceWidth

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

    val visualRingOffset = ringTopOffset - ringSize * RingVisualLiftFraction
    val centerBoxHeight = ringSize + ringTopOffset
    // On tall screens the unused part of the 60%-high header is added here, so the lower
    // controls retain their Figma bottom inset rather than floating above the gradient edge.
    val gaugeBoxHeight = glucoseGaugeBaseHeight(ringSize, ringTopOffset) + lowerControlsExtraOffset
    val discTopInset = glucoseGaugeDiscTopInset(ringSize)
    val discWidth = ringSize * (152f / 185f)
    val discHeight = ringSize * (150f / 185f)
    // The Figma frame has two independent lower rows: metrics and the sync action.
    // Both are anchored from the gradient bottom, so tall devices receive extra space
    // without separating the action from the cards above it.
    val syncRowHeight = SyncRowHeight * scaleFactor
    val syncRowBottomInset = GaugeSyncRowBottomInset * scaleFactor
    val syncRowTop = gaugeBoxHeight - syncRowBottomInset - syncRowHeight
    val metricsBottom = syncRowTop - MetricToSyncSpacing * scaleFactor
    val metricsTop = metricsBottom - TirVisualHeight * scaleFactor
    // In the reference, the callout ends in the clear space beside the TIR heading.
    val tirAnchorY = metricsTop + (10.dp + TirTitleOffset) * scaleFactor
    val glucoseValueFontSize = fittedGlucoseValueFontSize(
        value = glucoseValue,
        ringSize = ringSize,
        discWidth = discWidth
    )
    val showSyncStatus = isStatusVisible && statusText.isNotBlank()
    val syncActionText = if (showSyncStatus) statusText else "Синхронизация с устройством"

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
                    val centerY = (visualRingOffset + ringSize / 2f).toPx()
                    val strokeWidth = (11.dp * scaleFactor).toPx()
                    val ringRadius = (ringSize / 2f).toPx() - strokeWidth / 2f

                    // Point directly on the outer circle contour and end at the actual TIR anchor.
                    val lineStartX = centerX - ringRadius * 0.578f
                    val lineStartY = centerY + ringRadius * 0.812f
                    val tirCalloutEndX = size.width * TirCalloutEnd

                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(lineStartX, lineStartY),
                        end = Offset(tirCalloutEndX, tirAnchorY.toPx()),
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
                            .offset(y = visualRingOffset)
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
                    RingGaugeDisc(
                        width = discWidth,
                        height = discHeight,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = visualRingOffset + discTopInset)
                    ) {
                        // Keep each text baseline at a proportion of the inner disc. A centered
                        // Column makes the large value pull the unit and trend upward on compact
                        // devices, which no longer matches the intended gauge composition.
                        Box(modifier = Modifier.matchParentSize()) {
                            Text(
                                text = glucoseValue,
                                fontSize = glucoseValueFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                color = mainColor,
                                letterSpacing = (-0.067f).em,
                                lineHeight = 48.sp,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    // The main Figma value is visually centred in the disc;
                                    // its glyph box needs a lower anchor than the unit and trend.
                                    .offset(y = discHeight * 0.29f)
                            )
                            Text(
                                text = glucoseUnit,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = mainColor,
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

                }

                // Positions are percentages of the 343dp Figma content width. This keeps
                // the asymmetric visual rhythm from the mockup without fixed screen pixels.
                BoxWithConstraints(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(gaugeBoxHeight)
                ) {
                    TirIndicator(
                        tirPercentage = tirPercentage,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = maxWidth * TirStart, y = metricsTop)
                            .width(maxWidth * TirWidth),
                        designScale = scaleFactor
                    )
                    MetricValue(
                        title = "Хлебных ед.",
                        value = breadUnitsText,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(
                                x = maxWidth * BreadUnitsStart,
                                y = metricsBottom - IndicatorPillHeight * scaleFactor
                            )
                            .width(maxWidth * IndicatorWidth),
                        designScale = scaleFactor
                    )
                    MetricValue(
                        title = "Инсулина",
                        value = insulinText,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(
                                x = maxWidth * InsulinStart,
                                y = metricsBottom - IndicatorPillHeight * scaleFactor
                            )
                            .width(maxWidth * IndicatorWidth),
                        designScale = scaleFactor
                    )
                }

                SyncDeviceButton(
                    actionText = syncActionText,
                    syncTimeText = syncTimeText,
                    isSyncing = isSyncing,
                    iconRotation = syncIconRotation,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = syncRowTop)
                        .fillMaxWidth(),
                    onClick = onSyncClick
                )

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
    val layoutMetrics = emptyGaugeLayoutMetrics(ringSize)
    val emptyRingSize = layoutMetrics.ringSize
    val compactScale = layoutMetrics.scale
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = layoutMetrics.contentBottomInset),
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
                RingGaugeDisc(
                    width = discWidth,
                    height = discHeight
                ) {
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

        SyncDeviceButton(
            actionText = actionText,
            syncTimeText = null,
            isSyncing = isSyncing,
            iconRotation = syncIconRotation,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = layoutMetrics.buttonBottomInset),
            designScale = compactScale,
            outlined = true,
            onClick = onSyncClick
        )
    }
}

/**
 * Common decorative centre disc for both dashboard states.
 *
 * The drawable is deliberately decorative, so it has no accessibility description. Keeping the
 * background here ensures the empty and populated variants retain identical scaling and z-order.
 */
@Composable
private fun RingGaugeDisc(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.size(width = width, height = height),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_ring_gauge),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        content()
    }
}

/** Figma: the 199dp outer ring starts at y=126.24 and the disc at y=146.11. */
internal fun glucoseGaugeDiscTopInset(ringSize: Dp): Dp =
    (19.87f * (ringSize.value / 199f)).dp

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
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = TirTitleOffset * designScale)
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
                    .offset(y = TirPercentageOffset * designScale)
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
private fun MetricValue(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    designScale: Float
) {
    Column(
        modifier = modifier.height(IndicatorPillHeight * designScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = (13f * designScale).sp,
            lineHeight = (16f * designScale).sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            fontSize = (20f * designScale).sp,
            lineHeight = (26f * designScale).sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SyncDeviceButton(
    actionText: String,
    syncTimeText: String?,
    isSyncing: Boolean,
    iconRotation: Float,
    modifier: Modifier = Modifier,
    designScale: Float = 1f,
    outlined: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp * designScale)
    val buttonModifier = modifier
        .height(SyncRowHeight * designScale)
        .clip(shape)
        .background(Color.White.copy(alpha = if (outlined) 0f else 0.14f))
        .then(
            if (outlined) Modifier.border(1.dp, Color.White, shape) else Modifier
        )
        .clickable(enabled = !isSyncing, onClick = onClick)

    Row(
        modifier = buttonModifier.padding(horizontal = 16.dp * designScale),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_refresh_2),
            contentDescription = syncTimeText?.let { "Синхронизация с устройством. $it" },
            tint = Color.White,
            modifier = Modifier
                .size(30.dp * designScale)
                .rotate(iconRotation)
        )
        Spacer(modifier = Modifier.width(12.dp * designScale))
        Text(
            text = actionText,
            modifier = Modifier.weight(1f),
            fontSize = (15f * designScale).sp,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
