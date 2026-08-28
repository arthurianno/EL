package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R

data class GlucoseTrend(
    val direction: GlucoseTrendDirection,
    val valueText: String
)

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
    designScale: Float = 1f,
    verticalScale: Float = 1f,
    onSyncClick: () -> Unit = {}
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

    val gaugeBoxHeight = (280.dp * verticalScale) * designScale
    val centerBoxHeight = (195.dp * verticalScale) * designScale
    val ringSize = (185.dp * verticalScale) * designScale
    val discWidth = (152.dp * verticalScale) * designScale
    val discHeight = (150.dp * verticalScale) * designScale

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp * designScale, vertical = 0.dp),
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
                    val centerY = (centerBoxHeight / 2f + (6.dp * verticalScale * designScale)).toPx()
                    val strokeWidth = (11.dp * designScale * verticalScale).toPx()
                    val ringRadius = (ringSize / 2f).toPx() - strokeWidth / 2f

                    // Point directly on the outer circle ring contour line (Figma Vector 60 touching Ellipse 17)
                    val lineStartX = centerX - ringRadius * 0.578f
                    val lineStartY = centerY + ringRadius * 0.812f
                    // Point to the center top of TIR block
                    val lineEndX = (51.5.dp * designScale).toPx()
                    val lineEndY = gaugeBoxHeight.toPx() - (57.dp * verticalScale * designScale).toPx()

                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(lineStartX, lineStartY),
                        end = Offset(lineEndX, lineEndY),
                        strokeWidth = (1.5.dp * designScale).toPx()
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
                    val stateBadgeColor = when (state) {
                        GlucoseState.NORMAL -> GlucoseDashboardTheme.NormalChartColor
                        GlucoseState.HIGH -> Color(0xFFE47F1F)
                        GlucoseState.LOW -> GlucoseDashboardTheme.MinBadgeColor
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 0.dp, top = 2.dp * designScale)
                            .size(width = 85.dp * designScale, height = 29.dp * designScale)
                            .clip(RoundedCornerShape(35.5.dp * designScale))
                            .background(stateBadgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stateText,
                            fontSize = (18f * verticalScale).coerceAtLeast(14f).sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Outer Canvas for Arc Gauge Ring
                    Canvas(
                        modifier = Modifier
                            .size(ringSize)
                            .offset(y = (6.dp * verticalScale) * designScale)
                    ) {
                        val strokeWidth = (11.dp * designScale * verticalScale).toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radius = diameter / 2f

                        val tirValue = tirPercentage.replace("%", "").trim().toFloatOrNull() ?: 0f
                        val maxSweep = 260f
                        val progressSweep = (maxSweep * (tirValue / 100f)).coerceIn(0f, maxSweep)

                        // Thin outer background ring contour around the central disc
                        drawCircle(
                            color = Color.White.copy(alpha = 0.35f),
                            radius = radius,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = (1.dp * designScale).toPx())
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
                            .size(width = discWidth, height = discHeight)
                            .offset(y = (6.dp * verticalScale) * designScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_ring_gauge),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize()
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = glucoseValue,
                                fontSize = (64f * verticalScale).coerceAtLeast(44f).sp,
                                fontWeight = FontWeight.Bold,
                                color = mainColor,
                                lineHeight = (46f * verticalScale).coerceAtLeast(34f).sp
                            )
                            Text(
                                text = glucoseUnit,
                                fontSize = (12f * verticalScale).coerceAtLeast(10f).sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height((2.dp * verticalScale) * designScale))
                            GlucoseTrendValue(
                                trend = glucoseTrend,
                                fallbackText = deltaText,
                                designScale = designScale * verticalScale
                            )
                        }
                    }

                    // Sync Refresh Button Widget (Right Side matching reference)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 4.dp * designScale, bottom = 4.dp * designScale)
                            .offset(x = (-8).dp * designScale, y = (28.dp * verticalScale) * designScale)
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
                                    .size((48.dp * verticalScale).coerceAtLeast(36.dp) * designScale)
                                    .rotate(syncIconRotation)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp * designScale))
                        Text(
                            text = if (isSyncing) "Синхр..." else syncTimeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                }

                // Indicator Pills Row: "TIR" (80dp), "Хлебных ед." (109dp) & "Инсулина" (109dp)
                // Figma exact columns: 28dp left, 80dp TIR, 23dp gap, 109dp Bread, 10dp gap, 109dp Insulin, 16dp right
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height((57.dp * verticalScale) * designScale),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(12.dp * designScale))
                    TirIndicator(
                        tirPercentage = tirPercentage,
                        modifier = Modifier
                            .width(80.dp * designScale)
                            .align(Alignment.Top),
                        designScale = designScale * verticalScale
                    )
                    Spacer(modifier = Modifier.width(23.dp * designScale))
                    IndicatorPill(
                        title = "Хлебных ед.",
                        value = breadUnitsText,
                        modifier = Modifier
                            .width(109.dp * designScale)
                            .align(Alignment.Bottom),
                        designScale = designScale * verticalScale
                    )
                    Spacer(modifier = Modifier.width(10.dp * designScale))
                    IndicatorPill(
                        title = "Инсулина",
                        value = insulinText,
                        modifier = Modifier
                            .width(109.dp * designScale)
                            .align(Alignment.Bottom),
                        designScale = designScale * verticalScale
                    )
                }
            }

            // Dark Status Banner Pill (Only visible when isStatusVisible == true)
            if (isStatusVisible && statusText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                SyncStatusPillBanner(
                    statusText = statusText,
                    onClick = onSyncClick
                )
            }
        }
    }
}

@Composable
fun SyncStatusPillBanner(
    statusText: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF353B4B))
            .clickable { onClick() }
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
    designScale: Float
) {
    val trendColor = Color(0xFFBBBFCA)

    if (trend == null) {
        Text(
            text = fallbackText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = trendColor
        )
        return
    }

    Row(
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
    Box(
        modifier = modifier.height(57.dp * designScale),
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

        Text(
            text = tirPercentage,
            fontSize = if (isDash) 30.sp else 38.sp,
            lineHeight = if (isDash) 34.sp else 44.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = if (isDash) 8.dp * designScale else 10.dp * designScale)
                .graphicsLayer(scaleX = if (isDash) 1.0f else 1.18f)
        )
    }
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
            .height(45.dp * designScale)
            .clip(RoundedCornerShape(40.dp * designScale))
            .background(Color.White.copy(alpha = 0.13f)),
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
