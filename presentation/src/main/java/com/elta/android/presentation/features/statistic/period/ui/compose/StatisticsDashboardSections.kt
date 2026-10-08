@file:Suppress("LongMethod", "MagicNumber")

package com.elta.android.presentation.features.statistic.period.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.R
import com.elta.android.presentation.features.statistic.period.model.StatisticsBlock
import com.elta.android.presentation.utils.NumberFormatter

@Composable
internal fun MainIndicatorsSection(
    state: StatisticsDashboardUiState,
    isCompact: Boolean,
    onSettingsClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isCompact) 12.dp else 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            androidx.compose.material.Icon(
                painter = painterResource(R.drawable.ic_chart_settings),
                contentDescription = "Настройки статистики",
                tint = TextSecondary,
                modifier = Modifier.size(40.dp).clickable(onClick = onSettingsClick).padding(8.dp)
            )
        }
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().height(if (isCompact) 276.dp else 290.dp)
        ) {
            val gaugeSize = minOf(maxWidth * 0.54f, 184.dp).coerceAtLeast(164.dp)
            val pillWidth = if (isCompact) 84.dp else 102.dp
            val pillHeight = if (isCompact) 72.dp else 80.dp
            Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)) {
                GlucoseGauge(average = state.average, unit = state.unit, size = gaugeSize)
            }
            MinMaxLabel(
                text = "min ${state.minLabel}",
                color = Red,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = pillHeight + 6.dp)
            )
            MinMaxLabel(
                text = "max ${state.maxLabel}",
                color = Orange,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = pillHeight + 6.dp)
            )
            Row(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 8.dp)
            ) {
                TimeInRangePill("TBR", state.lowPercent, state.lowCount, Red, pillWidth, pillHeight)
                TimeInRangePill("TIR", state.inRangePercent, state.inRangeCount, Green, pillWidth, pillHeight)
                TimeInRangePill("TAR", state.highPercent, state.highCount, Orange, pillWidth, pillHeight)
            }
        }
        SectionDivider()
    }
}

@Composable
private fun GlucoseGauge(average: String, unit: String, size: Dp) {
    val stroke = with(LocalDensity.current) { 9.dp.toPx() }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            listOf(Red to 37f, Green to 162f, Orange to 112f).fold(-145f) { start, (color, sweep) ->
                drawArc(color, start, sweep - 4f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                start + sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(average, color = GreenDark, fontWeight = FontWeight.SemiBold, fontSize = (size.value * 0.29f).sp)
            Text(unit, color = GreenDark, fontSize = 12.sp)
            Text("Среднее", color = Divider, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TimeInRangePill(
    label: String,
    percent: Int,
    count: Int,
    color: Color,
    width: Dp,
    height: Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, Color(0xFF626876), RoundedCornerShape(26.dp))
            .background(ScreenBackground)
            .padding(vertical = 7.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("●", color = color, fontSize = 9.sp)
            Spacer(Modifier.width(3.dp))
            Text(label, color = TextPrimary, fontSize = 11.sp)
        }
        Text("$percent%", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("$count изм.", color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun MinMaxLabel(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 11.sp,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

@Composable
internal fun DistributionSection(state: StatisticsDashboardUiState) {
    SectionCard(title = "Основные показатели за период") {
        Text("Количество измерений", color = TextSecondary, fontSize = 10.sp)
        DistributionChart(state.distribution)
        ChartLegend()
    }
}

@Composable
private fun DistributionChart(values: List<Int>) {
    val colors = listOf(Red, Red, Green, Green, Orange, Orange)
    val labels = listOf("0–3", "3–4", "4–7", "7–10", "10–12", ">12")
    val axisMax = niceAxisMaximum(values.maxOrNull() ?: 0)
    Box(modifier = Modifier.fillMaxWidth().height(151.dp).padding(top = 8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(start = 28.dp, bottom = 38.dp)) {
            val gap = size.width / (values.size * 2f)
            val dash = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
            listOf(1f / 3f, 2f / 3f).forEach { ratio ->
                drawLine(Divider.copy(alpha = .65f), Offset(0f, size.height * ratio), Offset(size.width, size.height * ratio), 1.dp.toPx(), pathEffect = dash)
            }
            values.forEachIndexed { index, value ->
                val height = size.height * value / axisMax
                val left = gap * (index * 2 + 0.5f)
                drawRoundRect(colors[index], Offset(left, size.height - height), Size(gap, height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
            }
            drawLine(Divider, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
        }
        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(bottom = 38.dp).height(104.dp).width(26.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(axisMax, axisMax * 2 / 3, axisMax / 3, 0).forEach {
                Text(it.toString(), color = TextSecondary, fontSize = 8.sp)
            }
        }
        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(start = 28.dp, bottom = 18.dp).fillMaxWidth()) {
            labels.forEach { label -> Text(label, modifier = Modifier.weight(1f), color = TextSecondary, fontSize = 8.sp, textAlign = TextAlign.Center) }
        }
        Text(
            text = "ммоль/л",
            color = TextSecondary,
            fontSize = 8.sp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private fun niceAxisMaximum(maxValue: Int): Int {
    if (maxValue <= 3) return 3
    val magnitude = when {
        maxValue <= 10 -> 2
        maxValue <= 50 -> 10
        maxValue <= 100 -> 20
        maxValue <= 500 -> 100
        else -> 200
    }
    return ((maxValue + magnitude - 1) / magnitude) * magnitude
}

@Composable
internal fun DailyVariationSection(state: StatisticsDashboardUiState) {
    var weeksBack by remember(state.hourlyRanges) { mutableStateOf(0) }
    val maxWeeksBack = state.hourlyRanges.lastWeekOffset
    val visibleDays = state.hourlyRanges.weekFromLatest(weeksBack)
    val rangeTitle = if (weeksBack == 0) {
        state.dailyRangeTitle
    } else {
        val first = visibleDays.firstOrNull()?.date
        val last = visibleDays.lastOrNull()?.date
        if (first != null && last != null) {
            "${first.dayOfMonth}.${first.monthValue} – ${last.dayOfMonth}.${last.monthValue}"
        } else ""
    }
    SectionCard(title = "Суточные колебания") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp)
        ) {
            Text(
                text = rangeTitle,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
            if (weeksBack < maxWeeksBack) {
                androidx.compose.material.Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = "Предыдущая неделя",
                    tint = TextPrimary,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(32.dp)
                        .rotate(180f)
                        .clickable { weeksBack++ }
                        .padding(7.dp)
                )
            }
            if (weeksBack > 0) {
                androidx.compose.material.Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = "Следующая неделя",
                    tint = TextPrimary,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(32.dp)
                        .clickable { weeksBack-- }
                        .padding(7.dp)
                )
            }
        }
        if (state.hourlyRanges.isEmpty()) {
            EmptyChart("Недостаточно измерений для построения графика")
        } else {
            Heatmap(visibleDays)
        }
    }
}

@Composable
private fun Heatmap(days: List<HourlyRange>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(205.dp)) {
        val timeAxisWidth = if (maxWidth <= CompactScreenWidth) 44.dp else 54.dp
        // The reference deliberately keeps the heatmap narrow instead of stretching its
        // columns to a tablet width. This also leaves a readable gutter for time labels.
        val gridWidth = minOf(236.dp, (maxWidth - timeAxisWidth).coerceAtLeast(0.dp))
        Row(modifier = Modifier.align(Alignment.TopStart)) {
            Column(modifier = Modifier.width(timeAxisWidth)) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.height(167.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "24:00").forEach {
                        Text(it, color = TextSecondary, fontSize = 8.sp)
                    }
                }
            }
            Row(
                modifier = Modifier.width(gridWidth),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                days.forEach { day ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(day.dayLabel, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Column(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            day.statuses.forEach { status ->
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .background(status.toChartColor(), RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HypoHyperSection(state: StatisticsDashboardUiState) {
    SectionCard(title = "Статистика гипо/гипергликемии") {
        Text("Количество измерений", color = TextSecondary, fontSize = 10.sp)
        val episodes = state.dailyEpisodes.takeLast(14)
        val axisMax = niceAxisMaximum(
            episodes.flatMap { listOf(it.low, it.high) }.maxOrNull() ?: 0
        )
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp)) {
            Canvas(modifier = Modifier.fillMaxSize().padding(start = 28.dp, bottom = 38.dp)) {
                val step = size.width / episodes.size.coerceAtLeast(1)
                val barWidth = minOf(7.dp.toPx(), step * 0.24f)
                episodes.forEachIndexed { index, entry ->
                    val center = step * (index + 0.5f)
                    drawEpisodeBar(
                        value = entry.low,
                        axisMax = axisMax,
                        centerX = center - barWidth * 0.65f,
                        width = barWidth,
                        color = Red
                    )
                    drawEpisodeBar(
                        value = entry.high,
                        axisMax = axisMax,
                        centerX = center + barWidth * 0.65f,
                        width = barWidth,
                        color = Orange
                    )
                }
                listOf(1f / 3f, 2f / 3f, 1f).forEach { ratio ->
                    drawLine(
                        Divider.copy(alpha = .65f),
                        Offset(0f, size.height * (1f - ratio)),
                        Offset(size.width, size.height * (1f - ratio)),
                        1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
                    )
                }
                drawLine(Divider, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            }
            Column(
                modifier = Modifier.align(Alignment.CenterStart).padding(bottom = 38.dp).height(104.dp).width(26.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(axisMax, axisMax * 2 / 3, axisMax / 3, 0).forEach {
                    Text(it.toString(), color = TextSecondary, fontSize = 8.sp)
                }
            }
            Row(modifier = Modifier.align(Alignment.BottomEnd).padding(start = 28.dp, bottom = 18.dp).fillMaxWidth()) {
                episodes.forEach { entry ->
                    Text(
                        entry.date.dayOfMonth.toString(),
                        modifier = Modifier.weight(1f),
                        color = TextSecondary,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Text("Дата", color = TextSecondary, fontSize = 8.sp, modifier = Modifier.align(Alignment.BottomCenter))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Legend("Гипо", Red)
            Legend("Гипер эпизоды", Orange)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEpisodeBar(
    value: Int,
    axisMax: Int,
    centerX: Float,
    width: Float,
    color: Color
) {
    if (value == 0) return
    val height = size.height * value / axisMax
    drawRoundRect(
        color = color,
        topLeft = Offset(centerX - width / 2f, size.height - height),
        size = Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
    )
}

@Composable
internal fun KeyMetricsSection(state: StatisticsDashboardUiState) {
    SectionCard(title = "Ключевые метрики") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Metric("CV", state.coefficientOfVariation)
            Metric("SD", state.standardDeviation)
            Metric("GMI", state.gmi)
            Metric("Гипо", state.lowCount.toString())
            Metric("Гипер", state.highCount.toString())
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp)
                .height(1.dp)
                .background(Divider)
        )
        if (state.nightHypoEpisodes > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WarningIcon()
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Предупреждение", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Частые гипо ночью", color = TextPrimary, fontSize = 11.sp)
                    Text(
                        text = "${state.nightHypoEpisodes} эпизода между 02:00 и 05:00",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            Text(
                text = "Ночных гипо за выбранный период не выявлено",
                color = GreenDark,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
            )
        }
    }
}

@Composable
internal fun UnavailableStatisticsSection(block: StatisticsBlock) {
    SectionCard(title = block.title) {
        Text(
            text = "Нет данных для отображения",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun WarningIcon() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(51.dp)
            .height(46.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_statistics_warning_triangle),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(R.drawable.ic_statistics_warning_exclamation),
            contentDescription = null,
            modifier = Modifier
                .width(6.dp)
                .height(23.dp)
        )
    }
}

@Composable
internal fun PreviousPeriodSection(
    state: StatisticsDashboardUiState,
    isDemo: Boolean,
    onRequestDemoMode: () -> Unit
) {
    SectionCard(title = "Сравнение с предыдущим периодом") {
        val comparison = state.comparison
        if (comparison == null) {
            EmptyChart("Недостаточно данных для сравнения с предыдущим периодом")
            return@SectionCard
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CompareMetric(
                label = "TIR",
                value = "${comparison.currentTir}%",
                delta = comparison.currentTir - comparison.previousTir,
                decreaseIsPositive = false,
                suffix = "%"
            )
            CompareMetric(
                label = "Средний сахар",
                value = comparison.currentAverage,
                delta = comparison.currentAverageValue - comparison.previousAverageValue,
                decreaseIsPositive = true
            )
            CompareMetric(
                label = "Гипо эпизоды",
                value = comparison.currentHypoEpisodes.toString(),
                delta = comparison.currentHypoEpisodes - comparison.previousHypoEpisodes,
                decreaseIsPositive = true
            )
        }
        ComparisonLineChart(
            current = comparison.currentSeries,
            previous = comparison.previousSeries,
            axisDates = comparison.axisDates,
            normalStart = comparison.normalStart,
            normalEnd = comparison.normalEnd,
            isDemo = isDemo,
            modifier = Modifier
                .padding(top = 15.dp)
                .then(
                    if (BuildConfig.DEBUG) Modifier.pointerInput(Unit) {
                        detectTapGestures(onLongPress = { onRequestDemoMode() })
                    } else Modifier
                )
        )
    }
}

@Composable
private fun ComparisonLineChart(
    current: List<DailyGlucosePoint>,
    previous: List<DailyGlucosePoint>,
    axisDates: List<org.threeten.bp.LocalDate>,
    normalStart: Double,
    normalEnd: Double,
    isDemo: Boolean,
    modifier: Modifier = Modifier
) {
    val values = (current + previous).mapNotNull { it.value }
    val minValue = 0.0
    val maxValue = comparisonAxisMaximum(values.maxOrNull() ?: 0.0, normalEnd)
    val range = maxValue - minValue
    Box(modifier = modifier.fillMaxWidth().height(164.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(start = 28.dp, top = 6.dp, bottom = 50.dp)) {
            listOf(0f, 1f / 3f, 2f / 3f).forEach { ratio ->
                drawLine(
                    Divider.copy(alpha = .65f),
                    Offset(0f, size.height * ratio),
                    Offset(size.width, size.height * ratio),
                    1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
                )
            }
            drawSmoothGlucoseSeries(
                points = previous,
                minValue = minValue,
                range = range,
                maxGapFraction = 1.5f / axisDates.size.coerceAtLeast(1),
                colorForValue = { Divider }
            )
            drawSmoothGlucoseSeries(
                points = current,
                minValue = minValue,
                range = range,
                maxGapFraction = 1.5f / axisDates.size.coerceAtLeast(1),
                colorForValue = { value ->
                    when {
                        value < normalStart -> Red
                        value > normalEnd -> Orange
                        else -> GreenDark
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 6.dp)
                .height(108.dp)
                .width(26.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(maxValue, maxValue * 2 / 3, maxValue / 3, minValue).forEach {
                Text(NumberFormatter.format(it), color = TextSecondary, fontSize = 8.sp)
            }
        }
        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(start = 28.dp, bottom = 27.dp).fillMaxWidth()) {
            axisDates.forEach { date ->
                Text(
                    date.dayOfMonth.toString(),
                    modifier = Modifier.weight(1f),
                    color = TextSecondary,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Text(
            "Дата",
            color = TextSecondary,
            fontSize = 8.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
        )
        if (isDemo) {
            Text(
                text = "Демо-данные",
                color = TextSecondary,
                fontSize = 8.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp)
            )
        }
    }
}

private fun comparisonAxisMaximum(maxValue: Double, normalEnd: Double): Double {
    val minimumAxisMaximum = maxOf(12.0, normalEnd + 2.0)
    val requiredMaximum = maxOf(minimumAxisMaximum, maxValue)
    return kotlin.math.ceil(requiredMaximum / 4.0) * 4.0
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmoothGlucoseSeries(
    points: List<DailyGlucosePoint>,
    minValue: Double,
    range: Double,
    maxGapFraction: Float,
    colorForValue: (Double) -> Color
) {
    val groups = mutableListOf<List<Pair<Offset, Double>>>()
    var currentGroup = mutableListOf<Pair<Offset, Double>>()

    points.forEach { point ->
        val value = point.value
        if (value == null) {
            if (currentGroup.isNotEmpty()) groups += currentGroup
            currentGroup = mutableListOf()
            return@forEach
        }
        val x = size.width * point.position
        val y = size.height * (1f - ((value - minValue) / range).toFloat())
        val offset = Offset(x, y)
        if (currentGroup.lastOrNull()?.first?.let { x - it.x > size.width * maxGapFraction } == true) {
            groups += currentGroup
            currentGroup = mutableListOf()
        }
        currentGroup += offset to value
    }
    if (currentGroup.isNotEmpty()) groups += currentGroup

    groups.filter { it.size > 1 }.forEach { group ->
        group.zipWithNext().forEachIndexed { index, (from, to) ->
            val before = group[(index - 1).coerceAtLeast(0)].first
            val after = group[(index + 2).coerceAtMost(group.lastIndex)].first
            val controlOne = Offset(
                x = from.first.x + (to.first.x - before.x) / 6f,
                y = from.first.y + (to.first.y - before.y) / 6f
            )
            val controlTwo = Offset(
                x = to.first.x - (after.x - from.first.x) / 6f,
                y = to.first.y - (after.y - from.first.y) / 6f
            )
            drawPath(
                path = Path().apply {
                    moveTo(from.first.x, from.first.y)
                    cubicTo(controlOne.x, controlOne.y, controlTwo.x, controlTwo.y, to.first.x, to.first.y)
                },
                color = colorForValue((from.second + to.second) / 2.0),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, subtitle: String? = null, background: Color = ScreenBackground, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 27.dp)
            .background(background)
            .padding(top = 3.dp, bottom = 13.dp)
    ) {
        Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        subtitle?.let { Text(it, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp)) }
        content()
        SectionDivider()
    }
}

@Composable
private fun ChartLegend() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Legend("Низкий", Red)
        Legend("Норма", Green)
        Legend("Высокий", Orange)
    }
}

@Composable
private fun SectionDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .height(1.dp)
            .background(Divider)
    )
}

@Composable
private fun Legend(text: String, color: Color) {
    Text("●  $text", color = color.copy(alpha = 0.8f), fontSize = 10.sp)
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun CompareMetric(
    label: String,
    value: String,
    delta: Number,
    decreaseIsPositive: Boolean,
    suffix: String = ""
) {
    val isPositive = if (decreaseIsPositive) delta.toDouble() <= 0 else delta.toDouble() >= 0
    val arrow = if (delta.toDouble() < 0) "↓" else if (delta.toDouble() > 0) "↑" else "—"
    val deltaText = when (delta) {
        is Double -> NumberFormatter.format(kotlin.math.abs(delta))
        else -> kotlin.math.abs(delta.toInt()).toString()
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 10.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            text = "$arrow $deltaText$suffix",
            color = if (isPositive) GreenDark else Red,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun EmptyChart(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

private fun HourlyRangeStatus.toChartColor(): Color = when (this) {
    HourlyRangeStatus.LOW -> Red
    HourlyRangeStatus.IN_RANGE -> Green
    HourlyRangeStatus.HIGH -> Orange
    HourlyRangeStatus.NO_DATA -> Divider.copy(alpha = 0.32f)
}
