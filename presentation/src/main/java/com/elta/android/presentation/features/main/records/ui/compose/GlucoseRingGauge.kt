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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R

@Composable
fun GlucoseRingGauge(
    glucoseValue: String,
    glucoseUnit: String = "ммоль/л",
    deltaText: String = "▼2,4",
    tirPercentage: String = "49%",
    syncTimeText: String = "5 часов назад",
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
            // Upper Gauge & Indicators Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Top-Left State Pill Badge ("Норма", "Высокая", "Низкая")
                val stateText = when (state) {
                    GlucoseState.NORMAL -> "Норма"
                    GlucoseState.HIGH -> "Высокая"
                    GlucoseState.LOW -> "Низкая"
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 4.dp, top = 4.dp)
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

                // Outer Canvas for Arc Gauge Ring & Callout Line
                Canvas(
                    modifier = Modifier.size(180.dp)
                ) {
                    val strokeWidth = 8.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val radius = diameter / 2f

                    val tirValue = tirPercentage.replace("%", "").trim().toFloatOrNull() ?: 49f
                    val maxSweep = 260f
                    val progressSweep = (maxSweep * (tirValue / 100f)).coerceIn(4f, maxSweep)

                    // Thin outer background ring contour around the central disc
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f),
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Thick highlighted progress arc for TIR (starts at top 12 o'clock, goes clockwise)
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = progressSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Diagonal Callout Line starting from bottom-left of thin ring (135°) pointing to TIR text
                    val calloutAngleRad = Math.toRadians(135.0)
                    val lineStartX = centerX + radius * Math.cos(calloutAngleRad).toFloat()
                    val lineStartY = centerY + radius * Math.sin(calloutAngleRad).toFloat()
                    val lineEnd = Offset(size.width * 0.04f, size.height * 0.87f)

                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(lineStartX, lineStartY),
                        end = lineEnd,
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                // Central Disc
                Box(
                    modifier = Modifier
                        .size(130.dp)
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
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = mainColor,
                            lineHeight = 42.sp
                        )
                        Text(
                            text = glucoseUnit,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = deltaText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                }

                // TIR Widget (Bottom Left)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 4.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "TIR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = tirPercentage,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Sync Refresh Button Widget (Right Side matching reference)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                        .clickable { onSyncClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sync_refresh),
                                contentDescription = "Sync",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        // Info indicator dot on top right of sync circle
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "i",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isSyncing) "Синхр..." else syncTimeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Indicator Pills Row: "Хлебных ед." & "Инсулина"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
