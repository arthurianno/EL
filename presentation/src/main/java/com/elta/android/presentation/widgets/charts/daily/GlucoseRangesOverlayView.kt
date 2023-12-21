package com.elta.android.presentation.widgets.charts.daily

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.elta.android.presentation.R
import com.nullgr.core.ui.extensions.dpToPx
import com.nullgr.core.ui.extensions.getDisplaySize

private const val BAR_WIDTH_DP = 4f // dp
private const val LEVEL_TEXT_END_PADDING_DP = 6f
private const val LEVEL_TEXT_BOTTOM_PADDING_DP = 10f
private const val LEVEL_TEXT_SIZE = 40f

class GlucoseRangesOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var highRangeColor = 0
    private var normalRangeColor = 0
    private var lowRangeColor = 0
    private var highLabelColor = 0
    private var normalLabelColor = 0
    private var lowLabelColor = 0

    private var rangeBarWidth = 0
    private var fullViewHeight = 0
    private var fullViewWidth = 0

    private val highRangePaint = Paint()
    private val normalRangePaint = Paint()
    private val lowRangePaint = Paint()
    private val glucoseLevelText = Paint()

    private val highRangeStartRect = Rect()
    private val normalRangeStartRect = Rect()
    private val lowRangeStartRect = Rect()

    private val highRangeEndRect = Rect()
    private val normalRangeEndRect = Rect()
    private val lowRangeEndRect = Rect()

    private var lowText: String = "0"
    private var highText: String = "0"

    init {
        initDefault()
        initPaints()
    }

    fun applyParentRanges(
        highRect: Rect,
        normalRect: Rect,
        lowRect: Rect,
        lowText: String,
        highText: String
    ) {
        this.lowText = lowText
        this.highText = highText
        highRangeStartRect.set(0, highRect.top, rangeBarWidth, highRect.bottom)
        highRangeEndRect.set(
            fullViewWidth - rangeBarWidth,
            highRect.top,
            fullViewWidth,
            highRect.bottom
        )

        normalRangeStartRect.set(0, normalRect.top, rangeBarWidth, normalRect.bottom)
        normalRangeEndRect.set(
            fullViewWidth - rangeBarWidth,
            normalRect.top,
            fullViewWidth,
            normalRect.bottom
        )

        lowRangeStartRect.set(0, lowRect.top, rangeBarWidth, lowRect.bottom)
        lowRangeEndRect.set(
            fullViewWidth - rangeBarWidth,
            lowRect.top,
            fullViewWidth,
            lowRect.bottom
        )
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        fullViewWidth = getDisplaySize(context).first
        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(fullViewWidth, MeasureSpec.EXACTLY)
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(fullViewHeight, MeasureSpec.EXACTLY)
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.run {
            drawRect(highRangeStartRect, highRangePaint)
            drawRect(highRangeEndRect, highRangePaint)
            drawRect(normalRangeStartRect, normalRangePaint)
            drawRect(normalRangeEndRect, normalRangePaint)
            drawRect(lowRangeStartRect, lowRangePaint)
            drawRect(lowRangeEndRect, lowRangePaint)
            drawLevelText(this, highText, highRangeEndRect, highLabelColor)
            drawLevelText(this, lowText, normalRangeEndRect, normalLabelColor)
            drawLevelText(this, "0", lowRangeEndRect, lowLabelColor)
        }
    }

    private fun drawLevelText(canvas: Canvas, text: String, rect: Rect, color: Int) {
        canvas.drawText(
            text,
            rect.left.toFloat() -
                LEVEL_TEXT_END_PADDING_DP.dpToPx(context) -
                glucoseLevelText.measureText(text),
            rect.bottom - LEVEL_TEXT_BOTTOM_PADDING_DP.dpToPx(context),
            glucoseLevelText.apply { setColor(color) }
        )
    }

    private fun initDefault() {
        lowRangeColor = ContextCompat.getColor(context, R.color.chart_low_range_bar_color)
        normalRangeColor = ContextCompat.getColor(context, R.color.chart_medium_range_bar_color)
        highRangeColor = ContextCompat.getColor(context, R.color.chart_high_range_bar_color)
        lowLabelColor = ContextCompat.getColor(context, R.color.chart_low_range_item_color)
        normalLabelColor = ContextCompat.getColor(context, R.color.chart_medium_range_item_color)
        highLabelColor = ContextCompat.getColor(context, R.color.chart_high_range_item_color)
        val chartOffset = GlucoseDailyChartView.CHART_OFFSET.dpToPx(context)
        val chartHeight = GlucoseDailyChartView.FULL_CHART_HEIGHT.dpToPx(context)
        fullViewHeight = (chartHeight + chartOffset * 2).toInt()
        rangeBarWidth = BAR_WIDTH_DP.dpToPx(context).toInt()
    }

    private fun initPaints() {
        lowRangePaint.apply {
            color = lowRangeColor
            style = Paint.Style.FILL
        }
        normalRangePaint.apply {
            color = normalRangeColor
            style = Paint.Style.FILL
        }
        highRangePaint.apply {
            color = highRangeColor
            style = Paint.Style.FILL
        }
        glucoseLevelText.apply {
            typeface = Typeface.DEFAULT_BOLD.apply {
                textSize = LEVEL_TEXT_SIZE
            }
        }
    }
}
