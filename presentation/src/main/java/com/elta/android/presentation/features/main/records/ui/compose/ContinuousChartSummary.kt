package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R
import java.util.Locale
import org.threeten.bp.LocalDate

@Composable
internal fun ContinuousAxisLabels(modifier: Modifier, textAlign: TextAlign = TextAlign.Start) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.SpaceBetween
) {
    listOf("16", "12", "8", "4", "0").forEach {
        Text(
            text = it,
            fontSize = 11.sp,
            color = ContinuousSecondary,
            fontWeight = FontWeight.Medium,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
internal fun ContinuousLayerButton(
    icon: Int,
    description: String,
    active: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(32.dp)
            .clip(CircleShape)
            .background(if (active) color.copy(alpha = 0.16f) else Color(0xFFF3F4F6))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = description,
            tint = if (active) color else Color(0xFFB0B3BA),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable private fun ContinuousLegendItem(color: Color, label: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(8.dp).clip(CircleShape).background(color))
    Spacer(Modifier.width(5.dp))
    Text(label, fontSize = 11.sp, color = ContinuousSecondary)
}

@Composable
internal fun ContinuousLegend(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ContinuousLegendItem(ContinuousLow, "Низкий")
        ContinuousLegendItem(ContinuousNormal, "Норма")
        ContinuousLegendItem(ContinuousHigh, "Высокий")
    }
}

@Composable
internal fun ContinuousStatisticsSummary(
    title: String,
    statistics: ContinuousStatistics,
    viewportDuration: Long,
    compact: Boolean,
    denseMetrics: Boolean
) {
    val isSingleDay = viewportDuration <= DAY_MINUTES
    val totalMinutes = if (isSingleDay) DAY_MINUTES else viewportDuration
    val normalMinutes = if (statistics.count == 0) 0L else statistics.normal * totalMinutes / statistics.count
    val highMinutes = if (statistics.count == 0) 0L else statistics.high * totalMinutes / statistics.count
    val lowMinutes = if (statistics.count == 0) 0L else statistics.low * totalMinutes / statistics.count

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compactRowHeight = maxHeight / 2f
        if (compact) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().heightIn(min = compactRowHeight), verticalAlignment = Alignment.CenterVertically) {
                    ContinuousDateMetric(title, Modifier.weight(1.1f))
                    ContinuousAverageMetric(statistics, isSingleDay, Modifier.weight(0.9f), compact = true)
                    ContinuousTirMetrics(statistics, normalMinutes, highMinutes, lowMinutes, isSingleDay, Modifier.weight(1.7f), compact = true)
                }
                ContinuousVariabilityMetrics(statistics, Modifier.fillMaxWidth().heightIn(min = compactRowHeight), compact = true)
            }
        } else {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                ContinuousDateMetric(title, Modifier.weight(1.1f))
                ContinuousAverageMetric(statistics, isSingleDay, Modifier.weight(0.9f), compact = denseMetrics)
                ContinuousTirMetrics(statistics, normalMinutes, highMinutes, lowMinutes, isSingleDay, Modifier.weight(1.6f), compact = denseMetrics)
                ContinuousVariabilityMetrics(statistics, Modifier.weight(1.3f), compact = denseMetrics)
            }
        }
    }
}

@Composable
private fun ContinuousDateMetric(title: String, modifier: Modifier) {
    Text(title, modifier = modifier, fontSize = 13.sp, lineHeight = 15.sp,
        color = ContinuousPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
}

@Composable
private fun ContinuousAverageMetric(s: ContinuousStatistics, isSingleDay: Boolean, modifier: Modifier, compact: Boolean) {
    ContinuousMetric(
        value = continuousFormat(s.average),
        subtitle = if (isSingleDay) "средний за день" else "средний за период",
        label = "ммоль/л",
        modifier = modifier,
        color = Color(0xFFEE7300),
        compact = compact
    )
}

@Composable
private fun ContinuousTirMetrics(
    s: ContinuousStatistics,
    normalMinutes: Long,
    highMinutes: Long,
    lowMinutes: Long,
    isSingleDay: Boolean,
    modifier: Modifier,
    compact: Boolean
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        ContinuousTirColumn(ContinuousNormal, percent(s.normal, s.count), formatDuration(normalMinutes, isSingleDay), Modifier.weight(1f), compact)
        ContinuousTirColumn(ContinuousHigh, percent(s.high, s.count), formatDuration(highMinutes, isSingleDay), Modifier.weight(1f), compact)
        ContinuousTirColumn(ContinuousLow, percent(s.low, s.count), formatDuration(lowMinutes, isSingleDay), Modifier.weight(1f), compact)
    }
}

@Composable
private fun ContinuousVariabilityMetrics(s: ContinuousStatistics, modifier: Modifier, compact: Boolean) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        ContinuousMetric(s.cv?.let { "$it%" } ?: "-", "", "CV", Modifier.weight(1f), compact = compact)
        ContinuousMetric(s.sd?.let(::continuousFormat) ?: "-", "", "SD", Modifier.weight(1f), compact = compact)
        ContinuousMetric(s.gmi?.let { "${continuousFormat(it)}%" } ?: "-", "", "GMI", Modifier.weight(1f), compact = compact)
    }
}

@Composable
private fun ContinuousTirColumn(
    dotColor: Color,
    percentText: String,
    timeText: String,
    modifier: Modifier,
    compact: Boolean
) = Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(3.dp))
        Text("TIR", fontSize = 10.sp, color = ContinuousSecondary)
    }
    Text(percentText, fontSize = if (compact) 16.sp else 20.sp,
        fontWeight = FontWeight.Bold, color = ContinuousPrimary, maxLines = 1)
    Text(timeText, fontSize = if (compact) 9.sp else 11.sp,
        color = ContinuousSecondary, maxLines = 1)
}

private fun formatDuration(mins: Long, isSingleDay: Boolean): String {
    return if (isSingleDay) {
        val h = mins / 60
        val m = mins % 60
        if (h >= 24) "24ч" else "${h}ч ${String.format(Locale.US, "%02d", m)}м"
    } else {
        val days = mins / (24 * 60)
        val remainingHours = (mins % (24 * 60)) / 60
        val m = mins % 60
        when {
            days > 0 -> "${days}д ${remainingHours}ч"
            remainingHours > 0 -> "${remainingHours}ч ${m}м"
            else -> "${m}м"
        }
    }
}

@Composable
private fun ContinuousMetric(
    value: String,
    subtitle: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = ContinuousPrimary,
    icon: Int? = null,
    compact: Boolean = false
) = Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
        }
        Text(label, fontSize = if (compact) 10.sp else 11.sp, color = ContinuousSecondary, maxLines = 1)
    }
    Text(value, fontSize = if (compact) 16.sp else 20.sp, color = color,
        fontWeight = FontWeight.Bold, maxLines = 1)
    if (subtitle.isNotEmpty()) Text(subtitle, fontSize = if (compact) 9.sp else 10.sp,
        color = ContinuousSecondary, maxLines = 2, lineHeight = if (compact) 10.sp else 11.sp)
}

@Composable
internal fun ContinuousSelectedPointSummary(
    point: DetailedGlucosePoint,
    compact: Boolean,
    denseMetrics: Boolean
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compactRowHeight = maxHeight / 2f
        val trendColor = when {
            point.trendValue.startsWith("+") -> ContinuousHigh
            point.trendValue.startsWith("-") -> ContinuousLow
            else -> ContinuousNormal
        }
        if (compact) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().heightIn(min = compactRowHeight), verticalAlignment = Alignment.CenterVertically) {
                    ContinuousMetric(point.timeLabel, "", "Время", Modifier.weight(1f), compact = true)
                    ContinuousMetric(continuousFormat(point.value), "ммоль/л", "Глюкоза", Modifier.weight(1f), continuousPointColor(point.value), compact = true)
                    ContinuousMetric(point.trendValue, point.trendText, "Тренд", Modifier.weight(1f), trendColor, compact = true)
                }
                Row(Modifier.fillMaxWidth().heightIn(min = compactRowHeight), verticalAlignment = Alignment.CenterVertically) {
                    ContinuousMetric(point.foodUnits ?: "-", point.foodTimeAgo ?: "", "Еда", Modifier.weight(1f), ContinuousHigh, R.drawable.ic_spoon_and_fork_orange, true)
                    ContinuousMetric(point.insulinUnits ?: "-", point.insulinTimeAgo ?: "", "Инсулин", Modifier.weight(1f), Color(0xFF2E7BE6), R.drawable.ic_syringe_blue, true)
                    ContinuousMetric(point.activityDuration ?: "-", point.activityTimeAgo ?: "", "Активность", Modifier.weight(1f), Color(0xFF8B5CF6), R.drawable.ic_walking_blue, true)
                }
            }
        } else {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                ContinuousMetric(point.timeLabel, "", "Время", Modifier.weight(1f), compact = denseMetrics)
                ContinuousMetric(continuousFormat(point.value), "ммоль/л", "Глюкоза", Modifier.weight(1f), continuousPointColor(point.value), compact = denseMetrics)
                ContinuousMetric(point.trendValue, point.trendText, "Тренд", Modifier.weight(1f), trendColor, compact = denseMetrics)
                ContinuousMetric(point.foodUnits ?: "-", point.foodTimeAgo ?: "", "Еда", Modifier.weight(1f), ContinuousHigh, R.drawable.ic_spoon_and_fork_orange, denseMetrics)
                ContinuousMetric(point.insulinUnits ?: "-", point.insulinTimeAgo ?: "", "Инсулин", Modifier.weight(1f), Color(0xFF2E7BE6), R.drawable.ic_syringe_blue, denseMetrics)
                ContinuousMetric(point.activityDuration ?: "-", point.activityTimeAgo ?: "", "Активность", Modifier.weight(1f), Color(0xFF8B5CF6), R.drawable.ic_walking_blue, denseMetrics)
            }
        }
    }
}

@Composable
internal fun ContinuousSelectedEventSummary(event: ContinuousEvent, origin: LocalDate) {
    val title = when (event.kind) {
        ContinuousEventKind.FOOD -> "Еда"
        ContinuousEventKind.INSULIN -> "Инсулин"
        ContinuousEventKind.ACTIVITY -> "Активность"
    }
    val icon = when (event.kind) {
        ContinuousEventKind.FOOD -> R.drawable.ic_spoon_and_fork_orange
        ContinuousEventKind.INSULIN -> R.drawable.ic_syringe_blue
        ContinuousEventKind.ACTIVITY -> R.drawable.ic_walking_blue
    }
    val date = origin.plusDays(event.startMinute / DAY_MINUTES)
    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        ContinuousMetric(event.timeLabel, "${date.dayOfMonth}.${date.monthValue}", "Время", Modifier.weight(1f), compact = true)
        ContinuousMetric(title, "", "Событие", Modifier.weight(1f), event.color, icon, true)
        ContinuousMetric(event.label, "", "Значение", Modifier.weight(1f), event.color, compact = true)
    }
}
