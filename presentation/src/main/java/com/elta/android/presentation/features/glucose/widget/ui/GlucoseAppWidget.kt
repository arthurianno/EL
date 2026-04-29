package com.elta.android.presentation.features.glucose.widget.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.elta.android.domain.features.glucose.widget.model.GlucoseWidgetData
import com.elta.android.presentation.R
import com.elta.android.presentation.features.app.ui.AppActivity
import com.elta.android.presentation.features.glucose.widget.datasource.GlucoseWidgetPreferencesDataSource
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val WIDGET_SIZE_LOG_TAG = "GlucoseWidgetSize"


@SuppressLint("RestrictedApi")
open class GlucoseAppWidget(
    private val widgetSize: GlucoseWidgetSize = GlucoseWidgetSize.AUTO
) : GlanceAppWidget() {

    override val sizeMode: SizeMode =
        if (widgetSize == GlucoseWidgetSize.AUTO) {
            SizeMode.Responsive(
                setOf(
                    DpSize(200.dp, 200.dp),
                    DpSize(250.dp, 110.dp),
                    DpSize(250.dp, 250.dp)
                )
            )
        } else {
            SizeMode.Single
        }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = GlucoseWidgetPreferencesDataSource(context).loadWidgetData()
        provideContent {
            GlucoseWidgetRoot(data = data, widgetSize = widgetSize)
        }
    }
}

class GlucoseSmallAppWidget : GlucoseAppWidget(GlucoseWidgetSize.SMALL)
class GlucoseMediumAppWidget : GlucoseAppWidget(GlucoseWidgetSize.MEDIUM)
class GlucoseLargeAppWidget : GlucoseAppWidget(GlucoseWidgetSize.LARGE)

enum class GlucoseWidgetSize {
    SMALL,
    MEDIUM,
    LARGE,
    AUTO
}

@Composable
private fun GlucoseWidgetRoot(
    data: GlucoseWidgetData,
    widgetSize: GlucoseWidgetSize
) {
    val size = LocalSize.current

    val autoResolvedLayout = when {
        size.width >= 240.dp && size.height >= 220.dp -> GlucoseWidgetSize.LARGE
        size.width >= 220.dp && size.height >= 90.dp -> GlucoseWidgetSize.MEDIUM
        else -> GlucoseWidgetSize.SMALL
    }

    Log.d(
        WIDGET_SIZE_LOG_TAG,
        "root forced=$widgetSize localSize=${size.width.value}x${size.height.value} " +
                "autoResolved=$autoResolvedLayout glucose=${data.glucoseValue} " +
                "bread=${data.breadUnits} gDiff=${data.glucoseDiff} " +
                "bDiff=${data.breadDiff} sync=${data.syncStatus}"
    )

    GlucoseWidgetContainer(
        modifier = GlanceModifier.clickable(actionStartActivity<AppActivity>())
    ) {
        when (widgetSize) {
            GlucoseWidgetSize.SMALL -> {
                GlucoseSmallLayout(data = data, size = size)
            }

            GlucoseWidgetSize.MEDIUM -> {
                GlucoseMediumLayout(data = data)
            }

            GlucoseWidgetSize.LARGE -> {
                GlucoseLargeLayout(data = data)
            }

            GlucoseWidgetSize.AUTO -> {
                when (autoResolvedLayout) {
                    GlucoseWidgetSize.SMALL -> GlucoseSmallLayout(data = data, size = size)
                    GlucoseWidgetSize.MEDIUM -> GlucoseMediumLayout(data = data)
                    GlucoseWidgetSize.LARGE -> GlucoseLargeLayout(data = data)
                    GlucoseWidgetSize.AUTO -> GlucoseSmallLayout(data = data, size = size)
                }
            }
        }
    }
}


@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseLargeLayout(data: GlucoseWidgetData) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        GlucoseLargeHeader(
            status = data.syncStatus,
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(24.dp)
        )

        Spacer(GlanceModifier.height(8.dp))

        GlucoseLargeHeroCard(
            data = data,
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(68.dp)
        )

        Spacer(GlanceModifier.height(8.dp))

        GlucoseLargeChartCard(
            data = data,
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Spacer(GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(38.dp)
        ) {
            GlucoseLargeMiniChip(
                modifier = GlanceModifier.defaultWeight(),
                title = "ХЕ",
                value = floatValueText(data.breadUnits)
            )

            Spacer(GlanceModifier.width(6.dp))

            GlucoseLargeMiniChip(
                modifier = GlanceModifier.defaultWeight(),
                title = "Инсулин",
                value = floatValueText(data.insulinUnits)
            )

            Spacer(GlanceModifier.width(6.dp))

            GlucoseLargeMiniChip(
                modifier = GlanceModifier.defaultWeight(),
                title = "Напом.",
                value = reminderTimeText(data)
            )
        }
        Spacer(GlanceModifier.height(8.dp))
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(38.dp)
        ) {
            GlucoseLargeMiniChip(
                modifier = GlanceModifier.defaultWeight(),
                title = "ХЕ",
                value = floatValueText(data.breadUnits)
            )

            Spacer(GlanceModifier.width(6.dp))

            GlucoseLargeMiniChip(
                modifier = GlanceModifier.defaultWeight(),
                title = "Инсулин",
                value = floatValueText(data.insulinUnits)
            )

            Spacer(GlanceModifier.width(6.dp))

            GlucoseLargeMiniChip(
                modifier = GlanceModifier.defaultWeight(),
                title = "Напом.",
                value = reminderTimeText(data)
            )
        }
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseLargeHeader(
    status: GlucoseWidgetData.SyncStatus,
    modifier: GlanceModifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ЭЛТА",
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Text(
            text = status.smallLabel(),
            modifier = GlanceModifier
                .cornerRadius(100.dp)
                .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseLargeHeroCard(
    data: GlucoseWidgetData,
    modifier: GlanceModifier
) {
    Row(
        modifier = modifier
            .cornerRadius(26.dp)
            .background(ColorProvider(WidgetColors.CARD_FILL))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = glucoseValueText(data),
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = data.unit.symbol(),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            Spacer(GlanceModifier.height(4.dp))

            Text(
                text = glucoseTrendValue(data),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseLargeChartCard(
    data: GlucoseWidgetData,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier
            .cornerRadius(24.dp)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "График замеров",
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            Text(
                text = "последние",
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }

        Spacer(GlanceModifier.height(5.dp))

        Text(
            text = if (data.chartPoints.isEmpty()) {
                "Нет замеров"
            } else {
                sparkline(data.chartPoints)
            },
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = if (data.chartPoints.isEmpty()) 12.sp else 27.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            ),
            maxLines = 1
        )
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseLargeMiniChip(
    modifier: GlanceModifier,
    title: String,
    value: String
) {
    Column(
        modifier = modifier
            .cornerRadius(18.dp)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Spacer(GlanceModifier.height(1.dp))

        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )
    }
}





@Composable
private fun GlucoseWidgetContainer(
    modifier: GlanceModifier = GlanceModifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .then(modifier)
            .background(ImageProvider(R.drawable.bg_glucose_widget_gradient))
            .cornerRadius(18.dp)
    ) {
        content()
    }
}


@SuppressLint("RestrictedApi")
@Composable
private fun GlucoseSmallLayout(data: GlucoseWidgetData, size: DpSize) {
    val isTiny = size.width <= 140.dp || size.height <= 140.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(if (isTiny) 20.dp else 20.dp)
    ) {
        GlucoseWidgetHeader(
            title = "ЭЛТА",
            status = data.syncStatus,
            compact = true
        )

        Spacer(GlanceModifier.height(if (isTiny) 6.dp else 8.dp))

        GlucoseHeroValueCard(
            data = data,
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(if (isTiny) 58.dp else 82.dp),
            compact = isTiny
        )

        Spacer(GlanceModifier.height(if (isTiny) 6.dp else 8.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            GlucoseSmallInfoChip(
                modifier = GlanceModifier.defaultWeight().height(if (isTiny) 35.dp else 50.dp),
                title = "ХЕ",
                value = floatValueText(data.breadUnits),
                compact = isTiny
            )

            Spacer(GlanceModifier.width(if (isTiny) 6.dp else 8.dp))

            GlucoseSmallInfoChip(
                modifier = GlanceModifier.defaultWeight().height(if (isTiny) 35.dp else 50.dp),
                title = "Динамика",
                value = glucoseTrendValue(data),
                compact = isTiny
            )
        }
    }
}




@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseMediumLayout(data: GlucoseWidgetData) {
    val size = LocalSize.current
    val isTall = size.height >= 140.dp

    val outerPadding = if (isTall) 12.dp else 12.dp
    val contentHeight = if (isTall) 126.dp else 126.dp
    val heroWidth = if (isTall) 138.dp else 138.dp
    val topRowHeight = if (isTall) 56.dp else 56.dp
    val bottomRowHeight = if (isTall) 58.dp else 58.dp
    val spacing = if (isTall) 8.dp else 6.dp

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(outerPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlucoseMediumHeroCard(
            data = data,
            modifier = GlanceModifier
                .width(heroWidth)
                .height(contentHeight),
            tall = isTall
        )

        Spacer(GlanceModifier.width(spacing))

        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .height(contentHeight)
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(topRowHeight)
            ) {
                GlucoseMediumInfoChip(
                    modifier = GlanceModifier.defaultWeight(),
                    title = "ХЕ",
                    value = floatValueText(data.breadUnits),
                    tall = isTall
                )

                Spacer(GlanceModifier.width(6.dp))

                GlucoseMediumInfoChip(
                    modifier = GlanceModifier.defaultWeight(),
                    title = "Инс.",
                    value = floatValueText(data.insulinUnits),
                    tall = isTall
                )
            }

            Spacer(GlanceModifier.height(spacing))

            GlucoseMediumWideInfoChip(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(bottomRowHeight),
                title = "Напоминание",
                value = reminderTimeText(data),
                tall = isTall
            )
        }
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseMediumWideInfoChip(
    modifier: GlanceModifier,
    title: String,
    value: String,
    tall: Boolean
) {
    Row(
        modifier = modifier
            .cornerRadius(if (tall) 22.dp else 18.dp)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(
                horizontal = if (tall) 10.dp else 8.dp,
                vertical = if (tall) 8.dp else 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                fontSize = if (tall) 9.sp else 8.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = if (tall) 14.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseMediumInfoChip(
    modifier: GlanceModifier,
    title: String,
    value: String,
    tall: Boolean
) {
    Column(
        modifier = modifier
            .cornerRadius(if (tall) 22.dp else 18.dp)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(
                horizontal = if (tall) 7.dp else 5.dp,
                vertical = if (tall) 8.dp else 5.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                fontSize = if (tall) 9.sp else 8.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Spacer(GlanceModifier.height(if (tall) 3.dp else 2.dp))

        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = if (tall) 15.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )
    }
}


@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseMediumHeroCard(
    data: GlucoseWidgetData,
    modifier: GlanceModifier,
    tall: Boolean
) {
    Column(
        modifier = modifier

            .background(ColorProvider(WidgetColors.CARD_FILL))
            .padding(
                horizontal = if (tall) 14.dp else 12.dp,
                vertical = if (tall) 12.dp else 8.dp
            )
            .cornerRadius(if (tall) 28.dp else 24.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Глюкоза",
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                    fontSize = if (tall) 10.sp else 9.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            Text(
                text = data.syncStatus.smallLabel(),
                modifier = GlanceModifier
                    .cornerRadius(100.dp)
                    .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
                    .padding(
                        horizontal = if (tall) 8.dp else 7.dp,
                        vertical = if (tall) 4.dp else 3.dp
                    ),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                    fontSize = if (tall) 8.sp else 8.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }

        Spacer(GlanceModifier.height(if (tall) 14.dp else 6.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = glucoseValueText(data),
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                    fontSize = if (tall) 42.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 1
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = data.unit.symbol(),
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                        fontSize = if (tall) 10.sp else 9.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )

                Spacer(GlanceModifier.height(3.dp))

                Text(
                    text = glucoseTrendValue(data),
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                        fontSize = if (tall) 15.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    maxLines = 1
                )
            }
        }
    }
}



@SuppressLint("RestrictedApi")
@Composable
private fun GlucoseHeroValueCard(
    data: GlucoseWidgetData,
    modifier: GlanceModifier,
    compact: Boolean
) {
    Row(
        modifier = modifier
            .cornerRadius(if (compact) 22.dp else 28.dp)
            .background(ColorProvider(WidgetColors.CARD_FILL))
            .padding(
                horizontal = if (compact) 12.dp else 16.dp,
                vertical = if (compact) 8.dp else 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = glucoseValueText(data),
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = if (compact) 34.sp else 46.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = data.unit.symbol(),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                    fontSize = if (compact) 9.sp else 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            Spacer(GlanceModifier.height(4.dp))

            Text(
                text = glucoseTrendValue(data),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                    fontSize = if (compact) 13.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 1
            )
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun GlucoseSmallInfoChip(
    modifier: GlanceModifier,
    title: String,
    value: String,
    compact: Boolean
) {
    Column(
        modifier = modifier
            .cornerRadius(100.dp)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 4.dp else 7.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Spacer(GlanceModifier.height(1.dp))

        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = if (compact) 11.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun GlucoseStatusBadge(
    status: GlucoseWidgetData.SyncStatus,
    compact: Boolean
) {
    Text(
        text = status.smallLabel(),
        modifier = GlanceModifier
            .cornerRadius(100.dp)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(
                horizontal = if (compact) 7.dp else 10.dp,
                vertical = if (compact) 3.dp else 5.dp
            ),
        style = TextStyle(
            color = ColorProvider(WidgetColors.TEXT_PRIMARY),
            fontSize = if (compact) 8.sp else 10.sp,
            fontWeight = FontWeight.Medium
        ),
        maxLines = 1
    )
}

@SuppressLint("RestrictedApi")
@Composable
private fun GlucoseSparklineGlassCard(data: GlucoseWidgetData) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(58.dp)
            .cornerRadius(22.dp)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = "Динамика измерений",
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Spacer(GlanceModifier.height(5.dp))

        Text(
            text = if (data.chartPoints.isEmpty()) "Нет замеров" else sparkline(data.chartPoints),
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = if (data.chartPoints.isEmpty()) 12.sp else 24.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun GlucoseReminderGlassCard(data: GlucoseWidgetData) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(54.dp)
            .cornerRadius(22.dp)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "Напоминание",
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            Spacer(GlanceModifier.height(2.dp))

            Text(
                text = reminderDateText(data) ?: "Не задано",
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }

        Text(
            text = reminderTimeText(data),
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )
    }
}



@SuppressLint("RestrictedApi")
@Composable
private fun GlucoseWidgetHeader(
    title: String,
    status: GlucoseWidgetData.SyncStatus,
    compact: Boolean
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                fontSize = if (compact) 10.sp else 13.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        GlucoseStatusBadge(status = status, compact = compact)
    }
}


@SuppressLint("RestrictedApi")
@Composable
private fun GlucoseSyncStatusPill(
    status: GlucoseWidgetData.SyncStatus,
    compact: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    val horizontalPadding = if (compact) 8.dp else 12.dp
    val verticalPadding = if (compact) 5.dp else 10.dp

    Row(
        modifier = modifier
            .cornerRadius(100.dp)
            .background(ColorProvider(WidgetColors.CARD_FILL))
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .clickable(actionStartActivity<AppActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Text(
            text = status.label(isCompact = compact),
            modifier = GlanceModifier
                .defaultWeight()           // 👈 ВОТ ЭТО
                .fillMaxWidth(),           // 👈 И ЭТО
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = if (compact) 10.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            maxLines = 1
        )
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseMetricCard(
    modifier: GlanceModifier,
    title: String,
    value: String,
    valueSize: TextUnit,
    unit: String?,
    corner: Dp = 16.dp,
    cardFill: Color = WidgetColors.CARD_FILL,
    titleSize: TextUnit = 10.sp,
    unitSize: TextUnit = 10.sp,
    contentPaddingHorizontal: Dp = 10.dp,
    contentPaddingVertical: Dp = 8.dp,
    valueTopSpacing: Dp = 2.dp,
    unitTopSpacing: Dp = 1.dp
) {
    Column(
        modifier = modifier
            .cornerRadius(corner)
            .background(ColorProvider(cardFill))
            .padding(
                horizontal = contentPaddingHorizontal,
                vertical = contentPaddingVertical
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                fontSize = titleSize,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Spacer(GlanceModifier.height(valueTopSpacing))

        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = valueSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )

        if (!unit.isNullOrBlank()) {
            Spacer(GlanceModifier.height(unitTopSpacing))

            Text(
                text = unit,
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                    fontSize = unitSize,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseTrendChip(
    modifier: GlanceModifier,
    value: String,
    unit: String,
    compact: Boolean = false
) {
    val corner = if (compact) 100.dp else 100.dp
    val horizontalPadding = if (compact) 6.dp else 10.dp
    val verticalPadding = if (compact) 4.dp else 8.dp
    val valueSize = if (compact) 10.sp else 13.sp
    val unitSize = if (compact) 8.sp else 10.sp

    Row(
        modifier = modifier
            .cornerRadius(corner)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = valueSize,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )

        Spacer(GlanceModifier.width(if (compact) 3.dp else 4.dp))

        Text(
            text = unit,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                fontSize = unitSize
            ),
            maxLines = 1
        )
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun GlucoseReminderCard(data: GlucoseWidgetData) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(14.dp)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Напоминание",
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = 13.3.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Spacer(GlanceModifier.height(3.dp))

        Text(
            text = reminderTimeText(data),
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = 19.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 1
        )

        reminderDateText(data)?.let { date ->
            Spacer(GlanceModifier.height(2.dp))

            Text(
                text = date,
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                    fontSize = 8.5.sp
                ),
                maxLines = 1
            )
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun GlucoseChartCard(data: GlucoseWidgetData) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(16.dp)
            .background(ColorProvider(WidgetColors.CARD_SOFT_FILL))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = "График замеров",
            style = TextStyle(
                color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(GlanceModifier.height(6.dp))

        if (data.chartPoints.isEmpty()) {
            Text(
                text = "Нет замеров",
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_SECONDARY),
                    fontSize = 11.sp
                )
            )
        } else {
            Text(
                text = sparkline(data.chartPoints),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TEXT_PRIMARY),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 1
            )
        }
    }
}

private fun glucoseValueText(data: GlucoseWidgetData): String =
    if (!data.isAuthenticated) "—" else data.glucoseValue?.toString() ?: "--"

private fun glucoseTrendValue(data: GlucoseWidgetData): String =
    signedFloatValueText(data.glucoseDiff)
        .takeIf { it != "--" }
        ?: data.trend?.arrow()
        ?: "—"

private fun floatValueText(value: Float?): String =
    value?.let { String.format(Locale.US, "%.1f", it) } ?: "--"

private fun signedFloatValueText(value: Float?): String =
    value?.let {
        val abs = kotlin.math.abs(it)
        if (it >= 0f) {
            "+${String.format(Locale.US, "%.1f", abs)}"
        } else {
            "-${String.format(Locale.US, "%.1f", abs)}"
        }
    } ?: "--"

private fun reminderTimeText(data: GlucoseWidgetData): String =
    data.reminderTimeText
        ?: if (data.reminderActive) {
            data.reminderMessage ?: "--:--"
        } else {
            "--:--"
        }

private fun reminderDateText(data: GlucoseWidgetData): String? =
    data.reminderDateText
        ?: data.lastMeasurementTime
            ?.format(DateTimeFormatter.ofPattern("EEE, dd.MM", Locale("ru")))
            ?.replaceFirstChar { ch -> ch.titlecase(Locale("ru")) }

private fun sparkline(points: List<Float>): String {
    if (points.isEmpty()) return ""

    val ticks = "▁▂▃▄▅▆▇█"
    val visiblePoints = points.takeLast(20)
    val min = visiblePoints.minOrNull() ?: return ""
    val max = visiblePoints.maxOrNull() ?: return ""

    if (min == max) {
        return List(visiblePoints.size) { "▄" }.joinToString("")
    }

    return visiblePoints.joinToString("") { value ->
        val ratio = ((value - min) / (max - min)).coerceIn(0f, 1f)
        val index = (ratio * (ticks.length - 1)).toInt()
        ticks[index].toString()
    }
}

private object WidgetColors {
    val TEXT_PRIMARY = Color(0xF2FFFFFF)
    val TEXT_SECONDARY = Color(0xC7FFFFFF)
    val CARD_FILL = Color(0x29FFFFFF)
    val CARD_SOFT_FILL = Color(0x0FFFFFFF)
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 140, heightDp = 140)
@Composable
fun GlucoseWidgetPreviewSmall() {
    GlucoseWidgetContainer {
        GlucoseSmallLayout(
            data = previewData(),
            size = DpSize(140.dp, 140.dp)
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 150)
@Composable
fun GlucoseWidgetPreviewMedium() {
    GlucoseWidgetContainer {
        GlucoseMediumLayout(previewData())
    }
}



@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 250)
@Composable
fun GlucoseWidgetPreviewLarge() {
    GlucoseWidgetContainer {
        GlucoseLargeLayout(previewData())
    }
}

private fun previewData(): GlucoseWidgetData =
    GlucoseWidgetData(
        glucoseValue = 7,
        unit = GlucoseWidgetData.GlucoseUnit.MMOL_L,
        trend = GlucoseWidgetData.GlucoseTrend.UP,
        breadUnits = 3.4f,
        lastMeasurementTime = LocalDateTime.of(2026, 4, 27, 8, 45),
        syncStatus = GlucoseWidgetData.SyncStatus.SUCCESS,
        reminderActive = true,
        reminderMessage = "Проверить перед обедом",
        isAuthenticated = true,
        isOnline = true,
        glucoseDiff = 0.8f,
        breadDiff = -0.3f,
        insulinUnits = 6.0f,
        chartPoints = listOf(5.2f, 5.8f, 6.1f, 7.0f, 6.7f, 7.4f, 7.0f),
        reminderTimeText = "13:30",
        reminderDateText = "Пн, 27.04"
    )
