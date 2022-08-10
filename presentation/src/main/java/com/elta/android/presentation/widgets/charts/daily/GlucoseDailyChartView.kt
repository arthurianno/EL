@file:Suppress("TooManyFunctions", "MaxLineLength")

package com.elta.android.presentation.widgets.charts.daily

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.distanceBetween
import com.elta.android.presentation.widgets.charts.daily.models.ChartDataModel
import com.elta.android.presentation.widgets.charts.daily.models.ChartItemModel
import com.elta.android.presentation.widgets.charts.daily.models.ChartItemValueType
import com.nullgr.core.font.getTypeface
import com.nullgr.core.ui.extensions.dpToPx
import com.nullgr.core.ui.extensions.getDisplaySize
import com.nullgr.core.ui.extensions.spToPx
import org.threeten.bp.ZonedDateTime

@Suppress("LongMethod", "MagicNumber")
class GlucoseDailyChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var chartDataModel: ChartDataModel
        get() = dataModel()
        set(value) {
            _chartDataModel = value
            currentDateCalendar = ZonedDateTime.now()
            onDataModelChanged()
        }

    private var glucoseRangesOverlayView: GlucoseRangesOverlayView? = null

    private var currentDateCalendar = ZonedDateTime.now()
    private lateinit var minTitle: String
    private lateinit var maxTitle: String

    private var _chartDataModel: ChartDataModel? = null

    private var lowRangeBackgroundColor = 0
    private var normalRangeBackgroundColor = 0
    private var highRangeBackgroundColor = 0
    private var lowRangeDividerColor = 0
    private var normalRangeDividerColor = 0
    private var highRangeDividerColor = 0
    private var lowRangeItemColor = 0
    private var normalRangeItemColor = 0
    private var highRangeItemColor = 0
    private var lowRangeSelectedItemColor = 0
    private var normalRangeSelectedItemColor = 0
    private var highRangeSelectedItemColor = 0
    private var selectedItemInnerColor = 0
    private var futureTimeTextColor = 0
    private var timeTextColor = 0
    private var chartPointTitleColor = 0

    private var clearChartHeight = 0f
    private var fullViewHeight = 0f
    private var fullViewWidth = 0f
    private var sectionsDividerWidth = 0f
    private var chartOffset = 0f
    private var chartItemRadius = 0f
    private var chartPointTitleSize = 0f
    private var chartPointTitleBackgroundWidth = 0f
    private var chartPointTitleBackgroundHeight = 0f
    private var chartPointTitleBackgroundCorners = 0f
    private var timeTextSize = 0f

    private var selectedChartItemRadius = 0f
    private var selectedItemTimeBgWidth = 0f
    private var selectedItemTimeBgHeight = 0f
    private var selectedItemTimeBgPadding = 0f
    private var selectedItemTriangleWidth = 0f
    private var selectedItemTriangleHeight = 0f

    private var titlePadding = 0f
    private var singleHourWidth = 0f
    private var timeLineOffset = 0f
    private var timeTitleY = 0f
    private var titleHeight = 0f
    private var chartPointTitleHeight = 0f

    private val lowRangePaint = Paint()
    private val normalRangePaint = Paint()
    private val highRangePaint = Paint()
    private val sectionsDividerPaint = Paint()
    private val timeTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val chartItemPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedItemPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedItemTimeTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedItemTimeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedItemTrianglePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedItemLinePaint = Paint()
    private val chartPointTitlePaint = Paint()
    private val chartPointBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val lowRangeRect = Rect()
    private val normalRangeRect = Rect()
    private val highRangeRect = Rect()
    private val chartPointTitleBackgroundRect = RectF()
    private val selectedItemTimeBgRect = RectF()
    private val selectedItemTriangleTop = Path()
    private val selectedItemTriangleBottom = Path()
    private val selectedItemLinePath = Path()

    private val hoursCoordinatesMap = SparseArray<Float>()
    private val hoursTitlesMap = SparseArray<String>()
    private val chartPoints = hashMapOf<ChartItemModel, PointF>()
    private var scrollOffset = 0f

    init {
        initDefault()
        initPaints()
    }

    fun attachRangesOverlay(glucoseRangesOverlayView: GlucoseRangesOverlayView) {
        this.glucoseRangesOverlayView = glucoseRangesOverlayView
        if (_chartDataModel != null) {
            onDataModelChanged()
        }
    }

    fun getScrollPosition(): Float {
        val lastEventEntry = chartPoints.entries.maxByOrNull { it.value.x }
        if (lastEventEntry != null) {
            return lastEventEntry.value.x - scrollOffset
        }
        return 0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        onBeforeMeasure()
        val newWidthMeasureSpec =
            MeasureSpec.makeMeasureSpec(fullViewWidth.toInt(), MeasureSpec.EXACTLY)
        val newHeightMeasureSpec =
            MeasureSpec.makeMeasureSpec(fullViewHeight.toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
        onAfterMeasure()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val pointOfTouch = PointF(event.x, event.y)
            var clickedItem: ChartItemModel? = null
            chartPoints.entries.forEach {
                when {
                    it.key.isSelected -> it.key.isSelected = false
                    isPointClicked(it.value, pointOfTouch) -> clickedItem = it.key
                }
            }
            clickedItem?.isSelected = true
            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawSections()
            it.drawSectionDividers()
            it.drawTimeLine()
            it.drawPoints()
        }
    }

    private fun Canvas.drawPoints() {
        chartPoints.entries.forEach {
            if (!it.key.isSelected) {
                chartItemPaint.color = when {
                    it.key.valueType == ChartItemValueType.LOW -> lowRangeItemColor
                    it.key.valueType == ChartItemValueType.NORMAL -> normalRangeItemColor
                    it.key.valueType == ChartItemValueType.HIGH -> highRangeItemColor
                    else -> 0
                }
                drawCircle(it.value.x, it.value.y, chartItemRadius, chartItemPaint)

                if (it.key.isMaxValue && !it.key.isSelected) {
                    drawPointTitle(it.value, maxTitle, highRangeItemColor)
                }

                if (it.key.isMinValue && !it.key.isSelected) {
                    drawPointTitle(it.value, minTitle, lowRangeItemColor)
                }
            }
        }

        chartPoints.entries.find { it.key.isSelected }?.let {
            chartItemPaint.color = selectedItemInnerColor
            drawCircle(it.value.x, it.value.y, chartItemRadius, chartItemPaint)

            val selectionColor = when (it.key.valueType) {
                ChartItemValueType.LOW -> lowRangeSelectedItemColor
                ChartItemValueType.NORMAL -> normalRangeSelectedItemColor
                ChartItemValueType.HIGH -> highRangeSelectedItemColor
            }

            selectedItemPaint.color = selectionColor
            drawCircle(it.value.x, it.value.y, selectedChartItemRadius, selectedItemPaint)
            drawPointTitle(it.value, it.key.value.format(), selectionColor)
            drawSelected(it.value, selectionColor, it.key.formattedTime)
        }
    }

    private fun Canvas.drawPointTitle(pointF: PointF, text: String, backgroundColor: Int) {
        var bgStartX = pointF.x + chartItemRadius * 3
        val bgStartY = pointF.y - chartPointTitleBackgroundHeight / 2

        var bgEndX = bgStartX + chartPointTitleBackgroundWidth
        val bgEndY = bgStartY + chartPointTitleBackgroundHeight

        if (bgEndX > fullViewWidth) {
            bgEndX = pointF.x - chartItemRadius * 3
            bgStartX = bgEndX - chartPointTitleBackgroundWidth
        }

        chartPointTitleBackgroundRect.set(bgStartX, bgStartY, bgEndX, bgEndY)
        chartPointBackgroundPaint.color = backgroundColor
        drawRoundRect(
            chartPointTitleBackgroundRect,
            chartPointTitleBackgroundCorners,
            chartPointTitleBackgroundCorners,
            chartPointBackgroundPaint
        )

        val textX = chartPointTitleBackgroundRect.right - chartPointTitleBackgroundWidth / 2
        val textY =
            chartPointTitleBackgroundRect.bottom - chartPointTitleBackgroundHeight / 2 + chartPointTitleHeight / 4
        chartPointTitlePaint.color = chartPointTitleColor
        drawText(text, textX, textY, chartPointTitlePaint)
    }

    private fun Canvas.drawSelected(pointF: PointF, selectionColor: Int, time: String) {
        selectedItemLinePaint.color = selectionColor
        selectedItemLinePath.moveTo(pointF.x, 0f)
        selectedItemLinePath.lineTo(pointF.x, pointF.y - chartItemRadius)
        drawPath(selectedItemLinePath, selectedItemLinePaint)
        selectedItemLinePath.reset()

        selectedItemLinePath.moveTo(pointF.x, pointF.y + chartItemRadius)
        selectedItemLinePath.lineTo(pointF.x, timeTitleY)
        drawPath(selectedItemLinePath, selectedItemLinePaint)
        selectedItemLinePath.reset()

        val bgBottom = timeTitleY + selectedItemTimeBgPadding
        selectedItemTimeBgRect.set(
            pointF.x - selectedItemTimeBgWidth / 2,
            bgBottom - selectedItemTimeBgHeight,
            pointF.x + selectedItemTimeBgWidth / 2,
            bgBottom
        )
        selectedItemTimeBgPaint.color = selectionColor
        drawRoundRect(
            selectedItemTimeBgRect,
            chartPointTitleBackgroundCorners,
            chartPointTitleBackgroundCorners,
            selectedItemTimeBgPaint
        )
        drawText(time, pointF.x, timeTitleY, selectedItemTimeTitlePaint)

        selectedItemTrianglePaint.color = selectionColor

        selectedItemTriangleTop.moveTo(pointF.x - selectedItemTriangleWidth / 2, 0f)
        selectedItemTriangleTop.lineTo(pointF.x + selectedItemTriangleWidth / 2, 0f)
        selectedItemTriangleTop.lineTo(pointF.x, selectedItemTriangleHeight)
        selectedItemTriangleTop.lineTo(pointF.x - selectedItemTriangleWidth / 2, 0f)
        selectedItemTriangleTop.close()
        drawPath(selectedItemTriangleTop, selectedItemTrianglePaint)
        selectedItemTriangleTop.reset()

        selectedItemTriangleBottom.moveTo(
            pointF.x - selectedItemTriangleWidth / 2,
            selectedItemTimeBgRect.top
        )
        selectedItemTriangleBottom.lineTo(
            pointF.x + selectedItemTriangleWidth / 2,
            selectedItemTimeBgRect.top
        )
        selectedItemTriangleBottom.lineTo(
            pointF.x,
            selectedItemTimeBgRect.top - selectedItemTriangleHeight
        )
        selectedItemTriangleBottom.lineTo(
            pointF.x - selectedItemTriangleWidth / 2,
            selectedItemTimeBgRect.top
        )
        selectedItemTriangleBottom.close()
        drawPath(selectedItemTriangleBottom, selectedItemTrianglePaint)
        selectedItemTriangleBottom.reset()
    }

    private fun Canvas.drawSections() {
        drawRect(highRangeRect, highRangePaint)
        drawRect(normalRangeRect, normalRangePaint)
        drawRect(lowRangeRect, lowRangePaint)
    }

    private fun Canvas.drawSectionDividers() {
        val y1 = highRangeRect.top + sectionsDividerWidth / 2
        sectionsDividerPaint.color = highRangeDividerColor
        drawLine(0f, y1, fullViewWidth, y1, sectionsDividerPaint)

        val y2 = normalRangeRect.bottom - sectionsDividerWidth / 2
        val y3 = normalRangeRect.top + sectionsDividerWidth / 2
        sectionsDividerPaint.color = normalRangeDividerColor
        drawLine(0f, y2, fullViewWidth, y2, sectionsDividerPaint)
        drawLine(0f, y3, fullViewWidth, y3, sectionsDividerPaint)

        val y4 = lowRangeRect.bottom - sectionsDividerWidth / 2
        sectionsDividerPaint.color = lowRangeDividerColor
        drawLine(0f, y4, fullViewWidth, y4, sectionsDividerPaint)
    }

    private fun Canvas.drawTimeLine() {
        for (hour in START_HOUR..FULL_DAY_HOURS) {
            val text = hoursTitlesMap[hour]
            val x = hoursCoordinatesMap[hour]
            timeTitlePaint.color = when {
                hour <= currentDateCalendar.hour -> timeTextColor
                else -> futureTimeTextColor
            }
            drawText(text, x, timeTitleY, timeTitlePaint)
        }
    }

    private fun initDefault() {
        lowRangeBackgroundColor = getColor(R.color.chart_low_range_background_color)
        normalRangeBackgroundColor = getColor(R.color.chart_medium_range_background_color)
        highRangeBackgroundColor = getColor(R.color.chart_high_range_background_color)
        lowRangeDividerColor = getColor(R.color.chart_low_range_divider_color)
        normalRangeDividerColor = getColor(R.color.chart_medium_range_divider_color)
        highRangeDividerColor = getColor(R.color.chart_high_range_divider_color)

        lowRangeItemColor = getColor(R.color.chart_low_range_item_color)
        lowRangeSelectedItemColor = getColor(R.color.shade_blue3)
        normalRangeItemColor = getColor(R.color.chart_medium_range_item_color)
        normalRangeSelectedItemColor = getColor(R.color.shade_g_green2_a)
        highRangeItemColor = getColor(R.color.chart_high_range_item_color)
        highRangeSelectedItemColor = getColor(R.color.shade_red1)
        selectedItemInnerColor = getColor(R.color.white)

        futureTimeTextColor = getColor(R.color.chart_future_time_text_color)
        timeTextColor = getColor(R.color.chart_time_text_color)
        chartPointTitleColor = getColor(R.color.white)

        timeTextSize = TIME_TEXT_SIZE.spToPx(context)
        chartPointTitleSize = POINT_TITLE_TEXT_SIZE.spToPx(context)

        sectionsDividerWidth = SECTIONS_DIVIDER_WIDTH.dpToPx(context)
        chartOffset = CHART_OFFSET.dpToPx(context)
        clearChartHeight = FULL_CHART_HEIGHT.dpToPx(context)
        singleHourWidth = SINGLE_HOUR_WIDTH.dpToPx(context)
        timeLineOffset = TIME_LINE_OFFSET.dpToPx(context)
        chartItemRadius = ITEM_RADIUS.dpToPx(context)
        selectedChartItemRadius = SELECTED_ITEM_RADIUS.dpToPx(context)
        titlePadding = TITLE_PADDING.dpToPx(context)

        chartPointTitleBackgroundHeight = POINT_TITLE_BACKGROUND_HEIGHT.dpToPx(context)
        chartPointTitleBackgroundWidth = POINT_TITLE_BACKGROUND_WIDTH.dpToPx(context)
        chartPointTitleBackgroundCorners = POINT_TITLE_BACKGROUND_CORNERS.dpToPx(context)

        selectedItemTimeBgWidth = SELECTED_ITEM_TIME_BG_WIDTH.dpToPx(context)
        selectedItemTimeBgHeight = SELECTED_ITEM_TIME_BG_HEIGHT.dpToPx(context)
        selectedItemTimeBgPadding = SELECTED_ITEM_TIME_BG_PADDING.dpToPx(context)

        selectedItemTriangleWidth = SELECTED_ITEM_TRIANGLE_WIDTH.dpToPx(context)
        selectedItemTriangleHeight = SELECTED_ITEM_TRIANGLE_HEIGHT.dpToPx(context)

        minTitle = resources.getString(R.string.main_records_daily_chart_min_title)
        maxTitle = resources.getString(R.string.main_records_daily_chart_max_title)
    }

    private fun initPaints() {
        lowRangePaint.apply {
            color = lowRangeBackgroundColor
            style = Paint.Style.FILL
        }
        normalRangePaint.apply {
            color = normalRangeBackgroundColor
            style = Paint.Style.FILL
        }
        highRangePaint.apply {
            color = highRangeBackgroundColor
            style = Paint.Style.FILL
        }
        sectionsDividerPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = sectionsDividerWidth
        }
        timeTitlePaint.apply {
            textAlign = Paint.Align.CENTER
            textSize = timeTextSize
            color = timeTextColor
            typeface = context.getTypeface(TYPEFACE_MEDIUM)
        }
        chartPointTitlePaint.apply {
            textAlign = Paint.Align.CENTER
            textSize = chartPointTitleSize
            color = chartPointTitleColor
            typeface = context.getTypeface(TYPEFACE_BOLD)
        }
        chartPointBackgroundPaint.apply {
            style = Paint.Style.FILL
        }
        chartItemPaint.apply {
            style = Paint.Style.FILL
        }
        selectedItemPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = SELECTED_ITEM_STROKE_WIDTH.dpToPx(context)
        }
        selectedItemLinePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = SELECTED_ITEM_LINE_WIDTH.dpToPx(context)
            val gapLength = SELECTED_ITEM_GAP.dpToPx(context)
            pathEffect = DashPathEffect(floatArrayOf(gapLength, gapLength), 0f)
        }
        selectedItemTimeBgPaint.apply {
            style = Paint.Style.FILL
        }
        selectedItemTimeTitlePaint.apply {
            textAlign = Paint.Align.CENTER
            textSize = timeTextSize
            color = chartPointTitleColor
            typeface = context.getTypeface(TYPEFACE_MEDIUM)
        }
        selectedItemTrianglePaint.style = Paint.Style.FILL
    }

    private fun onBeforeMeasure() {
        val fontMetrics = timeTitlePaint.fontMetrics
        titleHeight = fontMetrics.descent - fontMetrics.ascent
        fullViewHeight =
            chartOffset * 2 + clearChartHeight + titlePadding + titleHeight + titlePadding
        fullViewWidth = singleHourWidth * FULL_DAY_HOURS + timeLineOffset * 2

        val fontMetricsChartPoint = chartPointTitlePaint.fontMetrics
        chartPointTitleHeight = fontMetricsChartPoint.descent - fontMetricsChartPoint.ascent

        hoursCoordinatesMap.put(START_HOUR, timeLineOffset)
        hoursTitlesMap.put(START_HOUR, 0.formatHour())
        for (hour in 1..FULL_DAY_HOURS) {
            val previousHoursX = hoursCoordinatesMap.get(hour - 1)
            hoursCoordinatesMap.put(hour, previousHoursX + singleHourWidth)
            hoursTitlesMap.put(hour, hour.formatHour())
        }
    }

    private fun onAfterMeasure() {
        if (_chartDataModel != null) {
            onDataModelChanged()
        }
        timeTitleY =
            chartOffset * 2 + clearChartHeight + titlePadding + (titleHeight * 0.75).toInt()
        scrollOffset = getDisplaySize(context).first * 0.8f
    }

    private fun onDataModelChanged() {
        if (ViewCompat.isAttachedToWindow(this)) {
            processRanges()
            processItems()
        }
        invalidate()
        requestLayout()
    }

    private fun processRanges() {
        with(dataModel().chartRangesModel) {
            val fullRange = end - start

            if (highMax != null) {
                val highRangePercents = (highMax - normalMax) / fullRange
                val highRangeHeight = clearChartHeight * highRangePercents
                highRangeRect.set(
                    0,
                    0,
                    fullViewWidth.toInt(),
                    chartOffset.toInt() + highRangeHeight.toInt()
                )
            } else {
                highRangeRect.set(0, 0, 0, 0)
            }

            val nonNullLowMax = lowMax ?: start
            val normalRangePercents = (normalMax - nonNullLowMax) / fullRange
            val normalRangeHeight = (clearChartHeight * normalRangePercents).toInt()

            val normalRangeBottom = when {
                needDrawHigh && needDrawLow -> highRangeRect.bottom + normalRangeHeight
                needDrawHigh && !needDrawLow -> highRangeRect.bottom + normalRangeHeight + chartOffset.toInt()
                !needDrawHigh && needDrawLow -> normalRangeHeight + chartOffset.toInt()
                else -> normalRangeHeight + chartOffset.toInt() * 2
            }
            normalRangeRect.set(0, highRangeRect.bottom, fullViewWidth.toInt(), normalRangeBottom)

            if (lowMax != null) {
                val lowRangePercents = (lowMax - start) / fullRange
                val lowRangeHeight = (clearChartHeight * lowRangePercents).toInt()
                lowRangeRect.set(
                    0,
                    normalRangeRect.bottom,
                    fullViewWidth.toInt(),
                    normalRangeRect.bottom + lowRangeHeight + chartOffset.toInt()
                )
            } else {
                lowRangeRect.set(0, 0, 0, 0)
            }

            glucoseRangesOverlayView?.applyParentRanges(
                highRangeRect,
                normalRangeRect,
                lowRangeRect
            )
        }
    }

    private fun processItems() {
        chartPoints.clear()
        dataModel().chartItems.forEach {
            chartPoints[it] = it.toPoint()
        }
    }

    private fun ChartItemModel.toPoint(): PointF {
        val startX = hoursCoordinatesMap[hourOfEvent]
        val x = startX + singleHourWidth * (minutesOfEvent.toFloat() / MINUTES_IN_HOUR)

        val valuesStart = dataModel().chartRangesModel.start
        val valuesEnd = dataModel().chartRangesModel.end
        val y =
            top + chartOffset.toInt() + clearChartHeight * (1 - (value - valuesStart) / (valuesEnd - valuesStart))
        return PointF(x, y.toFloat())
    }

    private fun Double.format() = NumberFormatter.numberFormat.format(this)

    private fun dataModel(): ChartDataModel =
        checkNotNull(_chartDataModel) { "Property `chartDataModel` did not initialized yet" }

    private fun getColor(color: Int) = ContextCompat.getColor(context, color)

    private fun isPointClicked(point1: PointF, point2: PointF) =
        point1 distanceBetween point2 <= chartItemRadius * 3

    private fun Int.formatHour(): String {
        val stringHour = this.toString()
        return resources.getString(
            when {
                stringHour.length == 1 -> R.string.main_records_daily_chart_time_mask
                else -> R.string.main_records_daily_chart_time_two_symbols_mask
            },
            stringHour
        )
    }

    companion object {
        const val FULL_CHART_HEIGHT = 144f
        const val CHART_OFFSET = 16f // dp

        private const val TIME_TEXT_SIZE = 12f // sp
        private const val SINGLE_HOUR_WIDTH = 54f // dp
        private const val TIME_LINE_OFFSET = 33f // dp
        private const val ITEM_RADIUS = 4f // dp
        private const val TITLE_PADDING = 16f // dp
        private const val TYPEFACE_MEDIUM = "roboto_medium.ttf"
        private const val TYPEFACE_BOLD = "roboto_bold.ttf"
        private const val START_HOUR = 0
        private const val FULL_DAY_HOURS = 24
        private const val MINUTES_IN_HOUR = 60
        private const val SELECTED_ITEM_RADIUS = 5f // dp
        private const val SELECTED_ITEM_STROKE_WIDTH = 2f // dp
        private const val SECTIONS_DIVIDER_WIDTH = 1f // dp
        private const val POINT_TITLE_TEXT_SIZE = 10f // sp
        private const val POINT_TITLE_BACKGROUND_HEIGHT = 16f // dp
        private const val POINT_TITLE_BACKGROUND_WIDTH = 32f // dp
        private const val POINT_TITLE_BACKGROUND_CORNERS = 4f // dp

        private const val SELECTED_ITEM_LINE_WIDTH = 1.5f // dp
        private const val SELECTED_ITEM_GAP = 4f // dp
        private const val SELECTED_ITEM_TIME_BG_WIDTH = 46f // dp
        private const val SELECTED_ITEM_TIME_BG_HEIGHT = 22f // dp
        private const val SELECTED_ITEM_TIME_BG_PADDING = 6f // dp
        private const val SELECTED_ITEM_TRIANGLE_WIDTH = 9f // dp
        private const val SELECTED_ITEM_TRIANGLE_HEIGHT = 7f // dp
    }
}
