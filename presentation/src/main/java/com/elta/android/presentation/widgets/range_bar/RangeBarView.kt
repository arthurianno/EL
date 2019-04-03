package com.elta.android.presentation.widgets.range_bar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.range_bar.listeners.OnRageBarValuesChangeListener
import com.elta.android.presentation.widgets.range_bar.listeners.RangeValuesChangedObserver
import com.nullgr.core.ui.extensions.dpToPx
import io.reactivex.Observable
import java.util.function.Consumer

class RangeBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val startValue: Double
        get() = values.start

    val endValue: Double
        get() = values.end

    private var valuesRange: ClosedFloatingPointRange<Double> = DEFAULT_START_VALUE..DEFAULT_END_VALUE
    private var values: Values = Values(DEFAULT_START_VALUE, DEFAULT_END_VALUE)
    private val listeners = arrayListOf<OnRageBarValuesChangeListener>()
    private var resultFraction: Int = FRACTION_DIGITS_COUNT

    @ColorRes
    private val defaultBackgroundColorRes = R.color.pale_gray
    @ColorRes
    private var defaultRangeBarColorRes = R.color.shade_g_green_a
    @ColorRes
    private var defaultIndicatorsColorRes = R.color.white
    @ColorInt
    private var viewBackgroundColor = 0
    @ColorInt
    private var rangeBarColor = 0
    @ColorInt
    private var indicatorsColor = 0

    private var rangeBarHeight = 0f
    private var rangeBarWidth = 0f
    private var cornerRadius = 0f
    private var indicatorsWidth = 0f
    private var indicatorsHeight = 0f
    private var indicatorsShift = 0f
    private var touchBounds = 0f

    private lateinit var backgroundPaint: Paint
    private lateinit var rangeBarPaint: Paint
    private lateinit var indicatorsPaint: Paint

    private val mainRect = RectF()
    private val rangeBarRect = RectF()
    private var indicatorsStartY = 0f

    private var startProgress = 0f
    private var endProgress = 1f

    private var movementState = MovementState.IDLE

    init {
        initAttributes(attrs)
        initPaints()
    }

    fun updateValuesRange(range: ClosedFloatingPointRange<Double>) {
        valuesRange = range
        setValues(range.start, range.endInclusive)
    }

    fun setValues(start: Double, end: Double) {
        if (start !in valuesRange || end !in valuesRange)
            throw IllegalArgumentException("Values must be in : $valuesRange")
        values = Values(start.normalize(), end.normalize())
        onValuesChangedOutside()
    }

    fun addOnValuesChangeListener(listener: OnRageBarValuesChangeListener) {
        listeners.add(listener)
    }

    fun removeOnValuesChangeListener(listener: OnRageBarValuesChangeListener) {
        listeners.remove(listener)
    }

    fun values() = Consumer<Pair<Double, Double>> {
        setValues(it.first, it.second)
    }

    fun valuesChanges(): Observable<Pair<Double, Double>> = RangeValuesChangedObserver(this)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        rangeBarWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        //todo calc text and dots
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(rangeBarHeight.toInt(), MeasureSpec.EXACTLY)
        setMeasuredDimension(widthMeasureSpec, newHeightMeasureSpec)
        prepare()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                if (isInLeftIndicatorBounds(x, y)) {
                    movementState = MovementState.DRAG_LEFT_EDGE
                    disableParentTouch()
                }
                if (isInRightIndicatorBounds(x, y)) {
                    movementState = MovementState.DRAG_RIGHT_EDGE
                    disableParentTouch()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (movementState != MovementState.IDLE) {
                    val touchX = event.x
                    if (movementState == MovementState.DRAG_LEFT_EDGE && canDragLeftEdge(touchX)) {
                        startProgress = touchX / rangeBarWidth
                        onUpdateAndInvalidate()
                    }
                    if (movementState == MovementState.DRAG_RIGHT_EDGE && canDragRightEdge(touchX)) {
                        endProgress = touchX / rangeBarWidth
                        onUpdateAndInvalidate()
                    }
                    disableParentTouch()
                }
            }
            else -> movementState = MovementState.IDLE
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawRoundRect(mainRect, cornerRadius, cornerRadius, backgroundPaint)
            it.drawRoundRect(rangeBarRect, cornerRadius, cornerRadius, rangeBarPaint)
            it.drawStartIndicators()
            it.drawEndIndicators()
        }
    }

    // ---- Private methods ---- //

    private fun Canvas.drawStartIndicators() {
        val x1 = rangeBarRect.left + indicatorsShift * 2
        drawLine(x1, indicatorsStartY, x1, indicatorsStartY + indicatorsHeight, indicatorsPaint)
        val x2 = x1 + indicatorsWidth + indicatorsShift
        drawLine(x2, indicatorsStartY, x2, indicatorsStartY + indicatorsHeight, indicatorsPaint)
    }

    private fun Canvas.drawEndIndicators() {
        val x1 = rangeBarRect.right - indicatorsShift * 2
        drawLine(x1, indicatorsStartY, x1, indicatorsStartY + indicatorsHeight, indicatorsPaint)
        val x2 = x1 - indicatorsWidth - indicatorsShift
        drawLine(x2, indicatorsStartY, x2, indicatorsStartY + indicatorsHeight, indicatorsPaint)
    }

    private fun initAttributes(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.RangeBarView, 0, 0)
            rangeBarHeight = a.getDimensionPixelSize(
                R.styleable.RangeBarView_rbv_height,
                DEFAULT_RANGE_BAR_HEIGHT_DP.dpToPx(context).toInt()
            ).toFloat()
            cornerRadius = a.getDimensionPixelSize(
                R.styleable.RangeBarView_rbv_corners,
                DEFAULT_CORNER_RADIUS_DP.dpToPx(context).toInt()
            ).toFloat()
            viewBackgroundColor = a.getColor(
                R.styleable.RangeBarView_rbv_background_color,
                ContextCompat.getColor(context, defaultBackgroundColorRes)
            )
            rangeBarColor = a.getColor(
                R.styleable.RangeBarView_rbv_color,
                ContextCompat.getColor(context, defaultRangeBarColorRes)
            )
            indicatorsColor = a.getColor(
                R.styleable.RangeBarView_rbv_indicators_color,
                ContextCompat.getColor(context, defaultIndicatorsColorRes)
            )
            resultFraction = a.getInteger(
                R.styleable.RangeBarView_rbv_fraction_digits_count, FRACTION_DIGITS_COUNT
            )
            val startRangeValue = a.getFloat(
                R.styleable.RangeBarView_rbv_range_start,
                DEFAULT_START_VALUE.toFloat()
            )
            val endRangeValue = a.getFloat(
                R.styleable.RangeBarView_rbv_range_end,
                DEFAULT_END_VALUE.toFloat()
            )
            updateValuesRange(startRangeValue.toDouble().normalize()..endRangeValue.toDouble().normalize())
            a.recycle()
        } else {
            initDefault()
        }
    }

    private fun initDefault() {
        rangeBarHeight = DEFAULT_RANGE_BAR_HEIGHT_DP.dpToPx(context)
        cornerRadius = DEFAULT_CORNER_RADIUS_DP.dpToPx(context)
        viewBackgroundColor = ContextCompat.getColor(context, defaultBackgroundColorRes)
        rangeBarColor = ContextCompat.getColor(context, defaultRangeBarColorRes)
        indicatorsColor = ContextCompat.getColor(context, defaultIndicatorsColorRes)
    }

    private fun initPaints() {
        backgroundPaint = Paint().apply {
            color = viewBackgroundColor
            style = Paint.Style.FILL
        }
        rangeBarPaint = Paint().apply {
            color = rangeBarColor
            style = Paint.Style.FILL
            flags = Paint.ANTI_ALIAS_FLAG
        }
        indicatorsPaint = Paint().apply {
            color = indicatorsColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            alpha = INDICATOR_ALPHA
        }
    }

    private fun prepare() {
        val left = rangeBarWidth * startProgress
        val right = rangeBarWidth * endProgress
        mainRect.set(paddingLeft.toFloat(), 0f, rangeBarWidth, rangeBarHeight)
        rangeBarRect.set(left, 0f, right, rangeBarHeight)
        initIndicatorsSizes()
        touchBounds = TOUCH_BOUNDS_DP.dpToPx(context)
    }

    private fun initIndicatorsSizes() {
        indicatorsWidth = INDICATOR_WIDTH_DP.dpToPx(context)
        indicatorsHeight = rangeBarHeight * INDICATOR_HEIGHT_PERCENTS
        indicatorsShift = INDICATOR_SHIFT_DP.dpToPx(context)
        indicatorsStartY = (rangeBarHeight - indicatorsHeight) / 2
        indicatorsPaint.strokeWidth = indicatorsWidth
    }

    private fun onUpdateAndInvalidate() {
        onProgressChanged()
        onValuesChanged()
        invalidate()
    }

    private fun onProgressChanged() {
        val left = rangeBarWidth * startProgress
        val right = rangeBarWidth * endProgress
        rangeBarRect.set(left, 0f, right, rangeBarHeight)
    }

    private fun onValuesChanged() {
        val start = ((valuesRange.endInclusive - valuesRange.start) * startProgress + valuesRange.start).normalize()
        val end = ((valuesRange.endInclusive - valuesRange.start) * endProgress + valuesRange.start).normalize()

        if (values.start != start || values.end != end) {
            values.start = start
            values.end = end
            notifyListeners()
        }
    }

    private fun onValuesChangedOutside() {
        startProgress = ((values.start - valuesRange.start) / (valuesRange.endInclusive - valuesRange.start)).toFloat()
        endProgress = ((values.end - valuesRange.start) / (valuesRange.endInclusive - valuesRange.start)).toFloat()
        onProgressChanged()
        invalidate()
    }

    private fun notifyListeners() {
        listeners.forEach {
            it.onValuesChanged(values.start, values.end)
        }
    }

    private fun Double.normalize(): Double =
        Math.round(this * 10.times(resultFraction)) / 10.times(resultFraction).toDouble()

    private fun isInLeftIndicatorBounds(x: Float, y: Float) =
        x in rangeBarRect.left - touchBounds..rangeBarRect.left + touchBounds &&
            y < rangeBarHeight

    private fun isInRightIndicatorBounds(x: Float, y: Float) =
        x in rangeBarRect.right - touchBounds..rangeBarRect.right + touchBounds &&
            y < rangeBarHeight

    private fun canDragLeftEdge(x: Float) =
        x > paddingLeft && x < rangeBarRect.right - touchBounds * 2

    private fun canDragRightEdge(x: Float): Boolean {
        return x < rangeBarWidth && x > rangeBarRect.left + touchBounds * 2
    }

    private fun disableParentTouch() {
        parent.requestDisallowInterceptTouchEvent(true)
    }

    companion object {
        private const val DEFAULT_RANGE_BAR_HEIGHT_DP = 48f
        private const val DEFAULT_CORNER_RADIUS_DP = 8f
        private const val INDICATOR_HEIGHT_PERCENTS = 0.4f
        private const val INDICATOR_WIDTH_DP = 2f
        private const val INDICATOR_SHIFT_DP = 2f
        private const val INDICATOR_ALPHA = 128
        private const val TOUCH_BOUNDS_DP = 20f
        private const val FRACTION_DIGITS_COUNT = 1
        private const val DEFAULT_START_VALUE = 0.0
        private const val DEFAULT_END_VALUE = 100.0
        private const val TRIANGLE_HEIGHT_DP = 7
        private const val TRIANGLE_PADDING_DP = 3
    }

    private enum class MovementState {
        DRAG_LEFT_EDGE, DRAG_RIGHT_EDGE, IDLE
    }

    private class Values(var start: Double, var end: Double)
}