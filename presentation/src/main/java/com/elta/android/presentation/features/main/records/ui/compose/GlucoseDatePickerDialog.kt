package com.elta.android.presentation.features.main.records.ui.compose

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.elta.android.presentation.R
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

enum class DayGlycemicStatus {
    NORM, HIGH, LOW, NONE
}

data class CalendarDay(
    val dayNumber: Int,
    val isCurrentMonth: Boolean = true,
    val status: DayGlycemicStatus = DayGlycemicStatus.NONE
)

private val MONTH_NAMES_RU = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)

private val GENITIVE_MONTHS_RU = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)

@Composable
fun GlucoseDatePickerDialog(
    initialDate: String = "28 июля 2026",
    onDismissRequest: () -> Unit = {},
    onDateSelected: (String) -> Unit = {}
) {
    // Parse initial date string or default to current date
    val parsedDate = remember(initialDate) { parseInitialDate(initialDate) }

    var selectedDate by remember { mutableStateOf(parsedDate) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(parsedDate)) }

    val daysInGrid = remember(currentYearMonth) {
        val daysList = mutableListOf<CalendarDay>()
        val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value // 1 = Mon ... 7 = Sun
        val leadingEmptyCount = firstDayOfWeek - 1

        for (i in 0 until leadingEmptyCount) {
            daysList.add(CalendarDay(0, isCurrentMonth = false))
        }

        val totalDays = currentYearMonth.lengthOfMonth()
        for (day in 1..totalDays) {
            val mockStatus = when (day % 4) {
                1 -> DayGlycemicStatus.NORM
                2 -> DayGlycemicStatus.HIGH
                3 -> DayGlycemicStatus.LOW
                else -> DayGlycemicStatus.NONE
            }
            daysList.add(CalendarDay(day, isCurrentMonth = true, status = mockStatus))
        }
        daysList
    }

    val currentMonthTitle = "${MONTH_NAMES_RU[currentYearMonth.monthValue - 1]} ${currentYearMonth.year}"
    val selectedDateFormatted = "${selectedDate.dayOfMonth} ${GENITIVE_MONTHS_RU[selectedDate.monthValue - 1]} ${selectedDate.year}"

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
                .background(Color.Black.copy(alpha = 0.5f))
                .rotateLandscape()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column: Instructions and Selection Display
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Выберите дату",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF17191F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "и мы покажем вам\nподробную статистику дня",
                                fontSize = 14.sp,
                                color = Color(0xFF878B93),
                                lineHeight = 20.sp
                            )
                        }

                        Column {
                            Text(
                                text = "Выбранная дата",
                                fontSize = 12.sp,
                                color = Color(0xFF878B93)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedDateFormatted,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5C3882),
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }

                    // Vertical Divider Line
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color(0xFFEAEAEA))
                    )

                    // Right Column: Calendar Grid
                    Column(
                        modifier = Modifier
                            .weight(1.4f)
                            .fillMaxHeight()
                            .padding(start = 24.dp)
                    ) {
                        // Month Header & Navigation Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        currentYearMonth = currentYearMonth.minusMonths(1)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_arrow_left),
                                        contentDescription = "Previous Month",
                                        tint = Color(0xFF878B93)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = currentMonthTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF17191F)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(
                                    onClick = {
                                        currentYearMonth = currentYearMonth.plusMonths(1)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_arrow_left),
                                        contentDescription = "Next Month",
                                        tint = Color(0xFF878B93),
                                        modifier = Modifier.rotate(180f)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.btn_close),
                                    contentDescription = "Close",
                                    tint = Color(0xFF878B93)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Weekday labels (Пн, Вт, Ср, Чт, Пт, Сб, Вс)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                            weekdays.forEach { dayName ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayName,
                                        fontSize = 12.sp,
                                        color = Color(0xFFB0B3BA),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Days Grid (7 columns)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(daysInGrid) { day ->
                                if (!day.isCurrentMonth) {
                                    Box(modifier = Modifier.size(36.dp))
                                } else {
                                    val isSelected = currentYearMonth == YearMonth.from(selectedDate) && day.dayNumber == selectedDate.dayOfMonth

                                    val (bgColor, textColor) = when {
                                        isSelected -> Color(0xFF363E4E) to Color.White
                                        day.status == DayGlycemicStatus.NORM -> Color(0xFFE1F5EC) to Color(0xFF17191F)
                                        day.status == DayGlycemicStatus.HIGH -> Color(0xFFFFF3E0) to Color(0xFF17191F)
                                        day.status == DayGlycemicStatus.LOW -> Color(0xFFFDE8E8) to Color(0xFF17191F)
                                        else -> Color.Transparent to Color(0xFF17191F)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(bgColor)
                                            .clickable {
                                                val newDate = currentYearMonth.atDay(day.dayNumber)
                                                selectedDate = newDate
                                                val dateStr = "${newDate.dayOfMonth} ${GENITIVE_MONTHS_RU[newDate.monthValue - 1]} ${newDate.year}"
                                                onDateSelected(dateStr)
                                                onDismissRequest()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.dayNumber.toString(),
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseInitialDate(dateStr: String): LocalDate {
    return try {
        val parts = dateStr.trim().split(" ")
        if (parts.size >= 3) {
            val day = parts[0].toIntOrNull() ?: 28
            val monthIdx = GENITIVE_MONTHS_RU.indexOfFirst { it.lowercase() == parts[1].lowercase() }
            val month = if (monthIdx != -1) monthIdx + 1 else 7
            val year = parts[2].toIntOrNull() ?: 2026
            LocalDate.of(year, month, day)
        } else {
            LocalDate.now()
        }
    } catch (e: Exception) {
        LocalDate.now()
    }
}
