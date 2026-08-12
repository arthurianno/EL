package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R

data class DetailedGlucosePoint(
    val timeLabel: String,
    val value: Float,
    val isMin: Boolean = false,
    val isMax: Boolean = false,
    val trendText: String = "стабилен",
    val trendValue: String = "0,0",
    val foodTimeAgo: String? = null,
    val foodUnits: String? = null,
    val insulinTimeAgo: String? = null,
    val insulinUnits: String? = null,
    val activityTimeAgo: String? = null,
    val activityDuration: String? = null
)

data class DetailedInsulinEntry(
    val timeLabel: String,
    val xIndex: Int,
    val units: String,
    val heightRatio: Float
)

data class DetailedFoodEntry(
    val timeLabel: String,
    val xIndex: Int,
    val breadUnits: String,
    val heightRatio: Float
)

data class DetailedActivityEntry(
    val startTimeLabel: String,
    val endTimeLabel: String,
    val durationMins: Long
)

fun Modifier.rotateLandscape(): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = constraints.minHeight,
            maxWidth = constraints.maxHeight,
            minHeight = constraints.minWidth,
            maxHeight = constraints.maxWidth
        )
    )
    layout(placeable.height, placeable.width) {
        placeable.placeWithLayer(
            x = (placeable.height - placeable.width) / 2,
            y = (placeable.width - placeable.height) / 2,
            layerBlock = {
                rotationZ = 90f
            }
        )
    }
}

fun getTodayFormattedDate(): String {
    val now = org.threeten.bp.LocalDate.now()
    val months = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )
    return "${now.dayOfMonth} ${months[now.monthValue - 1]} ${now.year}"
}

fun getTimeOfDayFraction(timeLabel: String): Float {
    return try {
        val parts = timeLabel.trim().split(":")
        if (parts.size >= 2) {
            val hours = parts[0].toFloatOrNull() ?: 0f
            val minutes = parts[1].toFloatOrNull() ?: 0f
            val totalMins = hours * 60f + minutes
            (totalMins / 1440f).coerceIn(0f, 1f)
        } else {
            0.5f
        }
    } catch (e: Exception) {
        0.5f
    }
}

@Composable
fun DetailedGlucoseChartScreen(
    onBackClick: () -> Unit = {},
    initialDate: String = getTodayFormattedDate(),
    glucosePoints: List<DetailedGlucosePoint> = remember { defaultDetailedGlucosePoints() },
    insulinEntries: List<DetailedInsulinEntry> = remember { defaultDetailedInsulinEntries() },
    foodEntries: List<DetailedFoodEntry> = remember { defaultDetailedFoodEntries() },
    activityEntries: List<DetailedActivityEntry> = emptyList()
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var isDatePickerVisible by remember { mutableStateOf(false) }

    // Layer toggles
    var isInsulinLayerVisible by remember { mutableStateOf(true) }
    var isFoodLayerVisible by remember { mutableStateOf(true) }
    var isActivityLayerVisible by remember { mutableStateOf(true) }

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onBackClick,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window
            window?.let { w ->
                w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                w.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF34B0D5))
                .systemBarsPadding()
                .rotateLandscape()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation & Back Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBackClick() }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Назад",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Main Detailed Graph Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Header Row inside Card (Date, Count, Legend)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date Picker Trigger
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isDatePickerVisible = true }
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedDate,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF17191F)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_left),
                                    contentDescription = "Select Date",
                                    tint = Color(0xFF17191F),
                                    modifier = Modifier
                                        .height(18.dp)
                                        .rotate(90f)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Количество измерений: ${glucosePoints.size}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF878B93)
                                )

                                Spacer(modifier = Modifier.width(20.dp))

                                // Legend Items
                                LegendDotItem(color = Color(0xFFF85F73), label = "Низкий")
                                Spacer(modifier = Modifier.width(12.dp))
                                LegendDotItem(color = Color(0xFF3BB2B8), label = "Норма")
                                Spacer(modifier = Modifier.width(12.dp))
                                LegendDotItem(color = Color(0xFFFFB74D), label = "Высокий")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Chart Canvas Box with Horizontal Scroll
                        val horizontalScrollState = rememberScrollState()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                // Y-Axis Fixed Labels (0, 4, 8, 12, 16)
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(end = 8.dp, bottom = 24.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.End
                                ) {
                                    listOf("16", "12", "8", "4", "0").forEach { yVal ->
                                        Text(
                                            text = yVal,
                                            fontSize = 12.sp,
                                            color = Color(0xFF878B93),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Scrollable Canvas Graph
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .horizontalScroll(horizontalScrollState)
                                ) {
                                    val contentWidthDp = 800.dp

                                    Box(
                                        modifier = Modifier
                                            .width(contentWidthDp)
                                            .fillMaxHeight()
                                    ) {
                                        Canvas(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .pointerInput(Unit) {
                                                    detectTapGestures { offset ->
                                                        if (glucosePoints.isNotEmpty()) {
                                                            val closestIdx = glucosePoints.indices.minByOrNull { idx ->
                                                                val ptX = getTimeOfDayFraction(glucosePoints[idx].timeLabel) * size.width
                                                                Math.abs(ptX - offset.x)
                                                            } ?: 0
                                                            selectedPointIndex = closestIdx
                                                        }
                                                    }
                                                }
                                        ) {
                                            val width = size.width
                                            val height = size.height
                                            val paddingBottom = 24.dp.toPx()
                                            val chartHeight = height - paddingBottom
                                            val maxVal = 16f

                                            // Draw Horizontal Grid Lines
                                            val yLevels = listOf(16f, 12f, 8f, 4f, 0f)
                                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                            yLevels.forEachIndexed { index, _ ->
                                                val y = (index.toFloat() / (yLevels.size - 1)) * chartHeight
                                                drawLine(
                                                    color = Color(0xFFE5E7EB),
                                                    start = Offset(0f, y),
                                                    end = Offset(width, y),
                                                    pathEffect = dashEffect,
                                                    strokeWidth = 1.dp.toPx()
                                                )
                                            }

                                            // Calculate Point Coordinates based on Time of Day (00:00 - 23:59)
                                            val linePoints = glucosePoints.map { pt ->
                                                val fraction = getTimeOfDayFraction(pt.timeLabel)
                                                val x = fraction * width
                                                val y = chartHeight - (pt.value / maxVal).coerceIn(0f, 1f) * chartHeight
                                                Offset(x, y)
                                            }

                                            // 1. Render Food Bars Overlay (if enabled)
                                            if (isFoodLayerVisible) {
                                                foodEntries.forEach { food ->
                                                    val ptX = getTimeOfDayFraction(food.timeLabel) * width
                                                    val barWidth = 24.dp.toPx()
                                                    val barHeight = chartHeight * food.heightRatio
                                                    val barTop = chartHeight - barHeight

                                                    drawRoundRect(
                                                        color = Color(0xFFFFB74D).copy(alpha = 0.65f),
                                                        topLeft = Offset(ptX - barWidth / 2, barTop),
                                                        size = Size(barWidth, barHeight),
                                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                                    )
                                                }
                                            }

                                            // 2. Render Insulin Bars Overlay (if enabled)
                                            if (isInsulinLayerVisible) {
                                                insulinEntries.forEach { ins ->
                                                    val ptX = getTimeOfDayFraction(ins.timeLabel) * width
                                                    val barWidth = 20.dp.toPx()
                                                    val barHeight = chartHeight * ins.heightRatio
                                                    val barTop = chartHeight - barHeight

                                                    drawRoundRect(
                                                        color = Color(0xFF29B6F6).copy(alpha = 0.75f),
                                                        topLeft = Offset(ptX - barWidth / 2, barTop),
                                                        size = Size(barWidth, barHeight),
                                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                                    )
                                                }
                                            }

                                            // 3. Render Main Glucose Curve Line & Measurement Dots
                                            if (linePoints.isNotEmpty()) {
                                                if (linePoints.size > 1) {
                                                    for (i in 0 until linePoints.size - 1) {
                                                        val p1 = linePoints[i]
                                                        val p2 = linePoints[i + 1]
                                                        val val1 = glucosePoints[i].value

                                                        val segmentColor = when {
                                                            val1 >= 10.0f -> Color(0xFFFFB74D)
                                                            val1 <= 3.9f -> Color(0xFFF85F73)
                                                            else -> Color(0xFF3BB2B8)
                                                        }

                                                        val segmentPath = Path().apply {
                                                            moveTo(p1.x, p1.y)
                                                            val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2, p1.y)
                                                            val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2, p2.y)
                                                            cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
                                                        }

                                                        drawPath(
                                                            path = segmentPath,
                                                            color = segmentColor,
                                                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                                        )
                                                    }
                                                }

                                                // Draw visible data dots for ALL points
                                                linePoints.forEachIndexed { idx, point ->
                                                    val value = glucosePoints[idx].value
                                                    val isSelected = selectedPointIndex == idx
                                                    val dotColor = when {
                                                        value >= 10f -> Color(0xFFFFB74D)
                                                        value <= 3.9f -> Color(0xFFF85F73)
                                                        else -> Color(0xFF3BB2B8)
                                                    }

                                                    val outerRadius = if (isSelected) 8.dp.toPx() else 5.dp.toPx()
                                                    val innerRadius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx()

                                                    drawCircle(
                                                        color = Color.White,
                                                        radius = outerRadius,
                                                        center = point
                                                    )
                                                    drawCircle(
                                                        color = dotColor,
                                                        radius = innerRadius,
                                                        center = point
                                                    )
                                                }
                                            }

                                            // 4. Render Hypoglycemia (Low Glucose) Period Lines on X-Axis
                                            val lowRanges = mutableListOf<Pair<String, String>>()
                                            var rangeStart: String? = null
                                            var rangeEnd: String? = null
                                            glucosePoints.forEach { pt ->
                                                if (pt.value <= 3.9f) {
                                                    if (rangeStart == null) rangeStart = pt.timeLabel
                                                    rangeEnd = pt.timeLabel
                                                } else {
                                                    if (rangeStart != null && rangeEnd != null) {
                                                        lowRanges.add(rangeStart!! to rangeEnd!!)
                                                        rangeStart = null
                                                        rangeEnd = null
                                                    }
                                                }
                                            }
                                            if (rangeStart != null && rangeEnd != null) {
                                                lowRanges.add(rangeStart!! to rangeEnd!!)
                                            }

                                            val hypoY = chartHeight + 2.dp.toPx()
                                            lowRanges.forEach { (startTime, endTime) ->
                                                val startX = getTimeOfDayFraction(startTime) * width
                                                val endX = getTimeOfDayFraction(endTime) * width
                                                val actualStartX = if (startX == endX) (startX - 12.dp.toPx()).coerceAtLeast(0f) else startX
                                                val actualEndX = if (startX == endX) (startX + 12.dp.toPx()).coerceAtMost(width) else endX

                                                drawLine(
                                                    color = Color(0xFFF85F73),
                                                    start = Offset(actualStartX, hypoY),
                                                    end = Offset(actualEndX, hypoY),
                                                    strokeWidth = 4.dp.toPx(),
                                                    cap = StrokeCap.Round
                                                )
                                            }

                                            // 5. Render Activity Line on X-Axis (only from real activity entries)
                                            if (isActivityLayerVisible && activityEntries.isNotEmpty()) {
                                                val activityY = chartHeight + 2.dp.toPx()
                                                activityEntries.forEach { act ->
                                                    val startX = getTimeOfDayFraction(act.startTimeLabel) * width
                                                    val endX = getTimeOfDayFraction(act.endTimeLabel) * width
                                                    val actualStartX = if (startX == endX) (startX - 12.dp.toPx()).coerceAtLeast(0f) else startX
                                                    val actualEndX = if (startX == endX) (startX + 12.dp.toPx()).coerceAtMost(width) else endX

                                                    drawLine(
                                                        color = Color(0xFF5C6BC0),
                                                        start = Offset(actualStartX, activityY),
                                                        end = Offset(actualEndX, activityY),
                                                        strokeWidth = 4.dp.toPx(),
                                                        cap = StrokeCap.Round
                                                    )
                                                }
                                            }

                                            // 6. Render Vertical Cursor Line & Selected Point Ring
                                            selectedPointIndex?.let { selIdx ->
                                                if (selIdx in linePoints.indices) {
                                                    val selectedPt = linePoints[selIdx]

                                                    drawLine(
                                                        color = Color(0xFF6B7280),
                                                        start = Offset(selectedPt.x, 0f),
                                                        end = Offset(selectedPt.x, chartHeight),
                                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
                                                        strokeWidth = 1.5.dp.toPx()
                                                    )

                                                    drawCircle(
                                                        color = Color(0xFF374151),
                                                        radius = 9.dp.toPx(),
                                                        center = selectedPt,
                                                        style = Stroke(width = 2.dp.toPx())
                                                    )
                                                    drawCircle(
                                                        color = Color.White,
                                                        radius = 7.dp.toPx(),
                                                        center = selectedPt
                                                    )
                                                    val selVal = glucosePoints[selIdx].value
                                                    val dotColor = when {
                                                        selVal >= 10f -> Color(0xFFFFB74D)
                                                        selVal <= 3.9f -> Color(0xFFF85F73)
                                                        else -> Color(0xFF3BB2B8)
                                                    }
                                                    drawCircle(
                                                        color = dotColor,
                                                        radius = 4.dp.toPx(),
                                                        center = selectedPt
                                                    )
                                                }
                                            }
                                        }

                                        // Dynamic Peak Glucose (Max / Min) Badges
                                        if (glucosePoints.isNotEmpty()) {
                                            val maxPtIdx = glucosePoints.indexOfFirst { it.isMax }
                                            if (maxPtIdx != -1) {
                                                val maxVal = glucosePoints[maxPtIdx].value
                                                val xOffset = (getTimeOfDayFraction(glucosePoints[maxPtIdx].timeLabel) * 800).dp
                                                PeakBadgeOverlay(
                                                    text = "max ${String.format(java.util.Locale.US, "%.1f", maxVal).replace('.', ',')}",
                                                    bgColor = Color(0xFFFF9800),
                                                    modifier = Modifier.padding(start = xOffset, top = 10.dp)
                                                )
                                            }

                                            val minPtIdx = glucosePoints.indexOfFirst { it.isMin }
                                            if (minPtIdx != -1) {
                                                val minVal = glucosePoints[minPtIdx].value
                                                val xOffset = (getTimeOfDayFraction(glucosePoints[minPtIdx].timeLabel) * 800).dp
                                                PeakBadgeOverlay(
                                                    text = "min ${String.format(java.util.Locale.US, "%.1f", minVal).replace('.', ',')}",
                                                    bgColor = Color(0xFFFF5252),
                                                    modifier = Modifier.padding(start = xOffset, top = 135.dp)
                                                )
                                            }
                                        }

                                        // Dynamic Food Badges
                                        if (isFoodLayerVisible && foodEntries.isNotEmpty() && glucosePoints.isNotEmpty()) {
                                            foodEntries.forEach { food ->
                                                val xOffset = (getTimeOfDayFraction(food.timeLabel) * 800).dp
                                                PeakBadgeOverlay(
                                                    text = food.breadUnits,
                                                    bgColor = Color(0xFFFF7043),
                                                    modifier = Modifier.padding(start = xOffset, top = 60.dp)
                                                )
                                            }
                                        }

                                        // Dynamic Insulin Badges
                                        if (isInsulinLayerVisible && insulinEntries.isNotEmpty() && glucosePoints.isNotEmpty()) {
                                            insulinEntries.forEach { ins ->
                                                val xOffset = (getTimeOfDayFraction(ins.timeLabel) * 800).dp
                                                PeakBadgeOverlay(
                                                    text = ins.units,
                                                    bgColor = Color(0xFF29B6F6),
                                                    modifier = Modifier.padding(start = xOffset, top = 35.dp)
                                                )
                                            }
                                        }

                                        // X-Axis 24-Hour Time Scale Row (Fixed Labels: 00:00, 02:00, 04:00... 22:00)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomStart)
                                                .padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            val timelineLabels = listOf(
                                                "00:00", "02:00", "04:00", "06:00", "08:00", "10:00",
                                                "12:00", "14:00", "16:00", "18:00", "20:00", "22:00"
                                            )
                                            timelineLabels.forEach { label ->
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF878B93),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right Side Vertical Toggle Bar Column
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        LayerToggleButton(
                            iconRes = R.drawable.ic_save_edit,
                            isActive = isInsulinLayerVisible,
                            activeBgColor = Color(0xFFE1F5FE),
                            onClick = { isInsulinLayerVisible = !isInsulinLayerVisible }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LayerToggleButton(
                            iconRes = R.drawable.ic_verify_dish,
                            isActive = isFoodLayerVisible,
                            activeBgColor = Color(0xFFFFF3E0),
                            onClick = { isFoodLayerVisible = !isFoodLayerVisible }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LayerToggleButton(
                            iconRes = R.drawable.ic_list,
                            isActive = isActivityLayerVisible,
                            activeBgColor = Color(0xFFEDE7F6),
                            onClick = { isActivityLayerVisible = !isActivityLayerVisible }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Clinical Statistics / Selected Event Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                val selectedPt = selectedPointIndex?.let { glucosePoints.getOrNull(it) }

                if (selectedPt != null) {
                    // --- Selected Event Details View (Picture 1) ---
                    val glucoseColor = when {
                        selectedPt.value >= 10.0f -> Color(0xFFFFB74D)
                        selectedPt.value <= 3.9f -> Color(0xFFF85F73)
                        else -> Color(0xFF3BB2B8)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Время события
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Время события",
                                fontSize = 11.sp,
                                color = Color(0xFF878B93)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedPt.timeLabel,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF17191F)
                            )
                        }

                        // Vertical Divider
                        Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0xFFE5E7EB)))

                        // 2. Уровень глюкозы
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", selectedPt.value).replace('.', ','),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = glucoseColor
                            )
                            Text(
                                text = "ммоль/л",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = glucoseColor
                            )
                        }

                        // Vertical Divider
                        Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0xFFE5E7EB)))

                        // 3. Тренд
                        val trendIcon = when {
                            selectedPt.trendValue.startsWith("+") -> "↗"
                            selectedPt.trendValue.startsWith("-") -> "↘"
                            else -> "→"
                        }
                        val trendColor = when {
                            selectedPt.trendValue.startsWith("+") -> Color(0xFFFFB74D)
                            selectedPt.trendValue.startsWith("-") -> Color(0xFFF85F73)
                            else -> Color(0xFF3BB2B8)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Тренд",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF878B93)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$trendIcon ${selectedPt.trendText}",
                                fontSize = 10.sp,
                                color = Color(0xFF17191F)
                            )
                            Text(
                                text = selectedPt.trendValue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = trendColor
                            )
                        }

                        // Vertical Divider
                        Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0xFFE5E7EB)))

                        // 4. Еда
                        val hasFood = selectedPt.foodUnits != null
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_verify_dish),
                                contentDescription = "Food",
                                tint = if (hasFood) Color(0xFFFF7043) else Color(0xFFB0B3BA),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = selectedPt.foodTimeAgo ?: "нет данных",
                                    fontSize = 10.sp,
                                    color = Color(0xFF878B93)
                                )
                                Text(
                                    text = selectedPt.foodUnits ?: "—",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasFood) Color(0xFF17191F) else Color(0xFF878B93)
                                )
                            }
                        }

                        // Vertical Divider
                        Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0xFFE5E7EB)))

                        // 5. Инсулин
                        val hasInsulin = selectedPt.insulinUnits != null
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_save_edit),
                                contentDescription = "Insulin",
                                tint = if (hasInsulin) Color(0xFF29B6F6) else Color(0xFFB0B3BA),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = selectedPt.insulinTimeAgo ?: "нет данных",
                                    fontSize = 10.sp,
                                    color = Color(0xFF878B93)
                                )
                                Text(
                                    text = selectedPt.insulinUnits ?: "—",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasInsulin) Color(0xFF17191F) else Color(0xFF878B93)
                                )
                            }
                        }

                        // Vertical Divider
                        Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0xFFE5E7EB)))

                        // 6. Активность
                        val hasActivity = selectedPt.activityDuration != null
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_list),
                                contentDescription = "Activity",
                                tint = if (hasActivity) Color(0xFF5C6BC0) else Color(0xFFB0B3BA),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = selectedPt.activityTimeAgo ?: "нет данных",
                                    fontSize = 10.sp,
                                    color = Color(0xFF878B93)
                                )
                                Text(
                                    text = selectedPt.activityDuration ?: "—",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasActivity) Color(0xFF17191F) else Color(0xFF878B93)
                                )
                            }
                        }
                    }
                } else {
                    // --- Daily Summary View (Picture 2) ---
                    val hasData = glucosePoints.isNotEmpty()
                    val totalPoints = glucosePoints.size

                    val avgVal = if (hasData) glucosePoints.map { it.value }.average().toFloat() else 0f
                    val avgValStr = if (hasData) String.format(java.util.Locale.US, "%.1f", avgVal).replace('.', ',') else "—"

                    val normCount = glucosePoints.count { it.value in 3.91f..9.99f }
                    val highCount = glucosePoints.count { it.value >= 10.0f }
                    val lowCount = glucosePoints.count { it.value <= 3.9f }

                    val normPct = if (totalPoints > 0) (normCount * 100) / totalPoints else 0
                    val highPct = if (totalPoints > 0) (highCount * 100) / totalPoints else 0
                    val lowPct = if (totalPoints > 0) (lowCount * 100) / totalPoints else 0

                    fun formatDurationMins(mins: Int): String {
                        val h = mins / 60
                        val m = mins % 60
                        return when {
                            h > 0 && m > 0 -> "${h}ч ${m}м"
                            h > 0 -> "${h}ч"
                            else -> "${m}м"
                        }
                    }

                    val normMins = if (totalPoints > 0) (normPct * 24 * 60) / 100 else 0
                    val highMins = if (totalPoints > 0) (highPct * 24 * 60) / 100 else 0
                    val lowMins = if (totalPoints > 0) (lowPct * 24 * 60) / 100 else 0

                    val normTimeStr = if (hasData) formatDurationMins(normMins) else "—"
                    val highTimeStr = if (hasData) formatDurationMins(highMins) else "—"
                    val lowTimeStr = if (hasData) formatDurationMins(lowMins) else "—"

                    val sdVal = if (hasData && totalPoints > 1) {
                        val variance = glucosePoints.map { (it.value - avgVal).let { d -> d * d } }.average()
                        Math.sqrt(variance).toFloat()
                    } else 0f

                    val cvVal = if (hasData && avgVal > 0f) (sdVal / avgVal) * 100f else 0f
                    val gmiVal = if (hasData) 12.71f + (0.091f * (avgVal * 18.0182f)) else 0f

                    val sdStr = if (hasData && totalPoints > 1) String.format(java.util.Locale.US, "%.1f", sdVal).replace('.', ',') else "—"
                    val cvStr = if (hasData && avgVal > 0f) "${Math.round(cvVal)}%" else "—"
                    val gmiStr = if (hasData) "${String.format(java.util.Locale.US, "%.1f", gmiVal).replace('.', ',')}%" else "—"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Events Date Header
                        Column {
                            Text(
                                text = "События дня",
                                fontSize = 12.sp,
                                color = Color(0xFF878B93)
                            )
                            Text(
                                text = selectedDate,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF17191F)
                            )
                        }

                        // Vertical Divider
                        Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0xFFE5E7EB)))

                        // 2. Average Daily Glucose
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = avgValStr,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                            Text(
                                text = "ммоль/л",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                            Text(
                                text = "средний за день",
                                fontSize = 10.sp,
                                color = Color(0xFF878B93)
                            )
                        }

                        // Vertical Divider
                        Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0xFFE5E7EB)))

                        // 3. Time in Range (TIR) Breakdown
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TirStatItem(color = Color(0xFF3BB2B8), percent = if (hasData) "$normPct%" else "—", label = normTimeStr)
                            Spacer(modifier = Modifier.width(16.dp))
                            TirStatItem(color = Color(0xFFFFB74D), percent = if (hasData) "$highPct%" else "—", label = highTimeStr)
                            Spacer(modifier = Modifier.width(16.dp))
                            TirStatItem(color = Color(0xFFF85F73), percent = if (hasData) "$lowPct%" else "—", label = lowTimeStr)
                        }

                        // Vertical Divider
                        Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0xFFE5E7EB)))

                        // 4. Clinical Indicators (CV, SD, GMI)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IndicatorStatItem(name = "CV", value = cvStr)
                            Spacer(modifier = Modifier.width(16.dp))
                            IndicatorStatItem(name = "SD", value = sdStr)
                            Spacer(modifier = Modifier.width(16.dp))
                            IndicatorStatItem(name = "GMI", value = gmiStr)
                        }
                    }
                }
            }
        }
    }
}

    // Custom Date Selection Calendar Dialog
    if (isDatePickerVisible) {
        GlucoseDatePickerDialog(
            initialDate = selectedDate,
            onDismissRequest = { isDatePickerVisible = false },
            onDateSelected = { dateStr ->
                selectedDate = dateStr
            }
        )
    }
}

@Composable
private fun LegendDotItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF878B93)
        )
    }
}

@Composable
private fun PeakBadgeOverlay(
    text: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun LayerToggleButton(
    iconRes: Int,
    isActive: Boolean,
    activeBgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (isActive) activeBgColor else Color(0xFFF3F4F6))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "Toggle Layer",
            tint = if (isActive) Color(0xFF17191F) else Color(0xFFB0B3BA),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TirStatItem(color: Color, percent: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "TIR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF17191F)
            )
        }
        Text(
            text = percent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF17191F)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF878B93)
        )
    }
}

@Composable
private fun IndicatorStatItem(name: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF878B93)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF17191F)
        )
    }
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

fun defaultDetailedGlucosePoints(): List<DetailedGlucosePoint> = listOf(
    DetailedGlucosePoint("02:00", 3.2f, trendText = "снижается", trendValue = "-0,4", insulinTimeAgo = "3ч назад", insulinUnits = "4,0 Ед."),
    DetailedGlucosePoint("02:30", 1.7f, isMin = true, trendText = "быстро снижается", trendValue = "-1,5", insulinTimeAgo = "3ч 30м назад", insulinUnits = "4,0 Ед."),
    DetailedGlucosePoint("03:00", 4.2f, trendText = "растёт", trendValue = "+2,5", foodTimeAgo = "15м назад", foodUnits = "6,5 ХЕ"),
    DetailedGlucosePoint("03:30", 8.8f, trendText = "быстро растёт", trendValue = "+4,6", foodTimeAgo = "45м назад", foodUnits = "6,5 ХЕ"),
    DetailedGlucosePoint("04:00", 7.2f, trendText = "снижается", trendValue = "-1,6", foodTimeAgo = "1ч 15м назад", foodUnits = "6,5 ХЕ"),
    DetailedGlucosePoint("04:30", 15.8f, isMax = true, trendText = "быстро растёт", trendValue = "+8,6", foodTimeAgo = "1ч 45м назад", foodUnits = "3 ХЕ", insulinTimeAgo = "10м назад", insulinUnits = "15,5 Ед."),
    DetailedGlucosePoint("05:00", 7.9f, trendText = "быстро снижается", trendValue = "-7,9", foodTimeAgo = "2ч 15м назад", foodUnits = "3 ХЕ", insulinTimeAgo = "40м назад", insulinUnits = "15,5 Ед."),
    DetailedGlucosePoint("05:30", 3.8f, trendText = "снижается", trendValue = "-4,1"),
    DetailedGlucosePoint("06:00", 5.2f, trendText = "растёт", trendValue = "+1,4", insulinTimeAgo = "5м назад", insulinUnits = "4,1 Ед."),
    DetailedGlucosePoint("06:30", 3.0f, trendText = "снижается", trendValue = "-2,2"),
    DetailedGlucosePoint("07:00", 2.1f, trendText = "снижается", trendValue = "-0,9"),
    DetailedGlucosePoint("07:30", 6.9f, trendText = "быстро растёт", trendValue = "+4,8", foodTimeAgo = "10м назад", foodUnits = "14 ХЕ"),
    DetailedGlucosePoint("08:00", 3.6f, trendText = "снижается", trendValue = "-3,3", foodTimeAgo = "40м назад", foodUnits = "14 ХЕ"),
    DetailedGlucosePoint("08:05", 4.1f, trendText = "снижается", trendValue = "-0,8", foodTimeAgo = "45м назад", foodUnits = "3 ХЕ", insulinTimeAgo = "1ч 15м назад", insulinUnits = "15,5 Ед.", activityTimeAgo = "7ч 24м назад", activityDuration = "20 мин."),
    DetailedGlucosePoint("08:30", 7.5f, trendText = "растёт", trendValue = "+3,9"),
    DetailedGlucosePoint("09:00", 14.8f, trendText = "быстро растёт", trendValue = "+7,3", foodTimeAgo = "1ч 40м назад", foodUnits = "14 ХЕ", insulinTimeAgo = "5м назад", insulinUnits = "7,3 Ед."),
    DetailedGlucosePoint("09:30", 2.5f, trendText = "быстро снижается", trendValue = "-12,3"),
    DetailedGlucosePoint("10:00", 3.0f, trendText = "растёт", trendValue = "+0,5"),
    DetailedGlucosePoint("10:30", 8.1f, trendText = "быстро растёт", trendValue = "+5,1"),
    DetailedGlucosePoint("11:00", 4.3f, trendText = "снижается", trendValue = "-3,8", activityTimeAgo = "10м назад", activityDuration = "45 мин."),
    DetailedGlucosePoint("11:30", 1.8f, trendText = "быстро снижается", trendValue = "-2,5", activityTimeAgo = "40м назад", activityDuration = "45 мин."),
    DetailedGlucosePoint("12:00", 4.0f, trendText = "растёт", trendValue = "+2,2", foodTimeAgo = "10м назад", foodUnits = "2 ХЕ", activityTimeAgo = "1ч 10м назад", activityDuration = "45 мин."),
    DetailedGlucosePoint("12:30", 5.8f, trendText = "растёт", trendValue = "+1,8", activityTimeAgo = "1ч 40м назад", activityDuration = "45 мин."),
    DetailedGlucosePoint("13:00", 3.1f, trendText = "снижается", trendValue = "-2,7"),
    DetailedGlucosePoint("13:30", 7.2f, trendText = "быстро растёт", trendValue = "+4,1"),
    DetailedGlucosePoint("14:00", 13.5f, trendText = "быстро растёт", trendValue = "+6,3"),
    DetailedGlucosePoint("14:30", 2.0f, trendText = "быстро снижается", trendValue = "-11,5")
)

fun defaultDetailedInsulinEntries(): List<DetailedInsulinEntry> = listOf(
    DetailedInsulinEntry("04:30", 5, "15,5 Ед.", 0.85f),
    DetailedInsulinEntry("06:00", 8, "4,1 Ед.", 0.30f),
    DetailedInsulinEntry("09:00", 14, "7,3 Ед.", 0.50f)
)

fun defaultDetailedFoodEntries(): List<DetailedFoodEntry> = listOf(
    DetailedFoodEntry("03:00", 2, "6,5 ХЕ", 0.55f),
    DetailedFoodEntry("04:30", 5, "3 ХЕ", 0.25f),
    DetailedFoodEntry("07:30", 11, "14 ХЕ", 0.78f)
)
