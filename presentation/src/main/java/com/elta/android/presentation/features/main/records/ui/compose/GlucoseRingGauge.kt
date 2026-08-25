package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    onSyncClick: () -> Unit = {}
) {
    val mainColor = GlucoseDashboardTheme.getMainTextColor(state)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gauge, callout line, and bottom indicators need one coordinate space.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(272.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val ringCenter = Offset(size.width / 2f, 107.dp.toPx())
                    val ringRadius = 90.dp.toPx()
                    val startAngle = Math.toRadians(135.0)
                    val lineStart = Offset(
                        x = ringCenter.x + ringRadius * Math.cos(startAngle).toFloat(),
                        y = ringCenter.y + ringRadius * Math.sin(startAngle).toFloat()
                    )
                    val lineEnd = Offset(56.dp.toPx(), 220.dp.toPx())

                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = lineStart,
                        end = lineEnd,
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(214.dp)
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    // Top-Left State Pill Badge ("Норма", "Высокая", "Низкая")
                    val stateText = when (state) {
                        GlucoseState.NORMAL -> "Норма"
                        GlucoseState.HIGH -> "Высокий"
                        GlucoseState.LOW -> "Низкий"
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 8.dp, top = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stateText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Outer Canvas for Arc Gauge Ring
                    Canvas(
                        modifier = Modifier.size(190.dp)
                    ) {
                        val strokeWidth = 10.dp.toPx()
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
                            style = Stroke(width = 2.dp.toPx())
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

                    // Central Disc
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .shadow(12.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = glucoseValue,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = mainColor,
                                lineHeight = 50.sp
                            )
                            Text(
                                text = glucoseUnit,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            GlucoseTrendValue(
                                trend = glucoseTrend,
                                fallbackText = deltaText
                            )
                        }
                    }

                    // Sync Refresh Button Widget (Right Side matching reference)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 5.dp, bottom = 8.dp)
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
                                modifier = Modifier.size(54.dp)
                            )
                            // Info indicator dot on top right of sync arrows
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "i",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.95f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSyncing) "Синхр..." else syncTimeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                }

                // Indicator Pills Row: "TIR", "Хлебных ед." & "Инсулина"
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TirIndicator(
                        tirPercentage = tirPercentage,
                        modifier = Modifier.width(72.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IndicatorPill(
                        title = "Хлебных ед.",
                        value = breadUnitsText,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IndicatorPill(
                        title = "Инсулина",
                        value = insulinText,
                        modifier = Modifier.weight(1f)
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
    fallbackText: String
) {
    val trendColor = Color(0xFFBBBFCA)

    if (trend == null) {
        Text(
            text = fallbackText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = trendColor
        )
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (trend.direction != GlucoseTrendDirection.STABLE) {
            Canvas(modifier = Modifier.size(width = 14.dp, height = 8.dp)) {
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
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = trend.valueText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = trendColor
        )
    }
}

@Composable
private fun TirIndicator(
    tirPercentage: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TIR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f)
        )
        Text(
            text = tirPercentage,
            fontSize = 32.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun IndicatorPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
