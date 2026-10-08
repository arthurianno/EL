package com.elta.android.presentation.features.statistic.period.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elta.android.presentation.R
import com.elta.android.presentation.features.statistic.period.model.StatisticsBlock
import kotlin.math.roundToInt

@Composable
internal fun StatisticsSettingsDialog(
    visibleBlocks: List<StatisticsBlock>,
    onDismiss: () -> Unit,
    onSave: (List<StatisticsBlock>) -> Unit
) {
    var selectedBlocks by remember(visibleBlocks) { mutableStateOf(visibleBlocks) }
    val hiddenBlocks = StatisticsBlock.entries.filterNot { it in selectedBlocks }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f))) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 380.dp)
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Настройка статистики",
                            modifier = Modifier.weight(1f),
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_statistics_settings_close),
                            contentDescription = "Закрыть настройки",
                            modifier = Modifier.size(24.dp).clickable(onClick = onDismiss)
                        )
                    }
                    Text(
                        text = "Отображаемые блоки",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 14.dp, bottom = 7.dp)
                    )
                    selectedBlocks.forEach { block ->
                        key(block) {
                            StatisticsSettingsRow(
                                block = block,
                                isVisible = true,
                                canReorder = true,
                                onToggleVisibility = { selectedBlocks = selectedBlocks - block },
                                onMove = { direction ->
                                    val moved = selectedBlocks.move(block, direction)
                                    val didMove = moved !== selectedBlocks
                                    selectedBlocks = moved
                                    didMove
                                }
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 14.dp)
                            .height(1.dp)
                            .background(Divider)
                    )
                    Text(
                        text = "Скрытые блоки",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 7.dp)
                    )
                    hiddenBlocks.forEach { block ->
                        key(block) {
                            StatisticsSettingsRow(
                                block = block,
                                isVisible = false,
                                canReorder = false,
                                onToggleVisibility = { selectedBlocks = selectedBlocks + block },
                                onMove = { false }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ⓘ", color = TextSecondary, fontSize = 20.sp)
                    Text(
                        text = "Выберите, какие блоки отображать в отчёте\nи их порядок (зажмите и перетащите)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
                Text(
                    text = "Сохранить изменения",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 8.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GreenDark)
                        .clickable { onSave(selectedBlocks) }
                        .padding(top = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun StatisticsSettingsRow(
    block: StatisticsBlock,
    isVisible: Boolean,
    canReorder: Boolean,
    onToggleVisibility: () -> Unit,
    onMove: (Int) -> Boolean
) {
    var accumulatedDrag by remember(block, isVisible) { mutableStateOf(0f) }
    var isDragging by remember(block, isVisible) { mutableStateOf(false) }
    val rowHeightPx = with(LocalDensity.current) { 48.dp.toPx() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .zIndex(if (isDragging) 1f else 0f)
            .offset { IntOffset(0, if (isDragging) accumulatedDrag.roundToInt() else 0) }
            .shadow(if (isDragging) 8.dp else 0.dp, RoundedCornerShape(8.dp))
            .background(if (isDragging) Color(0xFFE9F7F3) else Color.Transparent)
            .padding(horizontal = if (isDragging) 6.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatisticsBlockIcon(block = block, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(block.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(block.subtitle, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Image(
            painter = painterResource(
                if (isVisible) R.drawable.ic_statistics_settings_eye else R.drawable.ic_statistics_settings_eye_off
            ),
            contentDescription = if (isVisible) "Скрыть ${block.title}" else "Показать ${block.title}",
            modifier = Modifier.size(24.dp).clickable(onClick = onToggleVisibility)
        )
        if (canReorder) {
            StatisticsDragHandle(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
                    .pointerInput(block) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                accumulatedDrag = 0f
                                isDragging = true
                            },
                            onDragEnd = {
                                accumulatedDrag = 0f
                                isDragging = false
                            },
                            onDragCancel = {
                                accumulatedDrag = 0f
                                isDragging = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDrag += dragAmount.y
                                when {
                                    accumulatedDrag <= -rowHeightPx / 2f -> {
                                        accumulatedDrag = if (onMove(-1)) {
                                            accumulatedDrag + rowHeightPx
                                        } else 0f
                                    }

                                    accumulatedDrag >= rowHeightPx / 2f -> {
                                        accumulatedDrag = if (onMove(1)) {
                                            accumulatedDrag - rowHeightPx
                                        } else 0f
                                    }
                                }
                            }
                        )
                    }
            )
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
private fun StatisticsBlockIcon(block: StatisticsBlock, modifier: Modifier = Modifier) {
    when (block) {
        StatisticsBlock.KEY_METRICS -> Image(
            painter = painterResource(R.drawable.ic_statistics_settings_metrics),
            contentDescription = null,
            modifier = modifier
        )

        StatisticsBlock.COMPARISON -> Image(
            painter = painterResource(R.drawable.ic_statistics_settings_compare),
            contentDescription = null,
            modifier = modifier
        )

        StatisticsBlock.ACTIVITY -> Image(
            painter = painterResource(R.drawable.ic_statistics_settings_activity),
            contentDescription = null,
            modifier = modifier
        )

        StatisticsBlock.FOOD -> Image(
            painter = painterResource(R.drawable.ic_statistics_settings_food),
            contentDescription = null,
            modifier = modifier
        )

        StatisticsBlock.PERIOD -> Canvas(modifier = modifier) {
            val barWidth = size.width / 5f
            drawRect(Red, Offset(barWidth * .5f, size.height * .56f), Size(barWidth, size.height * .28f))
            drawRect(Green, Offset(barWidth * 2f, size.height * .22f), Size(barWidth, size.height * .62f))
            drawRect(Orange, Offset(barWidth * 3.5f, size.height * .42f), Size(barWidth, size.height * .42f))
        }

        StatisticsBlock.DAILY -> Canvas(modifier = modifier) {
            val lineHeight = 3.dp.toPx()
            listOf(Green, Orange, Red).forEachIndexed { index, color ->
                drawRoundRect(
                    color = color,
                    topLeft = Offset(2.dp.toPx(), (5 + index * 6).dp.toPx()),
                    size = Size(size.width - 4.dp.toPx(), lineHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(lineHeight / 2)
                )
            }
        }
    }
}

@Composable
private fun StatisticsDragHandle(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 1.5.dp.toPx()
        listOf(7.dp, 12.dp, 17.dp).forEach { y ->
            drawLine(
                color = TextSecondary,
                start = Offset(3.dp.toPx(), y.toPx()),
                end = Offset(size.width - 3.dp.toPx(), y.toPx()),
                strokeWidth = stroke
            )
        }
    }
}

private fun List<StatisticsBlock>.move(block: StatisticsBlock, direction: Int): List<StatisticsBlock> {
    val currentIndex = indexOf(block)
    if (currentIndex == -1) return this
    val targetIndex = (currentIndex + direction).coerceIn(indices)
    if (targetIndex == currentIndex) return this
    return toMutableList().apply {
        removeAt(currentIndex)
        add(targetIndex, block)
    }
}
