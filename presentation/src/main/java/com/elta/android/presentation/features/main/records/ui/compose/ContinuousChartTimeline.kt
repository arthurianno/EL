package com.elta.android.presentation.features.main.records.ui.compose

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToLong
import kotlinx.coroutines.delay
import org.threeten.bp.LocalDate

@Composable
internal fun ContinuousViewportSlider(
    start: Long,
    duration: Long,
    timelineDuration: Long,
    onViewportChanged: (Long, Long) -> Unit
) {
    val currentStart by rememberUpdatedState(start)
    val currentDuration by rememberUpdatedState(duration)
    val currentUpdate by rememberUpdatedState(onViewportChanged)
    Canvas(
        modifier = Modifier.fillMaxWidth().height(28.dp)
            .pointerInput(timelineDuration) {
                fun seek(x: Float) {
                    val selectedDuration = currentDuration
                    currentUpdate(sliderViewportStart(x, size.width.toFloat(), selectedDuration, timelineDuration, 24.dp.toPx()), selectedDuration)
                }
                detectTapGestures { seek(it.x) }
            }
            .pointerInput(timelineDuration) {
                var dragGrabOffset = 0f
                fun seek(x: Float) {
                    val selectedDuration = currentDuration
                    currentUpdate(sliderViewportStart(x, size.width.toFloat(), selectedDuration, timelineDuration, 24.dp.toPx()), selectedDuration)
                }
                detectDragGestures(onDragStart = { touch ->
                    val selectedDuration = currentDuration
                    val knob = sliderKnobWidth(size.width.toFloat(), selectedDuration, timelineDuration, 24.dp.toPx())
                    val travel = (size.width - knob).coerceAtLeast(0f)
                    val currentLeft = currentStart.toFloat() / (timelineDuration - selectedDuration).coerceAtLeast(1L) * travel
                    dragGrabOffset = if (touch.x in currentLeft..(currentLeft + knob)) {
                        touch.x - currentLeft - knob / 2f
                    } else {
                        seek(touch.x)
                        0f
                    }
                }) { change, _ ->
                    seek(change.position.x - dragGrabOffset)
                    change.consume()
                }
            }
    ) {
        val middle = size.height / 2f
        drawLine(Color(0xFFDCE1E5), Offset(0f, middle), Offset(size.width, middle), strokeWidth = 2.dp.toPx())
        if (timelineDuration > 0) {
            val knob = sliderKnobWidth(size.width, duration, timelineDuration, 24.dp.toPx())
            val travel = (size.width - knob).coerceAtLeast(0f)
            val left = start.toFloat() / (timelineDuration - duration).coerceAtLeast(1L) * travel
            drawRoundRect(
                color = ContinuousPrimary,
                topLeft = Offset(left, middle - 5.dp.toPx()),
                size = Size(knob, 10.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
            )
        }
    }
}

internal fun sliderViewportStart(
    touchX: Float,
    width: Float,
    duration: Long,
    timelineDuration: Long,
    minimumKnobWidth: Float
): Long {
    if (width <= 0f || timelineDuration <= duration) return 0L
    val knob = sliderKnobWidth(width, duration, timelineDuration, minimumKnobWidth)
    val travel = (width - knob).coerceAtLeast(1f)
    val fraction = ((touchX - knob / 2f) / travel).coerceIn(0f, 1f)
    return ((timelineDuration - duration) * fraction).toLong()
}

private fun sliderKnobWidth(width: Float, duration: Long, timelineDuration: Long, minimum: Float): Float {
    if (width <= 0f || timelineDuration <= 0L) return 0f
    return (width * duration.toFloat() / timelineDuration)
        .coerceIn(minimum.coerceAtMost(width), width)
}

@Composable
internal fun ContinuousTimelineGraph(
    modifier: Modifier,
    origin: LocalDate,
    viewportStart: Long,
    viewportDuration: Long,
    maxViewportDuration: Long,
    timelineDuration: Long,
    pointLevels: ContinuousGlucosePointLevels,
    insulinEntries: List<DetailedInsulinEntry>,
    foodEntries: List<DetailedFoodEntry>,
    activityEntries: List<DetailedActivityEntry>,
    selectedPoint: DetailedGlucosePoint?,
    selectedEvent: ContinuousEvent?,
    trendLineStyle: ContinuousTrendLineStyle,
    onPointSelected: (DetailedGlucosePoint?) -> Unit,
    onEventSelected: (ContinuousEvent?) -> Unit,
    onViewportChanged: (Long, Long) -> Unit
) {
    val density = LocalDensity.current
    // The canvas tracks the fingers immediately. The expensive viewport-dependent
    // model and statistics are committed only after the gesture pauses.
    var visualViewportStart by remember { mutableStateOf(viewportStart) }
    var visualViewportDuration by remember { mutableStateOf(viewportDuration) }
    val currentVisualStart by rememberUpdatedState(visualViewportStart)
    val currentVisualDuration by rememberUpdatedState(visualViewportDuration)
    LaunchedEffect(viewportStart, viewportDuration) {
        if (visualViewportStart != viewportStart || visualViewportDuration != viewportDuration) {
            visualViewportStart = viewportStart
            visualViewportDuration = viewportDuration
        }
    }
    LaunchedEffect(visualViewportStart, visualViewportDuration) {
        delay(120)
        if (visualViewportStart != viewportStart || visualViewportDuration != viewportDuration) {
            onViewportChanged(visualViewportStart, visualViewportDuration)
        }
    }
    val labels = remember(visualViewportStart, visualViewportDuration, origin) {
        continuousTimeLabels(origin, visualViewportStart, visualViewportDuration)
    }
    val visualResolution = remember(visualViewportDuration) {
        continuousGlucoseResolution(visualViewportDuration)
    }
    val visualPoints = remember(pointLevels, visualViewportStart, visualViewportDuration, origin, visualResolution) {
        pointLevels.pointsFor(visualResolution).visibleIn(
            origin,
            visualViewportStart,
            visualViewportStart + visualViewportDuration
        )
    }
    val visualRealPoints = remember(pointLevels, visualViewportStart, visualViewportDuration, origin) {
        pointLevels.raw.visibleIn(origin, visualViewportStart, visualViewportStart + visualViewportDuration)
    }
    val visibleEvents = remember(foodEntries, insulinEntries, activityEntries, origin, visualViewportStart, visualViewportDuration) {
        continuousEvents(foodEntries, insulinEntries, activityEntries, origin, visualViewportStart, visualViewportDuration)
    }
    BoxWithConstraints(modifier) {
        val widthPx = with(density) { maxWidth.toPx() }
        val labelAreaWidth = maxWidth
        val visibleLabels = remember(labels, maxWidth) {
            val minimumGap = 50f / maxWidth.value.coerceAtLeast(1f)
            val lastFraction = labels.lastOrNull()?.fraction ?: 1f
            buildList<ContinuousTimelineLabel> {
                labels.forEachIndexed { index, label ->
                    if (index == 0 || index == labels.lastIndex ||
                        (label.fraction - last().fraction >= minimumGap && lastFraction - label.fraction >= minimumGap)
                    ) add(label)
                }
            }
        }
        val hitRadius = with(density) { 24.dp.toPx() }
        val eventHitRadius = with(density) { 36.dp.toPx() }
        Column(Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .pointerInput(visualRealPoints, visibleEvents, visualViewportStart, visualViewportDuration, selectedPoint, selectedEvent) {
                        detectTapGestures { offset ->
                            val chartHeight = size.height - with(density) { 18.dp.toPx() }
                            val horizontalInset = with(density) { 7.dp.toPx() }.coerceAtMost(widthPx / 2f)
                            fun eventDistanceSquared(event: ContinuousEvent): Float {
                                val x = ((event.centerMinute - visualViewportStart).toFloat() / visualViewportDuration * widthPx +
                                    with(density) { event.horizontalOffsetDp.dp.toPx() }).coerceIn(horizontalInset, widthPx - horizontalInset)
                                val y = if (event.kind == ContinuousEventKind.ACTIVITY) chartHeight + with(density) { 8.dp.toPx() }
                                    else chartHeight - (chartHeight * event.height).coerceAtLeast(with(density) { 7.dp.toPx() }) - with(density) { 10.dp.toPx() }
                                return (x - offset.x) * (x - offset.x) + (y - offset.y) * (y - offset.y)
                            }
                            val nearestEvent = visibleEvents.minByOrNull(::eventDistanceSquared)
                            if (nearestEvent != null && eventDistanceSquared(nearestEvent) <= eventHitRadius * eventHitRadius) {
                                onEventSelected(nearestEvent.takeIf { it != selectedEvent })
                                return@detectTapGestures
                            }
                            if (visualResolution != ContinuousGlucoseResolution.RAW) return@detectTapGestures
                            val nearest = visualRealPoints.minByOrNull { point ->
                                val x = (point.continuousMinute(origin) - visualViewportStart).toFloat() / visualViewportDuration * widthPx
                                val y = chartHeight * (1f - (point.value / MAX_GLUCOSE).coerceIn(0f, 1f))
                                (x - offset.x) * (x - offset.x) + (y - offset.y) * (y - offset.y)
                            }
                            val hit = nearest?.takeIf { point ->
                                val x = (point.continuousMinute(origin) - visualViewportStart).toFloat() / visualViewportDuration * widthPx
                                val y = chartHeight * (1f - (point.value / MAX_GLUCOSE).coerceIn(0f, 1f))
                                (x - offset.x) * (x - offset.x) + (y - offset.y) * (y - offset.y) <= hitRadius * hitRadius
                            }
                            onPointSelected(hit?.takeIf { it != selectedPoint })
                        }
                    }
                    .pointerInput(widthPx) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val nextDuration = (currentVisualDuration / zoom).roundToLong()
                            val focusFraction = (centroid.x / widthPx).coerceIn(0f, 1f)
                            val focusMinute = currentVisualStart + (currentVisualDuration * focusFraction).roundToLong()
                            val startAfterZoom = focusMinute - (nextDuration * focusFraction).roundToLong()
                            val panMinutes = (pan.x / widthPx * nextDuration).roundToLong()
                            val safeDuration = nextDuration.coerceIn(MIN_VIEWPORT_MINUTES, maxViewportDuration)
                            visualViewportDuration = safeDuration
                            visualViewportStart = (startAfterZoom - panMinutes)
                                .coerceIn(0L, (timelineDuration - safeDuration).coerceAtLeast(0L))
                        }
                    }
            ) {
                val activityTrackHeight = 18.dp.toPx()
                val chartHeight = size.height - activityTrackHeight
                val xForMinute: (Long) -> Float = { minute ->
                    ((minute - visualViewportStart).toFloat() / visualViewportDuration * size.width)
                }
                val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
                drawLine(Color(0xFFDCE1E5), Offset(0f, 0f), Offset(0f, chartHeight), strokeWidth = 1.dp.toPx())
                (0..3).forEach { index ->
                    val y = index / 4f * chartHeight
                    drawLine(Color(0xFFDCE1E5), Offset(0f, y), Offset(size.width, y), pathEffect = dash, strokeWidth = 1.dp.toPx())
                }
                drawLine(Color(0xFFDCE1E5), Offset(0f, chartHeight), Offset(size.width, chartHeight), strokeWidth = 1.dp.toPx())
                visibleEvents.filter { it.kind != ContinuousEventKind.ACTIVITY }.forEach { event ->
                    val horizontalInset = minOf(7.dp.toPx(), size.width / 2f)
                    val x = (xForMinute(event.centerMinute) + event.horizontalOffsetDp.dp.toPx())
                        .coerceIn(horizontalInset, size.width - horizontalInset)
                    val barHeight = chartHeight * event.height.coerceIn(0f, 1f)
                    drawRoundRect(
                        color = event.color,
                        topLeft = Offset(x - 7.dp.toPx(), chartHeight - barHeight),
                        size = Size(14.dp.toPx(), barHeight.coerceAtLeast(7.dp.toPx())),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    drawCircle(event.color, 3.dp.toPx(), Offset(x, chartHeight + 8.dp.toPx()))
                    if (visualResolution == ContinuousGlucoseResolution.RAW) {
                        drawContinuousEventLabel(event, x, chartHeight - barHeight)
                    }
                }
                val offsets = visualPoints.map { point -> point to Offset(
                    xForMinute(point.continuousMinute(origin)),
                    (chartHeight - (point.value / MAX_GLUCOSE).coerceIn(0f, 1f) * chartHeight)
                        .coerceIn(5.dp.toPx(), chartHeight - 5.dp.toPx())
                ) }
                offsets.zipWithNext().forEach { (first, second) ->
                    val gap = second.first.continuousMinute(origin) - first.first.continuousMinute(origin)
                    val lineColor = continuousPointColor(first.first.value)
                    val gapPathEffect = if (gap > TREND_LINE_DASH_GAP_MINUTES) {
                        PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()), 0f)
                    } else {
                        null
                    }
                    when (trendLineStyle) {
                        ContinuousTrendLineStyle.SHARP -> drawLine(
                            lineColor,
                            first.second,
                            second.second,
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = gapPathEffect
                        )

                        ContinuousTrendLineStyle.SMOOTH -> {
                            val middleX = (first.second.x + second.second.x) / 2f
                            val segment = Path().apply {
                                moveTo(first.second.x, first.second.y)
                                cubicTo(
                                    middleX,
                                    first.second.y,
                                    middleX,
                                    second.second.y,
                                    second.second.x,
                                    second.second.y
                                )
                            }
                            drawPath(
                                path = segment,
                                color = lineColor,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    pathEffect = gapPathEffect
                                )
                            )
                        }
                    }
                }
                offsets.forEach { (point, offset) ->
                    val isSelected = point == selectedPoint
                    drawCircle(Color.White, if (isSelected) 8.dp.toPx() else 4.2.dp.toPx(), offset)
                    drawCircle(continuousPointColor(point.value), if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(), offset)
                    if (isSelected) {
                        drawLine(ContinuousSecondary.copy(alpha = .7f), Offset(offset.x, 0f), Offset(offset.x, chartHeight),
                            pathEffect = dash, strokeWidth = 1.dp.toPx())
                    }
                }
                visibleEvents.filter { it.kind == ContinuousEventKind.ACTIVITY }.forEach { event ->
                    val left = xForMinute(event.startMinute).coerceIn(0f, size.width)
                    val right = xForMinute(event.endMinute).coerceIn(0f, size.width)
                    drawLine(
                        color = event.color,
                        start = Offset(left, chartHeight + 8.dp.toPx()),
                        end = Offset((right).coerceAtLeast(left + 10.dp.toPx()).coerceAtMost(size.width), chartHeight + 8.dp.toPx()),
                        strokeWidth = 7.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(24.dp)) {
                visibleLabels.forEach { label ->
                    Text(label.text, fontSize = 11.sp, color = ContinuousSecondary, fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 3.dp).width(48.dp)
                            .offset(x = (labelAreaWidth * label.fraction - 18.dp).coerceIn(0.dp, labelAreaWidth - 48.dp)),
                        textAlign = TextAlign.Center)
                }
            }
        }
    }
}

internal enum class ContinuousGlucoseResolution { RAW, INTERMEDIATE, DAILY }

internal data class ContinuousGlucosePointLevels(
    val raw: List<DetailedGlucosePoint>,
    val intermediate: List<DetailedGlucosePoint>,
    val daily: List<DetailedGlucosePoint>
) {
    fun pointsFor(resolution: ContinuousGlucoseResolution): List<DetailedGlucosePoint> = when (resolution) {
        ContinuousGlucoseResolution.RAW -> raw
        ContinuousGlucoseResolution.INTERMEDIATE -> intermediate
        ContinuousGlucoseResolution.DAILY -> daily
    }
}

private fun continuousGlucoseResolution(duration: Long): ContinuousGlucoseResolution = when {
    duration <= RAW_DETAILS_MAX_MINUTES -> ContinuousGlucoseResolution.RAW
    duration <= INTERMEDIATE_DETAILS_MAX_MINUTES -> ContinuousGlucoseResolution.INTERMEDIATE
    else -> ContinuousGlucoseResolution.DAILY
}

internal fun List<DetailedGlucosePoint>.visibleIn(
    origin: LocalDate,
    start: Long,
    end: Long
): List<DetailedGlucosePoint> = filter { it.continuousMinute(origin) in start..end }

internal fun List<DetailedGlucosePoint>.bucketAverages(
    origin: LocalDate,
    bucketMinutes: Long
): List<DetailedGlucosePoint> = asSequence()
    .filter { it.date != null }
    .groupBy { it.continuousMinute(origin) / bucketMinutes }
    .toSortedMap()
    .map { (bucket, points) ->
        val middleMinute = bucket * bucketMinutes + bucketMinutes / 2L
        val date = origin.plusDays(middleMinute / DAY_MINUTES)
        val minuteOfDay = middleMinute % DAY_MINUTES
        DetailedGlucosePoint(
            timeLabel = String.format(Locale.US, "%02d:%02d", minuteOfDay / 60L, minuteOfDay % 60L),
            value = points.map { it.value }.average().toFloat(),
            date = date,
            trendText = "среднее за 3 часа"
        )
    }
    .toList()

private data class ContinuousTimelineLabel(val fraction: Float, val text: String)
internal enum class ContinuousEventKind { FOOD, INSULIN, ACTIVITY }

internal data class ContinuousEvent(
    val kind: ContinuousEventKind,
    val startMinute: Long,
    val endMinute: Long,
    val centerMinute: Long,
    val height: Float,
    val color: Color,
    val label: String,
    val timeLabel: String,
    val horizontalOffsetDp: Float = 0f
)

private fun continuousTimeLabels(origin: LocalDate, start: Long, duration: Long): List<ContinuousTimelineLabel> {
    val step = when {
        duration <= 2 * 60L -> 30L
        duration <= 12 * 60L -> 60L
        duration <= DAY_MINUTES -> 3 * 60L
        duration <= 7 * DAY_MINUTES -> DAY_MINUTES
        duration <= 14 * DAY_MINUTES -> 2 * DAY_MINUTES
        else -> 5 * DAY_MINUTES
    }
    val first = (start / step) * step
    return generateSequence(first) { it + step }.takeWhile { it <= start + duration }.map { minute ->
        val date = origin.plusDays(minute / DAY_MINUTES)
        val time = minute % DAY_MINUTES
        val text = if (duration <= DAY_MINUTES) String.format(Locale.US, "%02d:%02d", time / 60, time % 60)
        else "${String.format(Locale.US, "%02d", date.dayOfMonth)}.${String.format(Locale.US, "%02d", date.monthValue)}"
        ContinuousTimelineLabel(((minute - start).toFloat() / duration).coerceIn(0f, 1f), text)
    }.toList()
}

private fun continuousEvents(
    food: List<DetailedFoodEntry>,
    insulin: List<DetailedInsulinEntry>,
    activity: List<DetailedActivityEntry>,
    origin: LocalDate,
    start: Long,
    duration: Long
): List<ContinuousEvent> = buildList {
    val end = start + duration
    val foodMinutes = food.map { it.continuousMinute(origin) }.toSet()
    val insulinMinutes = insulin.map { it.continuousMinute(origin) }.toSet()
    food.forEach { entry ->
        val minute = entry.continuousMinute(origin)
        if (minute in start..end) add(
            ContinuousEvent(
                kind = ContinuousEventKind.FOOD,
                startMinute = minute,
                endMinute = minute,
                centerMinute = minute,
                height = entry.heightRatio,
                color = ContinuousHigh,
                label = entry.breadUnits,
                timeLabel = entry.timeLabel,
                horizontalOffsetDp = if (minute in insulinMinutes) -32f else 0f
            )
        )
    }
    insulin.forEach { entry ->
        val minute = entry.continuousMinute(origin)
        if (minute in start..end) add(
            ContinuousEvent(
                kind = ContinuousEventKind.INSULIN,
                startMinute = minute,
                endMinute = minute,
                centerMinute = minute,
                height = entry.heightRatio,
                color = Color(0xFF2E7BE6),
                label = entry.units,
                timeLabel = entry.timeLabel,
                horizontalOffsetDp = if (minute in foodMinutes) 32f else 0f
            )
        )
    }
    activity.forEach { entry ->
        val eventStart = entry.continuousStartMinute(origin)
        val eventEnd = entry.continuousEndMinute(origin)
        if (eventEnd >= start && eventStart <= end) add(
            ContinuousEvent(
                kind = ContinuousEventKind.ACTIVITY,
                startMinute = eventStart,
                endMinute = eventEnd,
                centerMinute = (maxOf(eventStart, start) + minOf(eventEnd, end)) / 2,
                height = 0f,
                color = Color(0xFF8B5CF6),
                label = "${entry.durationMins} мин",
                timeLabel = entry.startTimeLabel
            )
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawContinuousEventLabel(
    event: ContinuousEvent,
    centerX: Float,
    top: Float
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 10.sp.toPx()
        textAlign = Paint.Align.CENTER
    }
    val badgeWidth = paint.measureText(event.label) + 12.dp.toPx()
    val left = (centerX - badgeWidth / 2f).coerceIn(0f, (size.width - badgeWidth).coerceAtLeast(0f))
    val labelTop = (top - 22.dp.toPx()).coerceAtLeast(2.dp.toPx())
    drawRoundRect(
        color = event.color,
        topLeft = Offset(left, labelTop),
        size = Size(badgeWidth, 18.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawContext.canvas.nativeCanvas.drawText(event.label, left + badgeWidth / 2f, labelTop + 13.dp.toPx(), paint)
}
