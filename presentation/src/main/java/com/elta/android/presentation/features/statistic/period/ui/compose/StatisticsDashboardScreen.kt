@file:Suppress("LongMethod", "MagicNumber")

package com.elta.android.presentation.features.statistic.period.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R
import com.elta.android.presentation.features.statistic.period.ui.Period

private val Cyan = Color(0xFF1FBFD2)
private val ScreenBackground = Color(0xFFF4F4F4)
private val TextPrimary = Color(0xFF3D4556)
private val TextSecondary = Color(0x8C3D4556)
private val Green = Color(0xFF43E695)
private val GreenDark = Color(0xFF29AF99)
private val Orange = Color(0xFFF2A515)
private val Red = Color(0xFFD93B17)
private val Divider = Color(0xFFBBC0CA)

@Composable
fun StatisticsDashboardScreen(
    uiState: StatisticsDashboardUiState,
    onPeriodSelected: (Period) -> Unit,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(Cyan)) {
        StatisticsTopBar(
            uiState = uiState,
            onPeriodSelected = onPeriodSelected,
            onBack = onBack,
            onSettingsClick = onSettingsClick
        )
        Surface(
            modifier = Modifier.padding(top = 171.dp).fillMaxSize(),
            color = ScreenBackground,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 31.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { MainIndicatorsSection(uiState) }
                item { DistributionSection(uiState) }
                item { DailyVariationSection(uiState) }
                item { HypoHyperSection(uiState) }
                item { KeyMetricsSection(uiState) }
                item { PreviousPeriodSection(uiState) }
            }
        }
    }
}

@Composable
private fun StatisticsTopBar(
    uiState: StatisticsDashboardUiState,
    onPeriodSelected: (Period) -> Unit,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(26.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material.Icon(
                painter = painterResource(R.drawable.ic_arrow_left),
                contentDescription = "Назад",
                tint = Color.White,
                modifier = Modifier.size(24.dp).rotate(180f).clickable(onClick = onBack)
            )
            Text(
                text = "Статистика",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 17.sp
            )
            androidx.compose.material.Icon(
                painter = painterResource(R.drawable.ic_chart_settings),
                contentDescription = "Настройки статистики",
                tint = Color.White,
                modifier = Modifier.size(24.dp).clickable(onClick = onSettingsClick)
            )
        }
        Box(modifier = Modifier.padding(horizontal = 13.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(ScreenBackground)
                    .clickable { expanded = true }
                    .padding(horizontal = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(uiState.periodTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Text(uiState.period.displayName, color = TextPrimary, fontSize = 14.sp)
                androidx.compose.material.Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = "Выбрать период",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                Period.values().forEach { period ->
                    DropdownMenuItem(onClick = { expanded = false; onPeriodSelected(period) }) {
                        Text(period.displayName)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainIndicatorsSection(state: StatisticsDashboardUiState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            val gaugeSize = minOf(maxWidth * 0.68f, 224.dp).coerceAtLeast(170.dp)
            Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)) {
                GlucoseGauge(average = state.average, unit = state.unit, size = gaugeSize)
            }
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MinMaxLabel("min ${state.minLabel}", Red)
                TimeInRangePill("TBR", state.lowPercent, state.lowCount, Red)
                TimeInRangePill("TIR", state.inRangePercent, state.inRangeCount, Green)
                TimeInRangePill("TAR", state.highPercent, state.highCount, Orange)
                MinMaxLabel("max ${state.maxLabel}", Orange)
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
private fun TimeInRangePill(label: String, percent: Int, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, Color(0xFF626876), RoundedCornerShape(26.dp))
            .background(ScreenBackground)
            .padding(vertical = 5.dp)
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
private fun MinMaxLabel(text: String, color: Color) {
    Text(text, color = Color.White, fontSize = 11.sp, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color).padding(horizontal = 6.dp, vertical = 3.dp))
}

@Composable
private fun DistributionSection(state: StatisticsDashboardUiState) {
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
    Box(modifier = Modifier.fillMaxWidth().height(132.dp).padding(top = 8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(start = 24.dp, bottom = 19.dp)) {
            val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
            val gap = size.width / (values.size * 2f)
            val dash = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
            listOf(0.25f, 0.5f, 0.75f).forEach { ratio ->
                drawLine(Divider.copy(alpha = .65f), Offset(0f, size.height * ratio), Offset(size.width, size.height * ratio), 1.dp.toPx(), pathEffect = dash)
            }
            values.forEachIndexed { index, value ->
                val height = size.height * value / max * 0.76f
                val left = gap * (index * 2 + 0.5f)
                drawRoundRect(colors[index], Offset(left, size.height - height), Size(gap, height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
            }
            drawLine(Divider, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
        }
        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(bottom = 19.dp).height(94.dp).width(22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) { listOf("600", "400", "200", "0").forEach { Text(it, color = TextSecondary, fontSize = 8.sp) } }
        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(start = 24.dp).fillMaxWidth()) {
            labels.forEach { label -> Text(label, modifier = Modifier.weight(1f), color = TextSecondary, fontSize = 8.sp, textAlign = TextAlign.Center) }
        }
    }
}

@Composable
private fun DailyVariationSection(state: StatisticsDashboardUiState) {
    SectionCard(title = "Суточные колебания", subtitle = state.periodTitle) {
        if (state.hourlyRanges.isEmpty()) {
            EmptyChart("Недостаточно измерений для построения графика")
        } else {
            Heatmap(state.hourlyRanges)
        }
    }
}

@Composable
private fun Heatmap(days: List<HourlyRange>) {
    val visibleDays = days.takeLast(14)
    Row(modifier = Modifier.fillMaxWidth().height(186.dp).padding(top = 4.dp)) {
        Column(modifier = Modifier.width(23.dp), verticalArrangement = Arrangement.SpaceBetween) {
            listOf("00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "24:00").forEach { Text(it, color = TextSecondary, fontSize = 8.sp) }
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            visibleDays.forEach { day ->
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(day.dayLabel, color = TextSecondary, fontSize = 9.sp)
                    Column(modifier = Modifier.padding(top = 3.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        day.statuses.forEach { status ->
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .background(status.toChartColor(), RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HypoHyperSection(state: StatisticsDashboardUiState) {
    SectionCard(title = "Статистика гипо/гипергликемии") {
        Text("Количество измерений", color = TextSecondary, fontSize = 10.sp)
        Box(modifier = Modifier.fillMaxWidth().height(122.dp).padding(top = 8.dp)) {
            val entries = listOf(state.lowCount, 0, state.highCount, 0, state.lowCount / 2, 0, state.highCount / 2)
            Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 18.dp)) {
            val max = entries.maxOrNull()?.coerceAtLeast(1) ?: 1
            entries.forEachIndexed { index, entry ->
                if (entry == 0) return@forEachIndexed
                val height = size.height * entry / max * .65f
                val width = size.width / entries.size * .32f

                val left = (index + .5f) * size.width / entries.size - width / 2
                drawRoundRect(if (index % 3 == 0) Red else Orange, Offset(left, size.height - height - 15.dp.toPx()), Size(width, height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
            }
            drawLine(Divider, Offset(0f, size.height - 15.dp.toPx()), Offset(size.width, size.height - 15.dp.toPx()), 1.dp.toPx())
            }
            Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                listOf("12", "14", "16", "18", "20", "22", "24").forEach { label -> Text(label, modifier = Modifier.weight(1f), color = TextSecondary, fontSize = 8.sp, textAlign = TextAlign.Center) }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Legend("Гипо", Red)
            Legend("Гипер", Orange)
        }
    }
}

@Composable
private fun KeyMetricsSection(state: StatisticsDashboardUiState) {
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
        Row(modifier = Modifier.padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            WarningIcon()
            Column {
                Text("Предупреждение", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "${state.nightHypoEpisodes} эпизода между 02:00 и 05:00",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun WarningIcon() {
    val stroke = with(LocalDensity.current) { 2.dp.toPx() }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(51.dp, 45.dp).padding(horizontal = 2.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(size.width / 2f, stroke)
                lineTo(size.width - stroke, size.height - stroke)
                lineTo(stroke, size.height - stroke)
                close()
            }
            drawPath(path, color = Color(0xFFFFCC00), style = Stroke(width = stroke))
        }
        Text("!", color = Orange, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    }
}

@Composable
private fun PreviousPeriodSection(state: StatisticsDashboardUiState) {
    SectionCard(title = "Сравнение с предыдущим периодом", background = Color.White) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CompareMetric("TIR", "${state.inRangePercent}%", "—")
            CompareMetric("Средний сахар", state.average, "—")
            CompareMetric("Гипо эпизоды", state.lowCount.toString(), "—")
        }
        Text(
            text = "Сравнение появится после загрузки предыдущего периода",
            color = TextSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp).fillMaxWidth()
        )
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
private fun CompareMetric(label: String, value: String, delta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 10.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(delta, color = GreenDark, fontSize = 11.sp)
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
