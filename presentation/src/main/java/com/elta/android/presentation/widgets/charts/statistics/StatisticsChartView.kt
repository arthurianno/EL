package com.elta.android.presentation.widgets.charts.statistics

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
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
import com.elta.android.presentation.widgets.charts.statistics.models.DateModel
import com.elta.android.presentation.widgets.charts.statistics.models.SectionDataModel
import com.elta.android.presentation.widgets.charts.statistics.models.SectionModel
import com.elta.android.presentation.widgets.charts.statistics.models.StatisticsChartDataModel
import com.nullgr.core.font.getTypeface
import com.nullgr.core.ui.extensions.dpToPx
import com.nullgr.core.ui.extensions.getDisplaySize
import com.nullgr.core.ui.extensions.spToPx
import java.util.TreeMap

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
    private val pointsOfAverage = mutableListOf<Point>()

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

    // DRAW OBJECTS
    private val sectionLinePath = Path()
    private val selectedItemTimeBgRect = RectF()
    private val selectedItemTriangleTop = Path()
    private val selectedItemTriangleBottom = Path()
    private val selectedItemLinePath = Path()

    init {
        initDefault()
        initPaints()
    }

    fun getScrollPosition() = fullViewWidth

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        onBeforeMeasure()
        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(fullViewWidth.toInt(), MeasureSpec.EXACTLY)
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(fullViewHeight.toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
        onAfterMeasure()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val pointOfTouch = PointF(event.x, event.y)
            dateToSectionsMap.values.forEach {
                when {
                    it.isSelected -> it.performSelection(false)
                    it.isClicked(pointOfTouch) -> it.performSelection(true)
                }
            }
            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawBottomLine()
            it.drawSections()
            it.drawGlucose()
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
            if (sectionModel.isSelected && !dateModel.formattedDate.isNullOrEmpty()) {
                drawSelectedSectionAttributes(dateModel.formattedDate, sectionModel.sectionRect)
            }
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
        glucoseBlockMargin = GLUCOSE_BLOCK_MARGIN.dpToPx(context)

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
            strokeWidth = SECTIONS_LINE_WIDTH.dpToPx(context)
        }
        sectionLinePaint.apply {
            style = Paint.Style.STROKE
            color = sectionLineColor
            strokeWidth = SECTIONS_LINE_WIDTH.dpToPx(context)
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

    private fun onAfterMeasure() {

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
        singleSectionWidth = when {
            dataModel().period is Periods.SevenDays ->
                (getDisplaySize(context).first - CHART_VALUES_WIDTH.dpToPx(context)) / 8
            else -> SINGLE_SECTION_OTHER_WIDTH.dpToPx(context)
        }
        dataModel().statisticsPerDate.entries.forEachIndexed { index, entry ->
            val left = (singleSectionWidth * index).toInt()
            val right = (singleSectionWidth * (index + 1)).toInt()
            val sectionRect = Rect(left, 0, right, clearChartHeight.toInt())
            dateToSectionsMap[entry.key] = SectionModel(sectionRect, isStub = entry.key.isStub)
        }
    }

    private fun processItems() {
        sectionsData.clear()
        pointsOfAverage.clear()

        val max = dataModel().maxValue
        val min = dataModel().minValue

        dataModel().statisticsPerDate.entries.forEach { entry ->
            entry.value?.let { model ->

                val originSectionRect = checkNotNull(dateToSectionsMap[entry.key]).sectionRect
                val left = originSectionRect.left + glucoseBlockMargin
                val right = originSectionRect.right - glucoseBlockMargin

                var lowRect: RectF? = null
                var normalRect: RectF? = null
                var highRect: RectF? = null

                if (model.eventsLowCount > 0) {
                    // TODO create low rect
                }
                if (model.eventsNormalCount > 0) {
                    // TODO create normal  rect
                }
                if (model.eventsHighCount > 0) {
                    // TODO create high rect
                }

                if (model.averageLevel > 0) {

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

    private fun SectionModel.performSelection(state: Boolean) {
        isSelected = state
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private fun SectionModel.isClicked(pointF: PointF) =
        pointF.x.toInt() in sectionRect.left..sectionRect.right
            && pointF.y.toInt() < sectionRect.bottom
            && !isStub

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
        private const val CHART_OFFSET = 16f // dp

        private const val CHART_VALUES_WIDTH = 45f // dp
        private const val SINGLE_SECTION_OTHER_WIDTH = 34f // dp
        private const val SECTION_LINE_GAP = 2f // dp
        private const val SECTIONS_LINE_WIDTH = 1f // dp
        private const val DATE_TITLE_PADDING = 12f // dp
        private const val DATE_TEXT_SIZE = 10f // sp

        private const val SELECTED_ITEM_LINE_WIDTH = 1f // dp
        private const val SELECTED_ITEM_GAP = 4f // dp
        private const val SELECTED_ITEM_TIME_BG_WIDTH = 46f // dp
        private const val SELECTED_ITEM_TIME_BG_HEIGHT = 22f // dp
        private const val SELECTED_ITEM_TIME_BG_PADDING = 8f // dp
        private const val SELECTED_ITEM_TRIANGLE_WIDTH = 9f // dp
        private const val SELECTED_ITEM_TRIANGLE_HEIGHT = 7f // dp
        private const val SELECTED_ITEM_CORNER_RADIUS = 4f // dp

        private const val GLUCOSE_BLOCK_CORNER = 2f // dp
        private const val GLUCOSE_BLOCK_MARGIN = 12f // dp

        private const val TYPEFACE_BOLD = "roboto_bold.ttf"
    }
}