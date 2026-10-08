package com.elta.android.presentation.features.statistic.period.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elta.android.presentation.features.statistic.period.ui.Period

private val DEMO_CURRENT_SERIES = listOf(
    3.4, 0.8, 3.8, 4.2, 7.6, 6.1, 12.9,
    11.7, 3.7, 3.4, 5.4, 4.8, 1.6, 2.5
)
private val DEMO_PREVIOUS_SERIES = listOf(
    3.1, 5.9, 3.7, 8.2, 6.0, 5.8, 7.4,
    9.1, 8.0, 4.1, 3.5, 5.1, 9.3, 7.8
)

@Composable
internal fun DemoModePicker(
    isDemoMode: Boolean,
    onDemoModeSelected: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text("Режим отображения", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(
                "Выберите данные для всего экрана статистики",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            DemoModeOption(
                title = "Реальные данные",
                isSelected = !isDemoMode,
                onClick = { onDemoModeSelected(false) }
            )
            DemoModeOption(
                title = "Демо-данные",
                isSelected = isDemoMode,
                onClick = { onDemoModeSelected(true) }
            )
            Text(
                text = "Отмена",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun DemoModeOption(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) GreenDark.copy(alpha = 0.12f) else ScreenBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), color = TextPrimary, fontSize = 13.sp)
        Text(if (isSelected) "✓" else "", color = GreenDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
    Spacer(modifier = Modifier.height(8.dp))
}

internal fun StatisticsDashboardUiState.toDemoDashboard(): StatisticsDashboardUiState {
    val endDate = org.threeten.bp.LocalDate.now()
    val dates = (13 downTo 0).map { endDate.minusDays(it.toLong()) }
    return copy(
        period = Period.FOURTEEN,
        periodTitle = "12 июля – 25 июля",
        average = "6,8",
        lowPercent = 5,
        inRangePercent = 78,
        highPercent = 17,
        lowCount = 14,
        inRangeCount = 214,
        highCount = 48,
        minLabel = "1,7",
        maxLabel = "15,8",
        coefficientOfVariation = "31%",
        standardDeviation = "2,1",
        gmi = "6,7",
        nightHypoEpisodes = 4,
        hourlyRanges = dates.mapIndexed { dayIndex, date ->
            HourlyRange(
                date = date,
                dayLabel = date.toDemoWeekday(),
                statuses = (0..23).map { hour ->
                    when {
                        hour in 2..3 && dayIndex % 5 == 0 -> HourlyRangeStatus.LOW
                        hour in 12..13 && dayIndex % 4 == 0 -> HourlyRangeStatus.HIGH
                        hour in 6..20 && (hour + dayIndex) % 3 != 0 -> HourlyRangeStatus.IN_RANGE
                        else -> HourlyRangeStatus.NO_DATA
                    }
                }
            )
        },
        dailyRangeTitle = "19 июля – 25 июля",
        distribution = listOf(28, 16, 86, 120, 42, 18),
        dailyEpisodes = dates.mapIndexed { index, date ->
            DailyEpisodeCount(
                date = date,
                low = if (index in listOf(1, 7, 11)) 1 else 0,
                high = if (index in listOf(4, 9, 12)) 1 else 0
            )
        },
        comparison = ComparisonUiState(
            currentTir = 78,
            previousTir = 83,
            currentAverage = "6,8",
            previousAverage = "7,2",
            currentAverageValue = 6.8,
            previousAverageValue = 7.2,
            normalStart = 3.9,
            normalEnd = 10.0,
            currentHypoEpisodes = 4,
            previousHypoEpisodes = 6,
            axisDates = dates,
            currentSeries = dates.toDemoSeries(DEMO_CURRENT_SERIES),
            previousSeries = dates.toDemoSeries(DEMO_PREVIOUS_SERIES)
        )
    )
}

private fun org.threeten.bp.LocalDate.toDemoWeekday(): String = when (dayOfWeek.value) {
    1 -> "ПН"
    2 -> "ВТ"
    3 -> "СР"
    4 -> "ЧТ"
    5 -> "ПТ"
    6 -> "СБ"
    else -> "ВС"
}

private fun List<org.threeten.bp.LocalDate>.toDemoSeries(values: List<Double>): List<DailyGlucosePoint> =
    mapIndexed { index, date ->
        val samplePosition = index.toFloat() * values.lastIndex / lastIndex.coerceAtLeast(1)
        val lowerIndex = samplePosition.toInt()
        val upperIndex = (lowerIndex + 1).coerceAtMost(values.lastIndex)
        val fraction = samplePosition - lowerIndex
        DailyGlucosePoint(
            date = date,
            value = values[lowerIndex] + (values[upperIndex] - values[lowerIndex]) * fraction,
            position = (index + 0.5f) / size.coerceAtLeast(1)
        )
    }
