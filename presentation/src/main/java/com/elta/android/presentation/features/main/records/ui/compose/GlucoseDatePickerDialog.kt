package com.elta.android.presentation.features.main.records.ui.compose

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
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

private val CalendarBackground = Color(0xFF1FBFD2)
private val CalendarTextPrimary = Color(0xFF3D4556)
private val CalendarTextSecondary = Color(0xFF878B93)
private val CalendarBorder = Color(0xFFA4A4A4)

@Composable
fun GlucoseDatePickerDialog(
    initialDate: String = "28 июля 2026",
    dayStatuses: Map<LocalDate, DayGlycemicStatus> = emptyMap(),
    onDismissRequest: () -> Unit = {},
    onDateSelected: (String) -> Unit = {}
) {
    // Parse initial date string or default to current date
    val parsedDate = remember(initialDate) { parseInitialDate(initialDate) }

    var selectedDate by remember { mutableStateOf(parsedDate) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(parsedDate)) }

    val daysInGrid = remember(currentYearMonth, dayStatuses) {
        val daysList = mutableListOf<CalendarDay>()
        val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value // 1 = Mon ... 7 = Sun
        val leadingEmptyCount = firstDayOfWeek - 1

        for (i in 0 until leadingEmptyCount) {
            daysList.add(CalendarDay(0, isCurrentMonth = false))
        }

        val totalDays = currentYearMonth.lengthOfMonth()
        for (day in 1..totalDays) {
            val date = currentYearMonth.atDay(day)
            daysList.add(CalendarDay(day, isCurrentMonth = true, status = dayStatuses[date] ?: DayGlycemicStatus.NONE))
        }
        while (daysList.size % 7 != 0) {
            daysList.add(CalendarDay(0, isCurrentMonth = false))
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

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(CalendarBackground)
                .systemBarsPadding()
                .padding(top = 18.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val contentWidth = maxWidth.coerceAtMost(737.dp)
            val cardHeight = maxHeight.coerceAtMost(318.dp)

            Row(
                modifier = Modifier
                    .width(contentWidth)
                    .height(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onDismissRequest() }
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Назад",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = 29.dp)
                    .width(contentWidth)
                    .height(cardHeight)
                    .clip(RoundedCornerShape(13.dp))
                    .border(1.dp, CalendarBorder, RoundedCornerShape(13.dp))
                    .background(Color.White)
                    .padding(start = 40.dp, top = 24.dp, end = 22.dp, bottom = 28.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .width(296.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Выберите дату",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CalendarTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "и мы покажем вам\nподробную статистику дня",
                                fontSize = 16.sp,
                                color = CalendarTextSecondary,
                                lineHeight = 18.sp
                            )
                        }

                        Column {
                            Text(
                                text = "Выбранная дата",
                                fontSize = 12.sp,
                                color = Color(0xFFBBBFCA)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedDateFormatted,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CalendarTextPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color(0xFFE1E4E8))
                    )

                    Column(
                        modifier = Modifier
                            .padding(start = 42.dp)
                            .width(333.dp)
                            .fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                        ) {
                            IconButton(
                                onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_left),
                                    contentDescription = "Previous Month",
                                    tint = CalendarTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = currentMonthTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = CalendarTextPrimary,
                                modifier = Modifier.align(Alignment.Center)
                            )

                            IconButton(
                                onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 42.dp)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_left),
                                    contentDescription = "Next Month",
                                    tint = CalendarTextSecondary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(180f)
                                )
                            }

                            IconButton(
                                onClick = onDismissRequest,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.btn_close),
                                    contentDescription = "Close",
                                    tint = CalendarTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { weekday ->
                                Text(
                                    text = weekday,
                                    fontSize = 12.sp,
                                    color = Color(0xFFBBBFCA),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            daysInGrid.chunked(7).forEach { week ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    week.forEach { day ->
                                        if (!day.isCurrentMonth) {
                                            Spacer(modifier = Modifier.width(44.dp).height(36.dp))
                                        } else {
                                            val date = currentYearMonth.atDay(day.dayNumber)
                                            val isSelected = date == selectedDate
                                            val (bgColor, textColor) = when {
                                                isSelected -> CalendarTextPrimary to Color.White
                                                day.status == DayGlycemicStatus.NORM -> Color(0xFFDDF6F1) to CalendarTextPrimary
                                                day.status == DayGlycemicStatus.HIGH -> Color(0xFFFFF0D8) to CalendarTextPrimary
                                                day.status == DayGlycemicStatus.LOW -> Color(0xFFFDE1DC) to CalendarTextPrimary
                                                else -> Color.Transparent to CalendarTextPrimary
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .width(44.dp)
                                                    .height(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(bgColor)
                                                    .clickable {
                                                        selectedDate = date
                                                        val dateString = "${date.dayOfMonth} ${GENITIVE_MONTHS_RU[date.monthValue - 1]} ${date.year}"
                                                        onDateSelected(dateString)
                                                        onDismissRequest()
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = day.dayNumber.toString(),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
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
