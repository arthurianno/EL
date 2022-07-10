package com.elta.android.presentation.widgets.rangebar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.decodeBitmap
import com.elta.android.presentation.widgets.rangebar.listeners.OnRageBarValuesChangeListener
import com.elta.android.presentation.widgets.rangebar.listeners.RangeValuesChangedObserver
import com.nullgr.core.font.getTypeface
import com.nullgr.core.ui.extensions.dpToPx
import com.nullgr.core.ui.extensions.spToPx
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import kotlin.math.roundToInt

@Suppress("MagicNumbers", "TooManyFunctions")
class RangeBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val startValue: Double
        get() = values.start

    val endValue: Double
        get() = values.end

    var valuesRange = DEFAULT_START_VALUE..DEFAULT_END_VALUE
        set(value) {
            field = value
            setValues(value.start, value.endInclusive)
        }

    var minRange: Double = Double.NaN

    private var values = Values(DEFAULT_START_VALUE, DEFAULT_END_VALUE)
    private val listeners = arrayListOf<OnRageBarValuesChangeListener>()
    private var resultFraction = FRACTION_DIGITS_COUNT
    private var startProgress = 0f
    private var endProgress = 1f
    private var movementState = MovementState.IDLE

    @ColorRes
    private val defaultBackgroundColorRes = R.color.pale_gray

    @ColorRes
    private val defaultRangeBarColorRes = R.color.shade_g_green_a

    @ColorRes
    private val defaultIndicatorsColorRes = R.color.white

    @ColorRes
    private val defaultTextColor = R.color.black_blue

    @ColorInt
    private var viewBackgroundColor = 0

    @ColorInt
    private var rangeBarColor = 0

    @ColorInt
    private var dragIndicatorsColor = 0

    @ColorInt
    private var textColor = 0

    private var rangeBarTopY = 0f
    private var rangeBarHeight = 0f
    private var fullViewWidth = 0f
    private var cornerRadius = 0f

    private var dragIndicatorsWidth = 0f
    private var dragIndicatorsHeight = 0f
    private var dragIndicatorsPadding = 0f
    private var dragIndicatorsTopY = 0f
    private var dragTouchBounds = 0f
    private var minSpaceBetweenValues = 0f

    private var trianglesPadding = 0f
    private var trianglesHeight = 0f
    private var trianglesWidth = 0f

    private var titleHeight = 0f
    private var titlePadding = 0f
    private var titleOffset = 0f

    private lateinit var backgroundPaint: Paint
    private lateinit var rangeBarPaint: Paint
    private lateinit var dragIndicatorsPaint: Paint
    private lateinit var trianglesPaint: Paint
    private lateinit var valueTextPaint: Paint
    private lateinit var trianglesBitmap: Bitmap

    private val backgroundRect = RectF()
    private val rangeBarRect = RectF()

    init {
        initAttributes(attrs)
        initPaints()
    }

    fun setValues(start: Double, end: Double) {
        if (start in valuesRange && end in valuesRange) {
            values = Values(start.normalize(), end.normalize())
            onValuesChangedOutside()
        }
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
        beforeOnMeasure(widthMeasureSpec)
        val height =
            rangeBarHeight + trianglesHeight + trianglesPadding + titleHeight + titlePadding
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY)
        setMeasuredDimension(widthMeasureSpec, newHeightMeasureSpec)
        afterOnMeasure()
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

            MotionEvent.ACTION_MOVE ->
                if (movementState != MovementState.IDLE) {
                    if (movementState == MovementState.DRAG_LEFT_EDGE) {
                        val touchX = event.x.validateTouchXForLeftEdge()
                        startProgress = (touchX - backgroundRect.left) / backgroundRect.width()
                        onUpdateAndInvalidate()
                    }
                    if (movementState == MovementState.DRAG_RIGHT_EDGE) {
                        val touchX = event.x.validateTouchXForRightEdge()
                        endProgress = (touchX - backgroundRect.left) / backgroundRect.width()
                        onUpdateAndInvalidate()
                    }
                    disableParentTouch()
                }

            else -> movementState = MovementState.IDLE
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawRangeBar()
            it.drawStartValueText()
            it.drawEndValueText()
            it.drawStartDragIndicators()
            it.drawEndDragIndicators()
            it.drawStartTriangle()
            it.drawEndTriangle()
        }
    }

    // ------ Draw methods ----- //

    private fun Canvas.drawRangeBar() {
        drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint)
        drawRoundRect(rangeBarRect, cornerRadius, cornerRadius, rangeBarPaint)
    }

    private fun Canvas.drawStartValueText() {
        val left = rangeBarRect.left + trianglesPadding / 2 + trianglesWidth / 2
        drawText(values.start.format(), left, titleHeight, valueTextPaint)
    }

    private fun Canvas.drawEndValueText() {
        val left = rangeBarRect.right - trianglesWidth / 2 - trianglesPadding / 2
        drawText(values.end.format(), left, titleHeight, valueTextPaint)
    }

    private fun Canvas.drawStartTriangle() {
        val left = rangeBarRect.left + trianglesPadding / 2
        drawBitmap(trianglesBitmap, left, titleHeight + titlePadding, trianglesPaint)
    }

    private fun Canvas.drawEndTriangle() {
        val left = rangeBarRect.right - trianglesWidth - trianglesPadding / 2
        drawBitmap(trianglesBitmap, left, titleHeight + titlePadding, trianglesPaint)
    }

    private fun Canvas.drawStartDragIndicators() {
        val x1 = rangeBarRect.left + dragIndicatorsPadding * 2
        drawLine(
            x1,
            dragIndicatorsTopY,
            x1,
            dragIndicatorsTopY + dragIndicatorsHeight,
            dragIndicatorsPaint
        )
        val x2 = x1 + dragIndicatorsWidth + dragIndicatorsPadding
        drawLine(
            x2,
            dragIndicatorsTopY,
            x2,
            dragIndicatorsTopY + dragIndicatorsHeight,
            dragIndicatorsPaint
        )
    }

    private fun Canvas.drawEndDragIndicators() {
        val x1 = rangeBarRect.right - dragIndicatorsPadding * 2
        drawLine(
            x1,
            dragIndicatorsTopY,
            x1,
            dragIndicatorsTopY + dragIndicatorsHeight,
            dragIndicatorsPaint
        )
        val x2 = x1 - dragIndicatorsWidth - dragIndicatorsPadding
        drawLine(
            x2,
            dragIndicatorsTopY,
            x2,
            dragIndicatorsTopY + dragIndicatorsHeight,
            dragIndicatorsPaint
        )
    }

    // ---- Init methods ---- //

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
            dragIndicatorsColor = a.getColor(
                R.styleable.RangeBarView_rbv_indicators_color,
                ContextCompat.getColor(context, defaultIndicatorsColorRes)
            )
            textColor = a.getColor(
                R.styleable.RangeBarView_rbv_text_color,
                ContextCompat.getColor(context, defaultTextColor)
            )
            resultFraction = a.getInteger(
                R.styleable.RangeBarView_rbv_fraction_digits_count,
                FRACTION_DIGITS_COUNT
            )
            val startRangeValue = a.getFloat(
                R.styleable.RangeBarView_rbv_range_start,
                DEFAULT_START_VALUE.toFloat()
            )
            val endRangeValue = a.getFloat(
                R.styleable.RangeBarView_rbv_range_end,
                DEFAULT_END_VALUE.toFloat()
            )
            valuesRange =
                startRangeValue.toDouble().normalize()..endRangeValue.toDouble().normalize()
            minRange = a.getFloat(R.styleable.RangeBarView_rbv_min_range, Float.NaN).toDouble()
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
        dragIndicatorsColor = ContextCompat.getColor(context, defaultIndicatorsColorRes)
        textColor = ContextCompat.getColor(context, defaultTextColor)
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
        dragIndicatorsPaint = Paint().apply {
            color = dragIndicatorsColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            alpha = INDICATOR_ALPHA
        }
        trianglesPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = DEFAULT_TEXT_SIZE.spToPx(context)
            color = textColor
            typeface = context.getTypeface(TYPEFACE)
        }
    }

    private fun beforeOnMeasure(widthMeasureSpec: Int) {
        fullViewWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()

        trianglesBitmap = context.decodeBitmap(R.drawable.ic_range_bar_triangle)
        trianglesHeight = trianglesBitmap.height.toFloat()
        trianglesWidth = trianglesBitmap.width.toFloat()
        trianglesPadding = TRIANGLE_PADDING_DP.dpToPx(context)

        val fontMetrics = valueTextPaint.fontMetrics
        titleHeight = fontMetrics.descent - fontMetrics.ascent
        titlePadding = TEXT_PADDING_DP.dpToPx(context)
        titleOffset = valueTextPaint.measureText(STUB_TEXT)
    }

    private fun afterOnMeasure() {
        rangeBarTopY = trianglesHeight + trianglesPadding + titleHeight + titlePadding

        val backgroundLeft = paddingLeft.toFloat() + titleOffset
        val backgroundRight = fullViewWidth - titleOffset
        backgroundRect.set(
            backgroundLeft,
            rangeBarTopY,
            backgroundRight,
            rangeBarTopY + rangeBarHeight
        )

        val rangeBarLeft = backgroundRect.left + backgroundRect.width() * startProgress
        val rangeBarRight = backgroundRect.left + backgroundRect.width() * endProgress
        rangeBarRect.set(rangeBarLeft, rangeBarTopY, rangeBarRight, rangeBarTopY + rangeBarHeight)

        initDragIndicatorsSizes()
        dragTouchBounds = TOUCH_BOUNDS_DP.dpToPx(context)
        minSpaceBetweenValues = when (minRange.isNaN() || minRange == 0.0) {
            true -> dragTouchBounds * 2
            else -> {
                val minProgress = minRange / (valuesRange.endInclusive - valuesRange.start)
                (backgroundRect.width() * minProgress).toFloat()
            }
        }
    }

    private fun initDragIndicatorsSizes() {
        dragIndicatorsWidth = INDICATOR_WIDTH_DP.dpToPx(context)
        dragIndicatorsHeight = rangeBarHeight * INDICATOR_HEIGHT_PERCENTS
        dragIndicatorsPadding = INDICATOR_SHIFT_DP.dpToPx(context)
        dragIndicatorsTopY = backgroundRect.top + (rangeBarHeight - dragIndicatorsHeight) / 2
        dragIndicatorsPaint.strokeWidth = dragIndicatorsWidth
    }

    // --------- Interact methods ------ //

    private fun onUpdateAndInvalidate() {
        onProgressChanged()
        onValuesChanged()
        invalidate()
    }

    private fun onProgressChanged() {
        rangeBarRect.left = backgroundRect.left + backgroundRect.width() * startProgress
        rangeBarRect.right = backgroundRect.left + backgroundRect.width() * endProgress
    }

    private fun onValuesChanged() {
        val start =
            ((valuesRange.endInclusive - valuesRange.start) * startProgress + valuesRange.start).normalize()
        val end =
            ((valuesRange.endInclusive - valuesRange.start) * endProgress + valuesRange.start).normalize()

        if (values.start != start || values.end != end) {
            values.start = start
            values.end = end
            notifyListeners()
        }
    }

    private fun onValuesChangedOutside() {
        startProgress =
            ((values.start - valuesRange.start) / (valuesRange.endInclusive - valuesRange.start)).toFloat()
        endProgress =
            ((values.end - valuesRange.start) / (valuesRange.endInclusive - valuesRange.start)).toFloat()
        onProgressChanged()
        invalidate()
    }

    private fun notifyListeners() {
        listeners.forEach {
            it.onValuesChanged(values.start, values.end)
        }
    }

    // ------- Utility methods ------ //

    private fun Double.normalize(): Double =
        (this * 10.times(resultFraction)).roundToInt() / 10.times(resultFraction).toDouble()

    private fun Double.format() = NumberFormatter.numberFormat.format(this)

    private fun isInLeftIndicatorBounds(x: Float, y: Float) =
        x in rangeBarRect.left - dragTouchBounds..rangeBarRect.left + dragTouchBounds &&
            y > rangeBarTopY && y < rangeBarTopY + rangeBarHeight

    private fun isInRightIndicatorBounds(x: Float, y: Float) =
        x in rangeBarRect.right - dragTouchBounds..rangeBarRect.right + dragTouchBounds &&
            y > rangeBarTopY && y < rangeBarTopY + rangeBarHeight

    private fun Float.validateTouchXForLeftEdge(): Float =
        when {
            this < rangeBarRect.left -> backgroundRect.left.coerceAtLeast(this)
            else -> (rangeBarRect.right - minSpaceBetweenValues).coerceAtMost(this)
        }

    private fun Float.validateTouchXForRightEdge(): Float =
        when {
            this > rangeBarRect.right -> backgroundRect.right.coerceAtMost(this)
            else -> (rangeBarRect.left + minSpaceBetweenValues).coerceAtLeast(this)
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
        private const val TRIANGLE_PADDING_DP = 3f
        private const val TEXT_PADDING_DP = 5f
        private const val DEFAULT_TEXT_SIZE = 13f
        private const val TYPEFACE = "roboto_medium.ttf"
        private const val STUB_TEXT = "#"
    }

    private enum class MovementState {
        DRAG_LEFT_EDGE, DRAG_RIGHT_EDGE, IDLE
    }

    private data class Values(var start: Double, var end: Double)
}
