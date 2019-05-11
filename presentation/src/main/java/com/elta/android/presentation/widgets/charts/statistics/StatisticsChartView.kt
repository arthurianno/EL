@file:Suppress("TooManyFunctions", "MaxLineLength")

package com.elta.android.presentation.widgets.charts.statistics

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.elta.android.domain.features.statistics.model.Periods
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.charts.statistics.listeners.OnStatisticsDateChangedListener
import com.elta.android.presentation.widgets.charts.statistics.models.DateModel
import com.elta.android.presentation.widgets.charts.statistics.models.SectionDataModel
import com.elta.android.presentation.widgets.charts.statistics.models.SectionModel
import com.elta.android.presentation.widgets.charts.statistics.models.StatisticsChartDataModel
import com.nullgr.core.font.getTypeface
import com.nullgr.core.ui.extensions.dpToPx
import com.nullgr.core.ui.extensions.getDisplaySize
import com.nullgr.core.ui.extensions.spToPx
import java.util.Date
import java.util.TreeMap

@Suppress("LongMethod", "MagicNumber", "NestedBlockDepth")
class StatisticsChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var chartDataModel: StatisticsChartDataModel
        get() = dataModel()
        set(value) {
            _chartDataModel = value
            onDataModelChanged()
        }

    private var _chartDataModel: StatisticsChartDataModel? = null
    private val dateToSectionsMap = TreeMap<DateModel, SectionModel>()
    private val sectionsData = mutableListOf<SectionDataModel>()
    private val pointsOfAverage = mutableListOf<PointF>()
    private var listener: OnStatisticsDateChangedListener? = null

    // COLORS
    private var sectionLineColor = 0
    private var dateTitleColor = 0
    private var selectedSectionColor = 0
    private var sectionColor = 0
    private var selectedSectionAttrsColor = 0
    private var selectedDateTitleColor = 0

    // SIZES
    private var clearChartHeight = 0f
    private var fullViewHeight = 0f
    private var fullViewWidth = 0f
    private var singleSectionWidth = 0f
    private var sectionsLineWidth = 0f
    private var chartOffset = 0f
    private var dateTitleTextSize = 0f
    private var dateTitlePadding = 0f
    private var dateTitleY = 0f
    private var titleHeight = 0f
    private var glucoseBlockCorner = 0f
    private var glucoseBlockMargin = 0f
    private var glucoseMinBlockHeight = 0f
    private var averageLineWidth = 0f
    private var averagePointRadius = 0f
    private var averagePointCircleWidth = 0f

    private var selectedItemTimeBgWidth = 0f
    private var selectedItemTimeBgHeight = 0f
    private var selectedItemTimeBgPadding = 0f
    private var selectedItemTriangleWidth = 0f
    private var selectedItemTriangleHeight = 0f
    private var selectedItemBackgroundCorner = 0f

    // PAINTS
    private val sectionPaint = Paint()
    private val sectionBottomLinePaint = Paint()
    private val sectionLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dateTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val selectedDateTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedItemLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedItemShapePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val glucosePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val averageLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val averagePointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val averagePointCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // DRAW OBJECTS
    private val sectionLinePath = Path()
    private val selectedItemTimeBgRect = RectF()
    private val selectedItemTriangleTop = Path()
    private val selectedItemTriangleBottom = Path()
    private val selectedItemLinePath = Path()
    private val averageLinePath = Path()

    init {
        initDefault()
        initPaints()
    }

    fun setOnStatisticsDateChangedListener(listener: OnStatisticsDateChangedListener?) {
        this.listener = listener
    }

    fun getScrollPosition() = fullViewWidth

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        onBeforeMeasure()
        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(fullViewWidth.toInt(), MeasureSpec.EXACTLY)
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(fullViewHeight.toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val pointOfTouch = PointF(event.x, event.y)
            dateToSectionsMap.entries.forEach {
                when {
                    it.value.isSelected -> it.value.performSelection(false, it.key.date)
                    it.value.isClicked(pointOfTouch) -> it.value.performSelection(true, it.key.date)
                }
            }
            checkMaybeAllUnselected()
            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawBottomLine()
            it.drawSections()
            it.drawSelected()
            it.drawGlucose()
            it.drawAverageLevel()
        }
    }

    private fun Canvas.drawGlucose() {
        sectionsData.forEach { model ->
            model.lowRect?.let {
                glucosePaint.shader = model.lowShader
                drawRoundRect(it, glucoseBlockCorner, glucoseBlockCorner, glucosePaint)
            }
            model.normalRect?.let {
                glucosePaint.shader = model.normalShader
                drawRoundRect(it, glucoseBlockCorner, glucoseBlockCorner, glucosePaint)
            }
            model.highRect?.let {
                glucosePaint.shader = model.highShader
                drawRoundRect(it, glucoseBlockCorner, glucoseBlockCorner, glucosePaint)
            }
        }
    }

    private fun Canvas.drawAverageLevel() {
        if (pointsOfAverage.size > 2) {
            averageLinePath.reset()
            averageLinePath.moveTo(pointsOfAverage[0].x, pointsOfAverage[0].y)
            for (i in 1 until pointsOfAverage.size) {
                averageLinePath.lineTo(pointsOfAverage[i].x, pointsOfAverage[i].y)
            }
            drawPath(averageLinePath, averageLinePaint)
        }
        pointsOfAverage.forEach {
            drawCircle(it.x, it.y, averagePointRadius, averagePointPaint)
            drawCircle(it.x, it.y, averagePointRadius, averagePointCirclePaint)
        }
    }

    private fun Canvas.drawBottomLine() {
        drawLine(0f, clearChartHeight, fullViewWidth, clearChartHeight, sectionBottomLinePaint)
    }

    private fun Canvas.drawSections() {
        dateToSectionsMap.entries.forEachIndexed { index, mutableEntry ->
            val dateModel = mutableEntry.key
            val sectionModel = mutableEntry.value

            sectionPaint.color = when (sectionModel.isSelected) {
                true -> selectedSectionColor
                else -> sectionColor
            }

            drawRect(sectionModel.sectionRect, sectionPaint)

            if (index < dateToSectionsMap.size - 1) {
                sectionLinePath.moveTo(sectionModel.sectionRect.right.toFloat(), sectionModel.sectionRect.top.toFloat())
                sectionLinePath.lineTo(sectionModel.sectionRect.right.toFloat(), sectionModel.sectionRect.bottom.toFloat())
                drawPath(sectionLinePath, sectionLinePaint)
                sectionLinePath.reset()
            }
            if (dateModel.needDrawDateTile && !dateModel.formattedDate.isNullOrEmpty()) {
                drawDateTitle(dateModel.formattedDate, sectionModel.sectionRect)
            }
        }
    }

    private fun Canvas.drawSelected() {
        dateToSectionsMap.entries.find { !it.key.formattedDate.isNullOrEmpty() && it.value.isSelected }?.let {
            drawSelectedSectionAttributes(checkNotNull(it.key.formattedDate), it.value.sectionRect)
        }
    }

    private fun Canvas.drawDateTitle(date: String, sectionRect: Rect) {
        val x = sectionRect.left + (sectionRect.right - sectionRect.left) / 2
        drawText(date, x.toFloat(), dateTitleY, dateTitlePaint)
    }

    private fun Canvas.drawSelectedSectionAttributes(date: String, sectionRect: Rect) {
        val x = sectionRect.left + (sectionRect.right - sectionRect.left) / 2

        selectedItemLinePath.moveTo(x.toFloat(), 0f)
        selectedItemLinePath.lineTo(x.toFloat(), dateTitleY)
        drawPath(selectedItemLinePath, selectedItemLinePaint)
        selectedItemLinePath.reset()

        val bgBottom = dateTitleY + selectedItemTimeBgPadding
        selectedItemTimeBgRect.set(
            x - selectedItemTimeBgWidth / 2,
            bgBottom - selectedItemTimeBgHeight,
            x + selectedItemTimeBgWidth / 2,
            bgBottom
        )

        drawRoundRect(selectedItemTimeBgRect, selectedItemBackgroundCorner, selectedItemBackgroundCorner, selectedItemShapePaint)
        drawText(date, x.toFloat(), dateTitleY, selectedDateTitlePaint)

        selectedItemTriangleTop.moveTo(x - selectedItemTriangleWidth / 2, 0f)
        selectedItemTriangleTop.lineTo(x + selectedItemTriangleWidth / 2, 0f)
        selectedItemTriangleTop.lineTo(x.toFloat(), selectedItemTriangleHeight)
        selectedItemTriangleTop.lineTo(x - selectedItemTriangleWidth / 2, 0f)
        selectedItemTriangleTop.close()
        drawPath(selectedItemTriangleTop, selectedItemShapePaint)
        selectedItemTriangleTop.reset()

        selectedItemTriangleBottom.moveTo(x - selectedItemTriangleWidth / 2, selectedItemTimeBgRect.top)
        selectedItemTriangleBottom.lineTo(x + selectedItemTriangleWidth / 2, selectedItemTimeBgRect.top)
        selectedItemTriangleBottom.lineTo(x.toFloat(), selectedItemTimeBgRect.top - selectedItemTriangleHeight)
        selectedItemTriangleBottom.lineTo(x - selectedItemTriangleWidth / 2, selectedItemTimeBgRect.top)
        selectedItemTriangleBottom.close()
        drawPath(selectedItemTriangleBottom, selectedItemShapePaint)
        selectedItemTriangleBottom.reset()
    }

    private fun initDefault() {
        sectionLineColor = getColor(R.color.shade_black3)
        dateTitleColor = getColor(R.color.shade_black2)
        selectedSectionColor = getColor(R.color.pale_gray)
        sectionColor = getColor(R.color.white)
        selectedSectionAttrsColor = getColor(R.color.black_blue)
        selectedDateTitleColor = getColor(R.color.white)

        fullViewHeight = FULL_VIEW_HEIGHT.dpToPx(context)
        clearChartHeight = FULL_CHART_HEIGHT.dpToPx(context)
        chartOffset = CHART_OFFSET.dpToPx(context)
        sectionsLineWidth = SECTIONS_LINE_WIDTH.dpToPx(context)
        dateTitlePadding = DATE_TITLE_PADDING.dpToPx(context)
        glucoseBlockCorner = GLUCOSE_BLOCK_CORNER.dpToPx(context)
        glucoseMinBlockHeight = GLUCOSE_MIN_BLOCK_HEIGHT.dpToPx(context)
        averageLineWidth = AVERAGE_LINE_WIDTH.dpToPx(context)
        averagePointCircleWidth = AVERAGE_POINT_LINE_WIDTH.dpToPx(context)
        averagePointRadius = AVERAGE_POINT_RADIUS.dpToPx(context)

        selectedItemTimeBgWidth = SELECTED_ITEM_TIME_BG_WIDTH.dpToPx(context)
        selectedItemTimeBgHeight = SELECTED_ITEM_TIME_BG_HEIGHT.dpToPx(context)
        selectedItemTimeBgPadding = SELECTED_ITEM_TIME_BG_PADDING.dpToPx(context)
        selectedItemTriangleWidth = SELECTED_ITEM_TRIANGLE_WIDTH.dpToPx(context)
        selectedItemTriangleHeight = SELECTED_ITEM_TRIANGLE_HEIGHT.dpToPx(context)
        selectedItemBackgroundCorner = SELECTED_ITEM_CORNER_RADIUS.dpToPx(context)

        dateTitleTextSize = DATE_TEXT_SIZE.spToPx(context)
    }

    private fun initPaints() {
        sectionPaint.apply { style = Paint.Style.FILL }
        sectionBottomLinePaint.apply {
            style = Paint.Style.STROKE
            color = sectionLineColor
            strokeWidth = sectionsLineWidth
        }
        sectionLinePaint.apply {
            style = Paint.Style.STROKE
            color = sectionLineColor
            strokeWidth = sectionsLineWidth
            val gapLength = SECTION_LINE_GAP.dpToPx(context)
            pathEffect = DashPathEffect(floatArrayOf(gapLength, gapLength), 0f)
        }
        dateTitlePaint.apply {
            textAlign = Paint.Align.CENTER
            textSize = dateTitleTextSize
            color = dateTitleColor
            typeface = context.getTypeface(TYPEFACE_BOLD)
        }
        selectedItemLinePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = SELECTED_ITEM_LINE_WIDTH.dpToPx(context)
            val gapLength = SELECTED_ITEM_GAP.dpToPx(context)
            color = selectedSectionAttrsColor
            pathEffect = DashPathEffect(floatArrayOf(gapLength, gapLength), 0f)
        }
        selectedDateTitlePaint.apply {
            textAlign = Paint.Align.CENTER
            textSize = dateTitleTextSize
            color = selectedDateTitleColor
            typeface = context.getTypeface(TYPEFACE_BOLD)
        }
        selectedItemShapePaint.apply {
            color = selectedSectionAttrsColor
            style = Paint.Style.FILL
        }
        glucosePaint.apply { style = Paint.Style.FILL }
        averageLinePaint.apply {
            style = Paint.Style.STROKE
            color = getColor(R.color.g_green_b)
            strokeWidth = averageLineWidth
        }
        averagePointPaint.apply {
            style = Paint.Style.FILL
            color = getColor(R.color.white)
        }
        averagePointCirclePaint.apply {
            style = Paint.Style.STROKE
            color = getColor(R.color.g_green_b)
            strokeWidth = averagePointCircleWidth
        }
    }

    private fun onBeforeMeasure() {
        if (_chartDataModel != null) {
            onDataModelChanged()
        }
        val fontMetrics = dateTitlePaint.fontMetrics
        titleHeight = fontMetrics.descent - fontMetrics.ascent
        fullViewWidth = singleSectionWidth * dateToSectionsMap.size
        dateTitleY = clearChartHeight + dateTitlePadding + (titleHeight * 0.75).toInt()
    }

    private fun onDataModelChanged() {
        if (ViewCompat.isAttachedToWindow(this)) {
            processSections()
            processItems()
            invalidate()
        }
    }

    private fun processSections() {
        dateToSectionsMap.clear()
        val availableScreenWidth = getDisplaySize(context).first - CHART_VALUES_WIDTH.dpToPx(context)
        singleSectionWidth = when {
            dataModel().period is Periods.SevenDays -> availableScreenWidth / 8
            else -> availableScreenWidth / 14
        }
        glucoseBlockMargin = (singleSectionWidth * 0.3).toFloat()
        dataModel().statisticsPerDate.entries.forEachIndexed { index, entry ->
            val left = (singleSectionWidth * index).toInt()
            val right = (singleSectionWidth * (index + 1)).toInt()
            val sectionRect = Rect(left, 0, right, clearChartHeight.toInt())
            dateToSectionsMap[entry.key] = SectionModel(
                sectionRect = sectionRect,
                isSelected = !entry.key.isStub && entry.key.date == dataModel().selectedDate,
                isStub = entry.key.isStub
            )
        }
    }

    private fun processItems() {
        sectionsData.clear()
        pointsOfAverage.clear()

        val max = dataModel().maxValue
        val min = dataModel().minValue
        val fullRange = max - min
        val availableChartHeight = clearChartHeight - 2 * chartOffset

        dataModel().statisticsPerDate.entries.forEach { entry ->
            entry.value?.let { model ->

                val originSectionRect = checkNotNull(dateToSectionsMap[entry.key]).sectionRect
                val left = originSectionRect.left + glucoseBlockMargin
                val right = originSectionRect.right - glucoseBlockMargin
                val originTop = originSectionRect.top + chartOffset

                var lowRect: RectF? = null
                var normalRect: RectF? = null
                var highRect: RectF? = null

                if (model.eventsLowCount > 0) {
                    val minLow = model.minLowLevel ?: 0.0
                    val lowBottom = originTop + availableChartHeight * (1 - (minLow - min) / fullRange)
                    val maxLow = model.maxLowLevel ?: 0.0
                    var lowTop = originTop + availableChartHeight * (1 - (maxLow - min) / fullRange)

                    if (lowBottom - lowTop < glucoseMinBlockHeight) {
                        lowTop = lowBottom - glucoseMinBlockHeight
                    }
                    lowRect = RectF(left, lowTop.toFloat(), right, lowBottom.toFloat())
                }

                if (model.eventsNormalCount > 0) {
                    val minNormal = model.minNormalLevel ?: 0.0
                    val normalBottom = originTop + availableChartHeight * (1 - (minNormal - min) / fullRange)
                    val maxNormal = model.maxNormalLevel ?: 0.0
                    var normalTop = originTop + availableChartHeight * (1 - (maxNormal - min) / fullRange)

                    if (normalBottom - normalTop < glucoseMinBlockHeight) {
                        normalTop = normalBottom - glucoseMinBlockHeight
                    }
                    normalRect = RectF(left, normalTop.toFloat(), right, normalBottom.toFloat())
                }

                if (model.eventsHighCount > 0) {
                    val minHigh = model.minHighLevel ?: 0.0
                    val highBottom = originTop + availableChartHeight * (1 - (minHigh - min) / fullRange)
                    val maxHigh = model.maxHighLevel ?: 0.0
                    var highTop = originTop + availableChartHeight * (1 - (maxHigh - min) / fullRange)

                    if (highBottom - highTop < glucoseMinBlockHeight) {
                        highTop = highBottom - glucoseMinBlockHeight
                    }
                    highRect = RectF(left, highTop.toFloat(), right, highBottom.toFloat())
                }

                if (model.averageLevel > 0) {
                    val pointX = originSectionRect.left + originSectionRect.width() / 2
                    val pointY = originTop + availableChartHeight * (1 - (model.averageLevel - min) / fullRange)
                    pointsOfAverage.add(PointF(pointX.toFloat(), pointY.toFloat()))
                }

                if (lowRect != null || normalRect != null || highRect != null) {
                    sectionsData.add(
                        SectionDataModel(
                            lowRect = lowRect,
                            normalRect = normalRect,
                            highRect = highRect,
                            lowShader = lowRect?.lowLevelShader(),
                            normalShader = normalRect?.normalLevelShader(),
                            highShader = highRect?.highLevelShader()
                        )
                    )
                }
            }
        }
    }

    private fun SectionModel.performSelection(state: Boolean, date: Date?) {
        isSelected = state
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (isSelected)
            date?.let { listener?.onDateChanged(date) }
    }

    private fun checkMaybeAllUnselected() {
        if (dateToSectionsMap.all { !it.value.isSelected }) {
            listener?.onUnselectedAll()
        }
    }

    private fun SectionModel.isClicked(pointF: PointF) =
        pointF.x.toInt() in sectionRect.left..sectionRect.right &&
            pointF.y.toInt() < sectionRect.bottom &&
            !isStub

    private fun dataModel(): StatisticsChartDataModel =
        checkNotNull(_chartDataModel) { "Property `chartDataModel` did not initialized yet" }

    private fun getColor(color: Int) = ContextCompat.getColor(context, color)

    private fun RectF.lowLevelShader() =
        makeShader(this, getColor(R.color.g_purpur_a), getColor(R.color.g_purpur_b))

    private fun RectF.normalLevelShader() =
        makeShader(this, getColor(R.color.g_green_a), getColor(R.color.g_green_b))

    private fun RectF.highLevelShader() =
        makeShader(this, getColor(R.color.g_orange_a), getColor(R.color.g_orange_b))

    private fun makeShader(originRect: RectF, colorStart: Int, colorEnd: Int) =
        LinearGradient(
            originRect.left,
            originRect.top,
            originRect.left,
            originRect.bottom,
            colorStart,
            colorEnd,
            Shader.TileMode.CLAMP
        )

    companion object {
        private const val FULL_CHART_HEIGHT = 194f
        private const val FULL_VIEW_HEIGHT = 230f
        private const val CHART_OFFSET = 8f // dp

        private const val CHART_VALUES_WIDTH = 45f // dp
        private const val SECTION_LINE_GAP = 2f // dp
        private const val SECTIONS_LINE_WIDTH = 1.3f // dp
        private const val DATE_TITLE_PADDING = 12f // dp
        private const val DATE_TEXT_SIZE = 10f // sp
        private const val AVERAGE_LINE_WIDTH = 1f // dp
        private const val AVERAGE_POINT_RADIUS = 3f // dp
        private const val AVERAGE_POINT_LINE_WIDTH = 1f // dp

        private const val SELECTED_ITEM_LINE_WIDTH = 1f // dp
        private const val SELECTED_ITEM_GAP = 4f // dp
        private const val SELECTED_ITEM_TIME_BG_WIDTH = 46f // dp
        private const val SELECTED_ITEM_TIME_BG_HEIGHT = 22f // dp
        private const val SELECTED_ITEM_TIME_BG_PADDING = 8f // dp
        private const val SELECTED_ITEM_TRIANGLE_WIDTH = 9f // dp
        private const val SELECTED_ITEM_TRIANGLE_HEIGHT = 7f // dp
        private const val SELECTED_ITEM_CORNER_RADIUS = 4f // dp

        private const val GLUCOSE_BLOCK_CORNER = 2f // dp
        private const val GLUCOSE_MIN_BLOCK_HEIGHT = 3f // dp

        private const val TYPEFACE_BOLD = "roboto_bold.ttf"
    }
}