@file:Suppress("MagicNumber", "LongMethod")

package com.elta.android.presentation.features.statistic.period.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.core.compose.widgets.text.MetricValueText
import com.elta.android.presentation.utils.NumberFormatter
import kotlin.math.roundToInt

private val ActivityPurple = Color(0xFF9754FF)
private val FatBrown = Color(0xFF9E736B)
private val ProteinPink = Color(0xFFEA76A6)
private val CarbohydrateCyan = Color(0xFF42DCEB)

@Composable
internal fun ActivitySection(state: StatisticsDashboardUiState) {
    SectionCard(title = "Активность", minHeight = 390.dp) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(state.unit, color = TextSecondary, fontSize = 10.sp)
            Text("Минуты активности", color = TextSecondary, fontSize = 10.sp)
        }
        val days = state.activity.days.takeLast(14)
        if (days.isEmpty()) {
            AdditionalEmptyChart("Добавьте активность в дневник")
        } else {
            ActivityChart(days, state.normalStart, state.normalEnd)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Legend("Глюкоза", Green)
                Legend("Активность", ActivityPurple)
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("Активность в среднем за день", Modifier.fillMaxWidth(), color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
        MetricValueText(state.activity.averageMinutes?.let { "${NumberFormatter.format(it)} мин" } ?: "—",
            Modifier.fillMaxWidth(), color = ActivityPurple, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ActivityChart(days: List<ActivityDay>, normalStart: Double, normalEnd: Double) {
    val minutesMax = (kotlin.math.ceil((days.maxOfOrNull { it.minutes } ?: 0.0) / 60) * 60).coerceAtLeast(60.0)
    val glucoseMax = (kotlin.math.ceil((days.mapNotNull { it.glucose }.maxOrNull() ?: 0.0) / 4) * 4).coerceAtLeast(12.0)
    Column(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 12.dp)) {
        Row(Modifier.fillMaxWidth().height(150.dp)) {
            ActivityAxis(glucoseMax, Modifier.width(24.dp))
            Canvas(Modifier.weight(1f).fillMaxHeight().padding(vertical = 5.dp)) {
                val step = size.width / days.size
                listOf(0f, .25f, .5f, .75f, 1f).forEach { ratio ->
                    drawLine(
                        Divider.copy(alpha = .65f), Offset(0f, size.height * ratio), Offset(size.width, size.height * ratio),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
                    )
                }
                days.forEachIndexed { index, day ->
                    val height = (size.height * day.minutes / minutesMax).toFloat().coerceAtLeast(0f)
                    drawRect(ActivityPurple, Offset(step * (index + .3f), size.height - height), Size(step * .4f, height))
                }
                drawSmoothGlucoseSeries(
                    days.mapIndexed { index, day -> DailyGlucosePoint(day.date, day.glucose, (index + .5f) / days.size) },
                    0.0, glucoseMax, 2f / days.size
                ) { value -> when {
                    value < normalStart -> Red
                    value > normalEnd -> Orange
                    else -> Green
                } }
            }
            ActivityAxis(minutesMax, Modifier.width(30.dp).padding(start = 5.dp))
        }
        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 30.dp)) {
            days.forEach { day ->
                Text(day.date.dayOfMonth.toString(), Modifier.weight(1f), color = TextSecondary, fontSize = 9.sp, textAlign = TextAlign.Center)
            }
        }
        Text("Дата", Modifier.fillMaxWidth().padding(top = 5.dp), color = TextSecondary, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ActivityAxis(maximum: Double, modifier: Modifier) {
    Column(modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
        listOf(1.0, .75, .5, .25, 0.0).forEach { ratio ->
            Text((maximum * ratio).roundToInt().toString(), color = TextSecondary, fontSize = 8.sp, lineHeight = 10.sp)
        }
    }
}

@Composable
internal fun NutritionSection(state: StatisticsDashboardUiState) {
    val nutrition = state.nutrition
    SectionCard(title = "Питание", minHeight = 480.dp) {
        Text("БЖУ за указанный период", color = TextSecondary, fontSize = 11.sp)
        val macros = listOf(nutrition.fats, nutrition.proteins, nutrition.carbohydrates)
        val colors = listOf(FatBrown, ProteinPink, CarbohydrateCyan)
        val energy = listOf((nutrition.fats ?: 0.0) * 9, (nutrition.proteins ?: 0.0) * 4, (nutrition.carbohydrates ?: 0.0) * 4)
        val totalEnergy = energy.sum()
        Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().padding(4.dp)) {
                    val stroke = 7.dp.toPx()
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    if (totalEnergy == 0.0) drawArc(Divider.copy(alpha = .4f), 0f, 360f, false, Offset(stroke/2, stroke/2), arcSize, style = Stroke(stroke))
                    else energy.forEachIndexed { index, value ->
                        val start = -90f + (energy.take(index).sum() / totalEnergy * 360).toFloat()
                        drawArc(colors[index], start, (value / totalEnergy * 360).toFloat(), false,
                            Offset(stroke/2, stroke/2), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MetricValueText(nutrition.calories?.let { it.roundToInt().toString() } ?: "—", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("ккал", color = TextSecondary, fontSize = 10.sp)
                }
            }
            Column(Modifier.weight(1f).padding(start = 15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Жиры", "Белки", "Углеводы").forEachIndexed { index, title ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Legend(title, colors[index])
                        MetricValueText(macros[index]?.takeIf { it != 0.0 }?.let { "${NumberFormatter.format(it)} г" + if (totalEnergy > 0) " (${(energy[index] / totalEnergy * 100).roundToInt()}%)" else "" } ?: "—", color = TextPrimary, fontSize = 10.sp)
                    }
                }
            }
        }
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(Divider))
        Text("Углеводы (ХЕ) в среднем за день", Modifier.fillMaxWidth().padding(top = 18.dp), color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
        MetricValueText(nutrition.averageBreadUnits?.let { "${NumberFormatter.format(it)} ХЕ" } ?: "—", Modifier.fillMaxWidth().padding(bottom = 18.dp), color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(Divider))
        if (nutrition.meals.isEmpty()) {
            AdditionalEmptyChart("Для сравнения глюкозы отметьте измерения до и после еды")
        } else {
            val axisMax = nutrition.meals.flatMap { listOfNotNull(it.before, it.after) }.maxOrNull()?.coerceAtLeast(10.0) ?: 10.0
            Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                nutrition.meals.forEach { meal ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(meal.label, color = TextPrimary, fontSize = 10.sp)
                        Row(Modifier.height(92.dp).padding(top = 14.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(meal.before to Divider, meal.after to Orange).forEach { (value, color) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    MetricValueText(value?.let { NumberFormatter.format(it) } ?: "—", color = TextSecondary, fontSize = 9.sp)
                                    Spacer(Modifier.width(15.dp).height((65 * (value ?: 0.0) / axisMax).dp).background(color))
                                }
                            }
                        }
                        val delta = meal.before?.let { before -> meal.after?.minus(before) }
                        MetricValueText(delta?.let { (if (it > 0) "+" else "") + NumberFormatter.format(it) } ?: "—", color = Orange, fontSize = 11.sp)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Legend("До еды", Divider)
                Legend("После еды", Orange)
            }
        }
    }
}

@Composable
private fun AdditionalEmptyChart(message: String) {
    Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
        Text(message, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
