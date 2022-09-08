package com.elta.android.presentation.widgets.picker

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextUtils
import android.text.method.NumberKeyListener
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.elta.android.presentation.R
import com.nullgr.core.ui.extensions.hide
import java.text.NumberFormat
import java.util.Formatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val VERTICAL = LinearLayout.VERTICAL
private const val HORIZONTAL = LinearLayout.HORIZONTAL
private const val ASCENDING = 0
private const val DESCENDING = 1
private const val RIGHT = 0
private const val CENTER = 1
private const val LEFT = 2
private const val DEFAULT_LONG_PRESS_UPDATE_INTERVAL: Long = 300
private const val DEFAULT_MAX_FLING_VELOCITY_COEFFICIENT = 8
private const val SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800
private const val SNAP_SCROLL_DURATION = 300
private const val DEFAULT_FADING_EDGE_STRENGTH = 0.9f
private const val UNSCALED_DEFAULT_DIVIDER_THICKNESS = 2
private const val UNSCALED_DEFAULT_DIVIDER_DISTANCE = 48
private const val SIZE_UNSPECIFIED = -1
private const val DEFAULT_DIVIDER_COLOR = -0x1000000
private const val DEFAULT_MAX_VALUE = 100
private const val DEFAULT_MIN_VALUE = 1
private const val DEFAULT_WHEEL_ITEM_COUNT = 3
private const val DEFAULT_MAX_HEIGHT = 180
private const val DEFAULT_MIN_WIDTH = 64
private const val DEFAULT_TEXT_COLOR = -0x1000000
private const val DEFAULT_TEXT_SIZE = 25f
private const val DEFAULT_TEXT_ALIGN = CENTER
private const val DEFAULT_LINE_SPACING_MULTIPLIER = 1f
private val DIGIT_CHARACTERS = charArrayOf( // Latin digits are the common case
    '0',
    '1',
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9', // Arabic-Indic
    '\u0660',
    '\u0661',
    '\u0662',
    '\u0663',
    '\u0664',
    '\u0665',
    '\u0666',
    '\u0667',
    '\u0668',
    '\u0669', // Extended Arabic-Indic
    '\u06f0',
    '\u06f1',
    '\u06f2',
    '\u06f3',
    '\u06f4',
    '\u06f5',
    '\u06f6',
    '\u06f7',
    '\u06f8',
    '\u06f9', // Hindi and Marathi (Devanagari script)
    '\u0966',
    '\u0967',
    '\u0968',
    '\u0969',
    '\u096a',
    '\u096b',
    '\u096c',
    '\u096d',
    '\u096e',
    '\u096f', // Bengali
    '\u09e6',
    '\u09e7',
    '\u09e8',
    '\u09e9',
    '\u09ea',
    '\u09eb',
    '\u09ec',
    '\u09ed',
    '\u09ee',
    '\u09ef', // Kannada
    '\u0ce6',
    '\u0ce7',
    '\u0ce8',
    '\u0ce9',
    '\u0cea',
    '\u0ceb',
    '\u0cec',
    '\u0ced',
    '\u0cee',
    '\u0cef', // Negative
    '-'
)

/**
 * A widget that enables the user to select a number from a predefined range.
 */
class NumberPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    @IntDef(VERTICAL, HORIZONTAL)
    annotation class Orientation

    @IntDef(ASCENDING, DESCENDING)
    annotation class Order

    @IntDef(LEFT, CENTER, RIGHT)
    annotation class Align

    private val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    private val defaultClickListener = OnClickListener {
        selectedText.isVisible = true
        selectedText.requestFocus()
        showKeyboard()
    }

    private fun showKeyboard() {
        inputMethodManager.showSoftInput(selectedText, InputMethodManager.SHOW_IMPLICIT)
    }

    private val focusChangeListener = OnFocusChangeListener { _, isFocused ->
        if (!isFocused) {
            selectedText.hide()
            runCatching { pickerValue = selectedText.text.toString().toInt() }
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(
            windowToken,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    var minValue = DEFAULT_MIN_VALUE
        set(value) {
            field = value
            if (value > pickerValue) {
                pickerValue = value
            }
            val wrapSelectorWheel = maxValue - minValue > selectorIndices.size
            setWrapSelectorWheel(wrapSelectorWheel)
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }

    var maxValue = DEFAULT_MAX_VALUE
        set(value) {
            require(maxValue >= 0) { "maxValue must be >= 0" }
            field = value
            if (value < pickerValue) {
                pickerValue = value
            }
            updateWrapSelectorWheel()
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }

    var pickerValue: Int = 0
        set(value) {
            if (field != value) notifyChange(field, value)
            if (wrapSelectorWheel) {
                field = getWrappedSelectorIndex(value)
            } else {
                field = max(value, minValue)
                field = min(value, maxValue)
            }
            selectedText.setText(value.toString())
            if (scrollState != OnScrollListener.SCROLL_STATE_FLING) {
                updateInputTextView()
            }
            initializeSelectorWheelIndices()
            updateAccessibilityDescription()
            invalidate()
        }

    private var formatter: Formatter? = null
        set(value) {
            if (field === value) {
                return
            }
            field = value
            initializeSelectorWheelIndices()
            updateInputTextView()
        }

    private var selectedText: EditText = EditText(context)
    private var selectedTextCenterX = 0f
    private var selectedTextCenterY = 0f
    private var minHeight = 0
    private var maxHeight = 0
    private var minWidth = 0

    private var maxWidth = 0
    private var computeMaxWidth = true

    @Align
    private var selectedTextAlign = DEFAULT_TEXT_ALIGN
    private var selectedTextColor = DEFAULT_TEXT_COLOR
    private var selectedTextSize = DEFAULT_TEXT_SIZE
    private var selectedTextStrikeThrough = false
    private var selectedTextUnderline = false
    private var textAlign = DEFAULT_TEXT_ALIGN
    private var textColor = DEFAULT_TEXT_COLOR
    private var textSize = DEFAULT_TEXT_SIZE
    private var textStrikeThrough = false
    private val textMaxSize: Float
        get() = max(textSize, selectedTextSize)
    private var textUnderline = false
    private var typeface: Typeface = Typeface.DEFAULT
    private var selectorTextGapWidth = 0
    private var selectorTextGapHeight = 0
    private var displayedValues: List<String> = emptyList()
    private var clickListener: OnClickListener? = defaultClickListener
    private val valueChangeListeners = mutableListOf<OnValueChangeListener>()
    private var scrollListener: OnScrollListener? = null
    private var longPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL
    private val selectorIndexToStringCache = SparseArray<String>()
    private var wheelItemCount = DEFAULT_WHEEL_ITEM_COUNT
    private var realWheelItemCount = DEFAULT_WHEEL_ITEM_COUNT
    private var wheelMiddleItemIndex = wheelItemCount / 2
    private var selectorIndices = IntArray(wheelItemCount)
    private var selectorWheelPaint: Paint = Paint()
    private var selectorElementSize = 0
    private var initialScrollOffset = Int.MIN_VALUE
    private var currentScrollOffset = 0
    private val flingScroller: Scroller = Scroller(context, null, true)
    private val adjustScroller: Scroller = Scroller(context, DecelerateInterpolator(2.5f))
    private var previousScrollerX = 0
    private var previousScrollerY = 0
    private var setSelectionCommand: SetSelectionCommand? = null
    private var changeCurrentByOneFromLongPressCommand: ChangeCurrentByOneFromLongPressCommand? =
        null
    private var lastDownEventX = 0f
    private var lastDownEventY = 0f
    private var lastDownOrMoveEventX = 0f
    private var lastDownOrMoveEventY = 0f
    private var velocityTracker: VelocityTracker? = null
    private val touchSlop: Int
    private val minimumFlingVelocity: Int
    private var maximumFlingVelocity: Int
    private var wrapSelectorWheel = true
    private var wrapSelectorWheelPreferred = true
    private var pickerDividerDrawable: Drawable? = null
    private var dividerColor = DEFAULT_DIVIDER_COLOR
        set(value) {
            field = value
            pickerDividerDrawable = ColorDrawable(value)
        }
    private var dividerDistance: Int
    private var dividerThickness: Int
    private var topDividerTop = 0
    private var bottomDividerBottom = 0
    private var leftDividerLeft = 0
    private var rightDividerRight = 0
    private var scrollState = OnScrollListener.SCROLL_STATE_IDLE
    private var lastHandledDownDpadKeyCode = -1
    private val hideWheelUntilFocused: Boolean
    private val pickerWidth: Float
    private val pickerHeight: Float
    private var orientation: Int
    private var order: Int = ASCENDING
    private var fadingEdgeEnabled = true
    private var fadingEdgeStrength = DEFAULT_FADING_EDGE_STRENGTH
    private var scrollerEnabled = true
    private var lineSpacingMultiplier = DEFAULT_LINE_SPACING_MULTIPLIER
    private var maxFlingVelocityCoefficient = DEFAULT_MAX_FLING_VELOCITY_COEFFICIENT
    private var numberFormatter: NumberFormat
    private val viewConfiguration: ViewConfiguration

    init {
        numberFormatter = NumberFormat.getInstance()
        val attributes = context.obtainStyledAttributes(
            attrs,
            R.styleable.NumberPicker,
            defStyle,
            0
        )
        val selectionDivider = attributes.getDrawable(
            R.styleable.NumberPicker_np_divider
        )
        if (selectionDivider != null) {
            selectionDivider.callback = this
            if (selectionDivider.isStateful) {
                selectionDivider.state = drawableState
            }
            pickerDividerDrawable = selectionDivider
        } else {
            dividerColor = attributes.getColor(
                R.styleable.NumberPicker_np_dividerColor,
                dividerColor
            )
            this.dividerColor = dividerColor
        }
        val displayMetrics = resources.displayMetrics
        val defDividerDistance = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            UNSCALED_DEFAULT_DIVIDER_DISTANCE.toFloat(),
            displayMetrics
        ).toInt()
        val defDividerThickness = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            UNSCALED_DEFAULT_DIVIDER_THICKNESS.toFloat(),
            displayMetrics
        ).toInt()
        dividerDistance = attributes.getDimensionPixelSize(
            R.styleable.NumberPicker_np_dividerDistance,
            defDividerDistance
        )
        dividerThickness = attributes.getDimensionPixelSize(
            R.styleable.NumberPicker_np_dividerThickness,
            defDividerThickness
        )
        order = attributes.getInt(R.styleable.NumberPicker_np_order, ASCENDING)
        orientation = attributes.getInt(R.styleable.NumberPicker_np_orientation, VERTICAL)
        pickerWidth = attributes.getDimensionPixelSize(
            R.styleable.NumberPicker_np_width,
            SIZE_UNSPECIFIED
        ).toFloat()
        pickerHeight = attributes.getDimensionPixelSize(
            R.styleable.NumberPicker_np_height,
            SIZE_UNSPECIFIED
        ).toFloat()
        setWidthAndHeight()
        pickerValue = attributes.getInt(R.styleable.NumberPicker_np_value, 0)
        maxValue = attributes.getInt(R.styleable.NumberPicker_np_max, maxValue)
        minValue = attributes.getInt(R.styleable.NumberPicker_np_min, minValue)
        selectedTextAlign = attributes.getInt(
            R.styleable.NumberPicker_np_selectedTextAlign,
            selectedTextAlign
        )
        selectedTextColor = attributes.getColor(
            R.styleable.NumberPicker_np_selectedTextColor,
            selectedTextColor
        )
        selectedTextSize = attributes.getDimension(
            R.styleable.NumberPicker_np_selectedTextSize,
            spToPx(selectedTextSize)
        )
        selectedTextStrikeThrough = attributes.getBoolean(
            R.styleable.NumberPicker_np_selectedTextStrikeThru,
            selectedTextStrikeThrough
        )
        selectedTextUnderline = attributes.getBoolean(
            R.styleable.NumberPicker_np_selectedTextUnderline,
            selectedTextUnderline
        )
        textAlign = attributes.getInt(R.styleable.NumberPicker_np_textAlign, textAlign)
        textColor = attributes.getColor(R.styleable.NumberPicker_np_textColor, textColor)
        textSize = attributes.getDimension(
            R.styleable.NumberPicker_np_textSize,
            spToPx(textSize)
        )
        textStrikeThrough = attributes.getBoolean(
            R.styleable.NumberPicker_np_textStrikeThru,
            textStrikeThrough
        )
        textUnderline = attributes.getBoolean(
            R.styleable.NumberPicker_np_textUnderline,
            textUnderline
        )
        typeface = Typeface.create(
            attributes.getString(R.styleable.NumberPicker_np_typeface),
            Typeface.NORMAL
        )
        formatter =
            stringToFormatter(attributes.getString(R.styleable.NumberPicker_np_formatter).orEmpty())
        fadingEdgeEnabled = attributes.getBoolean(
            R.styleable.NumberPicker_np_fadingEdgeEnabled,
            fadingEdgeEnabled
        )
        fadingEdgeStrength = attributes.getFloat(
            R.styleable.NumberPicker_np_fadingEdgeStrength,
            fadingEdgeStrength
        )
        scrollerEnabled = attributes.getBoolean(
            R.styleable.NumberPicker_np_scrollerEnabled,
            scrollerEnabled
        )
        wheelItemCount = attributes.getInt(
            R.styleable.NumberPicker_np_wheelItemCount,
            wheelItemCount
        )
        lineSpacingMultiplier = attributes.getFloat(
            R.styleable.NumberPicker_np_lineSpacingMultiplier,
            lineSpacingMultiplier
        )
        maxFlingVelocityCoefficient = attributes.getInt(
            R.styleable.NumberPicker_np_max_fling_velocity_coefficient,
            maxFlingVelocityCoefficient
        )
        hideWheelUntilFocused = attributes.getBoolean(
            R.styleable.NumberPicker_np_hideWheelUntilFocused,
            false
        )

        // By default Linearlayout that we extend is not drawn. This is
        // its draw() method is not called but dispatchDraw() is called
        // directly (see ViewGroup.drawChild()). However, this class uses
        // the fading edge effect implemented by View and we need our
        // draw() method to be called. Therefore, we declare we will draw.
        setWillNotDraw(false)
        val inflater = context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater
        inflater.inflate(R.layout.number_picker_material, this, true)

        // input text
        initInputField()

        // create the selector wheel paint
        val paint = Paint()
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        selectorWheelPaint = paint
        selectorWheelPaint.isFakeBoldText = true
        setSelectedTextColor(selectedTextColor)
        setTextColor(textColor)
        setTextSize(textSize)
        setSelectedTextSize(selectedTextSize)
        setTypeface(typeface)
        updateInputTextView()
        setWheelItemCount(wheelItemCount)
        wrapSelectorWheel = attributes.getBoolean(
            R.styleable.NumberPicker_np_wrapSelectorWheel,
            wrapSelectorWheel
        )
        setWrapSelectorWheel(wrapSelectorWheel)
        if (pickerWidth != SIZE_UNSPECIFIED.toFloat() && pickerHeight != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = pickerWidth / minWidth
            scaleY = pickerHeight / maxHeight
        } else if (pickerWidth != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = pickerWidth / minWidth
            scaleY = pickerWidth / minWidth
        } else if (pickerHeight != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = pickerHeight / maxHeight
            scaleY = pickerHeight / maxHeight
        }

        // initialize constants
        viewConfiguration = ViewConfiguration.get(context)
        touchSlop = viewConfiguration.scaledTouchSlop
        minimumFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
        maximumFlingVelocity = (
            viewConfiguration.scaledMaximumFlingVelocity /
                maxFlingVelocityCoefficient
            )

        // create the fling and adjust scrollers
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // If not explicitly specified this view is important for accessibility.
            if (importantForAccessibility == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Should be focusable by default, as the text view whose visibility changes is focusable
            if (focusable == FOCUSABLE_AUTO) {
                focusable = FOCUSABLE
                isFocusableInTouchMode = true
            }
        }
        attributes.recycle()
    }

    private fun initInputField() {
        selectedText = findViewById(R.id.np_numberpicker_input)
        selectedText.setText(formatNumber(pickerValue))
        selectedText.inputType = InputType.TYPE_CLASS_NUMBER
        selectedText.onFocusChangeListener = focusChangeListener
        selectedText.setSelectAllOnFocus(true)
        selectedText.imeOptions = EditorInfo.IME_ACTION_NEXT
        selectedText.filters = arrayOf(
            InputFilter { source: CharSequence?, _: Int, _: Int, prev: Spanned?, prevStart: Int, prevEnd: Int ->
                runCatching {
                    val sourceInt = if (prevStart == prevEnd) {
                        (prev?.toString().orEmpty() + source).toInt()
                    } else {
                        source?.toString()?.toInt()
                    }
                    if (sourceInt in minValue..maxValue) {
                        source
                    } else {
                        ""
                    }
                }.getOrNull() ?: ""
            }
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // Input text centered horizontally.
        val inputTextLeft = (measuredWidth - selectedText.measuredWidth) / 2
        val inputTextTop = (measuredHeight - selectedText.measuredHeight) / 2
        val inputTextRight = inputTextLeft + selectedText.measuredWidth
        val inputTextBottom = inputTextTop + selectedText.measuredHeight
        selectedText.layout(inputTextLeft, inputTextTop, inputTextRight, inputTextBottom)
        selectedTextCenterX = selectedText.x + selectedText.measuredWidth / 2
        selectedTextCenterY = selectedText.y + selectedText.measuredHeight / 2
        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel()
            initializeFadingEdges()
            val dividerDistance = 2 * dividerThickness + dividerDistance
            if (isHorizontalMode()) {
                leftDividerLeft = (width - this.dividerDistance) / 2 - dividerThickness
                rightDividerRight = leftDividerLeft + dividerDistance
            } else {
                topDividerTop = (height - this.dividerDistance) / 2 - dividerThickness
                bottomDividerBottom = topDividerTop + dividerDistance
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Try greedily to fit the max width and height.
        super.onMeasure(
            makeMeasureSpec(widthMeasureSpec, maxWidth),
            makeMeasureSpec(heightMeasureSpec, maxHeight)
        )
        // Flag if we are measured with width or height less than the respective min.
        setMeasuredDimension(
            resolveSizeAndStateRespectingMinSize(
                minWidth,
                measuredWidth,
                widthMeasureSpec
            ),
            resolveSizeAndStateRespectingMinSize(
                minHeight,
                measuredHeight,
                heightMeasureSpec
            )
        )
    }

    override fun onDraw(canvas: Canvas) {
        // save canvas
        canvas.save()
        val showSelectorWheel = if (hideWheelUntilFocused) hasFocus() else true
        var x: Float
        var y: Float
        if (isHorizontalMode()) {
            x = currentScrollOffset.toFloat()
            y = (selectedText.baseline + selectedText.top).toFloat()
            if (realWheelItemCount < DEFAULT_WHEEL_ITEM_COUNT) {
                canvas.clipRect(leftDividerLeft, 0, rightDividerRight, bottom)
            }
        } else {
            x = ((right - left) / 2).toFloat()
            y = currentScrollOffset.toFloat()
            if (realWheelItemCount < DEFAULT_WHEEL_ITEM_COUNT) {
                canvas.clipRect(0, topDividerTop, right, bottomDividerBottom)
            }
        }

        // draw the selector wheel
        val selectorIndices = getSelectorIndices()
        for (i in selectorIndices.indices) {
            if (i == wheelMiddleItemIndex) {
                selectorWheelPaint.textAlign = Paint.Align.values()[selectedTextAlign]
                selectorWheelPaint.textSize = selectedTextSize
                selectorWheelPaint.color = selectedTextColor
                selectorWheelPaint.isStrikeThruText = selectedTextStrikeThrough
                selectorWheelPaint.isUnderlineText = selectedTextUnderline
            } else {
                selectorWheelPaint.textAlign = Paint.Align.values()[textAlign]
                selectorWheelPaint.textSize = textSize
                selectorWheelPaint.color = textColor
                selectorWheelPaint.isStrikeThruText = textStrikeThrough
                selectorWheelPaint.isUnderlineText = textUnderline
            }
            val selectorIndex =
                selectorIndices[if (isAscendingOrder()) i else selectorIndices.size - i - 1]
            val scrollSelectorValue = selectorIndexToStringCache[selectorIndex]
            // Do not draw the middle item if input is visible since the input
            // is shown only if the wheel is static and it covers the middle
            // item. Otherwise, if the user starts editing the text via the
            // IME he may see a dimmed version of the old weight intermixed
            // with the new one.
            if (showSelectorWheel && i != wheelMiddleItemIndex ||
                i == wheelMiddleItemIndex && selectedText.visibility != VISIBLE
            ) {
                var textY = y
                if (!isHorizontalMode()) {
                    textY += getPaintCenterY(selectorWheelPaint.fontMetrics)
                }
                drawText(scrollSelectorValue, x, textY, selectorWheelPaint, canvas)
            }
            if (isHorizontalMode()) {
                x += selectorElementSize.toFloat()
            } else {
                y += selectorElementSize.toFloat()
            }
        }

        // restore canvas
        canvas.restore()

        // draw the dividers
        if (showSelectorWheel && pickerDividerDrawable != null) {
            if (isHorizontalMode()) {
                val bottom = bottom

                // draw the left divider
                val leftOfLeftDivider = leftDividerLeft
                val rightOfLeftDivider = leftOfLeftDivider + dividerThickness
                pickerDividerDrawable!!.setBounds(leftOfLeftDivider, 0, rightOfLeftDivider, bottom)
                pickerDividerDrawable!!.draw(canvas)

                // draw the right divider
                val rightOfRightDivider = rightDividerRight
                val leftOfRightDivider = rightOfRightDivider - dividerThickness
                pickerDividerDrawable!!.setBounds(
                    leftOfRightDivider,
                    0,
                    rightOfRightDivider,
                    bottom
                )
                pickerDividerDrawable!!.draw(canvas)
            } else {
                val right = right

                // draw the top divider
                val topOfTopDivider = topDividerTop
                val bottomOfTopDivider = topOfTopDivider + dividerThickness
                pickerDividerDrawable!!.setBounds(0, topOfTopDivider, right, bottomOfTopDivider)
                pickerDividerDrawable!!.draw(canvas)

                // draw the bottom divider
                val bottomOfBottomDivider = bottomDividerBottom
                val topOfBottomDivider = bottomOfBottomDivider - dividerThickness
                pickerDividerDrawable!!.setBounds(
                    0,
                    topOfBottomDivider,
                    right,
                    bottomOfBottomDivider
                )
                pickerDividerDrawable!!.draw(canvas)
            }
        }
    }

    /**
     * Move to the final position of a scroller. Ensures to force finish the scroller
     * and if it is not at its final position a scroll of the selector wheel is
     * performed to fast forward to the final position.
     *
     * @param scroller The scroller to whose final position to get.
     * @return True of the a move was performed, i.e. the scroller was not in final position.
     */
    private fun moveToFinalScrollerPosition(scroller: Scroller): Boolean {
        scroller.forceFinished(true)
        if (isHorizontalMode()) {
            var amountToScroll = scroller.finalX - scroller.currX
            val futureScrollOffset = (currentScrollOffset + amountToScroll) % selectorElementSize
            var overshootAdjustment = initialScrollOffset - futureScrollOffset
            if (overshootAdjustment != 0) {
                if (abs(overshootAdjustment) > selectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= selectorElementSize
                    } else {
                        overshootAdjustment += selectorElementSize
                    }
                }
                amountToScroll += overshootAdjustment
                scrollBy(amountToScroll, 0)
                return true
            }
        } else {
            var amountToScroll = scroller.finalY - scroller.currY
            val futureScrollOffset = (currentScrollOffset + amountToScroll) % selectorElementSize
            var overshootAdjustment = initialScrollOffset - futureScrollOffset
            if (overshootAdjustment != 0) {
                if (abs(overshootAdjustment) > selectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= selectorElementSize
                    } else {
                        overshootAdjustment += selectorElementSize
                    }
                }
                amountToScroll += overshootAdjustment
                scrollBy(0, amountToScroll)
                return true
            }
        }
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                removeAllCallbacks()
                // Make sure we support flinging inside scrollables.
                parent.requestDisallowInterceptTouchEvent(true)
                if (isHorizontalMode()) {
                    lastDownEventX = event.x
                    lastDownOrMoveEventX = lastDownEventX
                    when {
                        !flingScroller.isFinished -> {
                            flingScroller.forceFinished(true)
                            adjustScroller.forceFinished(true)
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                        }
                        !adjustScroller.isFinished -> {
                            flingScroller.forceFinished(true)
                            adjustScroller.forceFinished(true)
                        }
                        lastDownEventX >= leftDividerLeft &&
                            lastDownEventX <= rightDividerRight -> clickListener?.onClick(this)
                        lastDownEventX < leftDividerLeft ->
                            postChangeCurrentByOneFromLongPress(false)
                        lastDownEventX > rightDividerRight ->
                            postChangeCurrentByOneFromLongPress(true)
                    }
                } else {
                    lastDownEventY = event.y
                    lastDownOrMoveEventY = lastDownEventY
                    when {
                        !flingScroller.isFinished -> {
                            flingScroller.forceFinished(true)
                            adjustScroller.forceFinished(true)
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                        }
                        !adjustScroller.isFinished -> {
                            flingScroller.forceFinished(true)
                            adjustScroller.forceFinished(true)
                        }
                        lastDownEventY >= topDividerTop &&
                            lastDownEventY <= bottomDividerBottom ->
                            clickListener?.onClick(this)
                        lastDownEventY < topDividerTop ->
                            postChangeCurrentByOneFromLongPress(false)
                        lastDownEventY > bottomDividerBottom ->
                            postChangeCurrentByOneFromLongPress(true)
                    }
                }
                return true
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || !scrollerEnabled) {
            return false
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {
                if (isHorizontalMode()) {
                    val currentMoveX = event.x
                    if (scrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        val deltaDownX = abs(currentMoveX - lastDownEventX).toInt()
                        if (deltaDownX > touchSlop) {
                            removeAllCallbacks()
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                    } else {
                        val deltaMoveX = (currentMoveX - lastDownOrMoveEventX).toInt()
                        scrollBy(deltaMoveX, 0)
                        invalidate()
                    }
                    lastDownOrMoveEventX = currentMoveX
                } else {
                    val currentMoveY = event.y
                    if (scrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        val deltaDownY = abs(currentMoveY - lastDownEventY).toInt()
                        if (deltaDownY > touchSlop) {
                            removeAllCallbacks()
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                    } else {
                        val deltaMoveY = (currentMoveY - lastDownOrMoveEventY).toInt()
                        scrollBy(0, deltaMoveY)
                        invalidate()
                    }
                    lastDownOrMoveEventY = currentMoveY
                }
            }
            MotionEvent.ACTION_UP -> {
                removeChangeCurrentByOneFromLongPress()
                val velocityTracker = velocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, maximumFlingVelocity.toFloat())
                if (isHorizontalMode()) {
                    val initialVelocity = velocityTracker.xVelocity.toInt()
                    if (abs(initialVelocity) > minimumFlingVelocity) {
                        fling(initialVelocity)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    } else {
                        val eventX = event.x.toInt()
                        val deltaMoveX = abs(eventX - lastDownEventX).toInt()
                        if (deltaMoveX <= touchSlop) {
                            val selectorIndexOffset = (
                                eventX / selectorElementSize -
                                    wheelMiddleItemIndex
                                )
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true)
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false)
                            } else {
                                ensureScrollWheelAdjusted()
                            }
                        } else {
                            ensureScrollWheelAdjusted()
                        }
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    }
                } else {
                    val initialVelocity = velocityTracker.yVelocity.toInt()
                    if (abs(initialVelocity) > minimumFlingVelocity) {
                        fling(initialVelocity)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    } else {
                        val eventY = event.y.toInt()
                        val deltaMoveY = abs(eventY - lastDownEventY).toInt()
                        if (deltaMoveY <= touchSlop) {
                            val selectorIndexOffset = (
                                eventY / selectorElementSize -
                                    wheelMiddleItemIndex
                                )
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true)
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false)
                            } else {
                                ensureScrollWheelAdjusted()
                            }
                        } else {
                            ensureScrollWheelAdjusted()
                        }
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    }
                }
                this.velocityTracker!!.recycle()
                this.velocityTracker = null
            }
        }
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> removeAllCallbacks()
        }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        when (val keyCode = event.keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> removeAllCallbacks()
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_UP -> when (event.action) {
                KeyEvent.ACTION_DOWN -> if (wrapSelectorWheel || (if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) pickerValue < maxValue else pickerValue > minValue)) {
                    requestFocus()
                    lastHandledDownDpadKeyCode = keyCode
                    removeAllCallbacks()
                    if (flingScroller.isFinished) {
                        changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                    }
                    return true
                }
                KeyEvent.ACTION_UP -> if (lastHandledDownDpadKeyCode == keyCode) {
                    lastHandledDownDpadKeyCode = -1
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTrackballEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> removeAllCallbacks()
        }
        return super.dispatchTrackballEvent(event)
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = NumberPicker::class.java.name
        event.isScrollable = scrollerEnabled
        val scroll = (minValue + pickerValue) * selectorElementSize
        val maxScroll = (maxValue - minValue) * selectorElementSize
        if (isHorizontalMode()) {
            event.scrollX = scroll
            event.maxScrollX = maxScroll
        } else {
            event.scrollY = scroll
            event.maxScrollY = maxScroll
        }
    }

    override fun computeScroll() {
        if (!scrollerEnabled) {
            return
        }
        var scroller = flingScroller
        if (scroller.isFinished) {
            scroller = adjustScroller
            if (scroller.isFinished) {
                return
            }
        }
        scroller.computeScrollOffset()
        if (isHorizontalMode()) {
            val currentScrollerX = scroller.currX
            if (previousScrollerX == 0) {
                previousScrollerX = scroller.startX
            }
            scrollBy(currentScrollerX - previousScrollerX, 0)
            previousScrollerX = currentScrollerX
        } else {
            val currentScrollerY = scroller.currY
            if (previousScrollerY == 0) {
                previousScrollerY = scroller.startY
            }
            scrollBy(0, currentScrollerY - previousScrollerY)
            previousScrollerY = currentScrollerY
        }
        if (scroller.isFinished) {
            onScrollerFinished(scroller)
        } else {
            postInvalidate()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        selectedText.isEnabled = enabled
    }

    override fun scrollBy(x: Int, y: Int) {
        if (!scrollerEnabled) {
            return
        }
        val selectorIndices = getSelectorIndices()
        val startScrollOffset = currentScrollOffset
        val gap: Int
        if (isHorizontalMode()) {
            if (isAscendingOrder()) {
                if (!wrapSelectorWheel && x > 0 && selectorIndices[wheelMiddleItemIndex] <= minValue) {
                    currentScrollOffset = initialScrollOffset
                    return
                }
                if (!wrapSelectorWheel && x < 0 && selectorIndices[wheelMiddleItemIndex] >= maxValue) {
                    currentScrollOffset = initialScrollOffset
                    return
                }
            } else {
                if (!wrapSelectorWheel && x > 0 && selectorIndices[wheelMiddleItemIndex] >= maxValue) {
                    currentScrollOffset = initialScrollOffset
                    return
                }
                if (!wrapSelectorWheel && x < 0 && selectorIndices[wheelMiddleItemIndex] <= minValue) {
                    currentScrollOffset = initialScrollOffset
                    return
                }
            }
            currentScrollOffset += x
            gap = selectorTextGapWidth
        } else {
            if (isAscendingOrder()) {
                if (!wrapSelectorWheel && y > 0 && selectorIndices[wheelMiddleItemIndex] <= minValue) {
                    currentScrollOffset = initialScrollOffset
                    return
                }
                if (!wrapSelectorWheel && y < 0 && selectorIndices[wheelMiddleItemIndex] >= maxValue) {
                    currentScrollOffset = initialScrollOffset
                    return
                }
            } else {
                if (!wrapSelectorWheel && y > 0 && selectorIndices[wheelMiddleItemIndex] >= maxValue) {
                    currentScrollOffset = initialScrollOffset
                    return
                }
                if (!wrapSelectorWheel && y < 0 && selectorIndices[wheelMiddleItemIndex] <= minValue) {
                    currentScrollOffset = initialScrollOffset
                    return
                }
            }
            currentScrollOffset += y
            gap = selectorTextGapHeight
        }
        while (currentScrollOffset - initialScrollOffset > gap) {
            currentScrollOffset -= selectorElementSize
            if (isAscendingOrder()) {
                decrementSelectorIndices(selectorIndices)
            } else {
                incrementSelectorIndices(selectorIndices)
            }
            pickerValue = selectorIndices[wheelMiddleItemIndex]
            if (!wrapSelectorWheel && selectorIndices[wheelMiddleItemIndex] < minValue) {
                currentScrollOffset = initialScrollOffset
            }
        }
        while (currentScrollOffset - initialScrollOffset < -gap) {
            currentScrollOffset += selectorElementSize
            if (isAscendingOrder()) {
                incrementSelectorIndices(selectorIndices)
            } else {
                decrementSelectorIndices(selectorIndices)
            }
            pickerValue = selectorIndices[wheelMiddleItemIndex]
            if (!wrapSelectorWheel && selectorIndices[wheelMiddleItemIndex] > maxValue) {
                currentScrollOffset = initialScrollOffset
            }
        }
        if (startScrollOffset != currentScrollOffset) {
            if (isHorizontalMode()) {
                onScrollChanged(currentScrollOffset, 0, startScrollOffset, 0)
            } else {
                onScrollChanged(0, currentScrollOffset, 0, startScrollOffset)
            }
        }
    }

    fun setDividerColorResource(@ColorRes colorId: Int) {
        dividerColor = ContextCompat.getColor(context, colorId)
    }

    fun setDividerDistanceResource(@DimenRes dimenId: Int) {
        dividerDistance = resources.getDimensionPixelSize(dimenId)
    }

    fun setDividerThicknessResource(@DimenRes dimenId: Int) {
        dividerThickness = resources.getDimensionPixelSize(dimenId)
    }

    override fun setOrientation(@Orientation orientation: Int) {
        this.orientation = orientation
        setWidthAndHeight()
    }

    fun setWheelItemCount(count: Int) {
        require(count >= 1) { "Wheel item count must be >= 1" }
        realWheelItemCount = count
        wheelItemCount = if (count < DEFAULT_WHEEL_ITEM_COUNT) DEFAULT_WHEEL_ITEM_COUNT else count
        wheelMiddleItemIndex = wheelItemCount / 2
        selectorIndices = IntArray(wheelItemCount)
    }

    fun setFormatter(formatter: String) {
        if (TextUtils.isEmpty(formatter)) {
            return
        }
        this@NumberPicker.formatter = stringToFormatter(formatter)
    }

    fun setFormatter(@StringRes stringId: Int) {
        setFormatter(resources.getString(stringId))
    }

    fun setFadingEdgeStrength(strength: Float) {
        fadingEdgeStrength = strength
    }

    fun setSelectedTextColor(@ColorInt color: Int) {
        selectedTextColor = color
        selectedText.setTextColor(selectedTextColor)
    }

    fun setSelectedTextColorResource(@ColorRes colorId: Int) {
        setSelectedTextColor(ContextCompat.getColor(context, colorId))
    }

    fun setSelectedTextSize(textSize: Float) {
        selectedTextSize = textSize
        selectedText.textSize = pxToSp(selectedTextSize)
    }

    fun setSelectedTextSize(@DimenRes dimenId: Int) {
        setSelectedTextSize(resources.getDimension(dimenId))
    }

    fun setSelectedTextStrikeThru(strikeThruText: Boolean) {
        selectedTextStrikeThrough = strikeThruText
    }

    fun setSelectedTextUnderline(underlineText: Boolean) {
        selectedTextUnderline = underlineText
    }

    fun setTextAlign(@Align align: Int) {
        textAlign = align
    }

    fun setTextColor(@ColorInt color: Int) {
        textColor = color
        selectorWheelPaint.color = textColor
    }

    fun setTextColorResource(@ColorRes colorId: Int) {
        setTextColor(ContextCompat.getColor(context, colorId))
    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
        selectorWheelPaint.textSize = this.textSize
    }

    fun setTextSize(@DimenRes dimenId: Int) {
        setTextSize(resources.getDimension(dimenId))
    }

    fun setTextStrikeThru(strikeThruText: Boolean) {
        textStrikeThrough = strikeThruText
    }

    fun setTextUnderline(underlineText: Boolean) {
        textUnderline = underlineText
    }

    fun setTypeface(typeface: Typeface) {
        this.typeface = typeface
        selectedText.typeface = this.typeface
        selectorWheelPaint.typeface = this.typeface
    }

    fun setTypeface(string: String?, style: Int) {
        if (TextUtils.isEmpty(string)) {
            return
        }
        setTypeface(Typeface.create(string, style))
    }

    fun setTypeface(string: String?) {
        setTypeface(string, Typeface.NORMAL)
    }

    fun setTypeface(@StringRes stringId: Int, style: Int) {
        setTypeface(resources.getString(stringId), style)
    }

    fun setTypeface(@StringRes stringId: Int) {
        setTypeface(stringId, Typeface.NORMAL)
    }

    fun setLineSpacingMultiplier(multiplier: Float) {
        lineSpacingMultiplier = multiplier
    }

    fun setMaxFlingVelocityCoefficient(coefficient: Int) {
        maxFlingVelocityCoefficient = coefficient
        maximumFlingVelocity = (
            viewConfiguration.scaledMaximumFlingVelocity /
                maxFlingVelocityCoefficient
            )
    }

    fun isHorizontalMode(): Boolean {
        return orientation == HORIZONTAL
    }

    fun isAscendingOrder(): Boolean {
        return order == ASCENDING
    }

    override fun getTopFadingEdgeStrength(): Float =
        getFadingEdgeStrength(!isHorizontalMode())

    override fun getBottomFadingEdgeStrength(): Float =
        getFadingEdgeStrength(!isHorizontalMode())

    override fun getLeftFadingEdgeStrength(): Float =
        getFadingEdgeStrength(isHorizontalMode())

    override fun getRightFadingEdgeStrength(): Float =
        getFadingEdgeStrength(isHorizontalMode())

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeAllCallbacks()
    }

    @CallSuper
    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val selectionDivider = pickerDividerDrawable
        if (selectionDivider != null && selectionDivider.isStateful &&
            selectionDivider.setState(drawableState)
        ) {
            invalidateDrawable(selectionDivider)
        }
    }

    @CallSuper
    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        pickerDividerDrawable?.jumpToCurrentState()
    }

    override fun getOrientation(): Int {
        return orientation
    }

    override fun computeHorizontalScrollOffset(): Int {
        return computeScrollOffset(isHorizontalMode())
    }

    override fun computeHorizontalScrollRange(): Int {
        return computeScrollRange(isHorizontalMode())
    }

    override fun computeHorizontalScrollExtent(): Int {
        return computeScrollExtent(isHorizontalMode())
    }

    override fun computeVerticalScrollOffset(): Int {
        return computeScrollOffset(!isHorizontalMode())
    }

    override fun computeVerticalScrollRange(): Int {
        return computeScrollRange(!isHorizontalMode())
    }

    override fun computeVerticalScrollExtent(): Int {
        return computeScrollExtent(isHorizontalMode())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        numberFormatter = NumberFormat.getInstance()
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        clickListener = onClickListener
    }

    fun addOnValueChangedListener(onValueChangedListener: OnValueChangeListener?) {
        onValueChangedListener?.let { valueChangeListeners.add(it) }
    }

    fun removeOnValueChangedListener(onValueChangedListener: OnValueChangeListener?) {
        valueChangeListeners.remove(onValueChangedListener)
    }

    fun clearOnValueChangedListeners() {
        valueChangeListeners.clear()
    }

    fun setOnScrollListener(onScrollListener: OnScrollListener?) {
        scrollListener = onScrollListener
    }

    fun setOnLongPressUpdateInterval(intervalMillis: Long) {
        longPressUpdateInterval = intervalMillis
    }

    fun setDisplayedValues(displayedValues: List<String>) {
        if (this.displayedValues == displayedValues) {
            return
        }
        this.displayedValues = displayedValues.toList()
        if (this.displayedValues.isNotEmpty()) {
            // Allow text entry rather than strictly numeric entry.
            selectedText.setRawInputType(
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            )
        } else {
            selectedText.setRawInputType(InputType.TYPE_CLASS_NUMBER)
        }
        updateInputTextView()
        initializeSelectorWheelIndices()
        tryComputeMaxWidth()
    }

    fun getWrapSelectorWheel(): Boolean {
        return wrapSelectorWheel
    }

    fun setWrapSelectorWheel(wrapSelectorWheel: Boolean) {
        wrapSelectorWheelPreferred = wrapSelectorWheel
        updateWrapSelectorWheel()
    }

    private fun computeScrollOffset(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) currentScrollOffset else 0
    }

    private fun computeScrollRange(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) (maxValue - minValue + 1) * selectorElementSize else 0
    }

    private fun computeScrollExtent(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) width else height
    }

    private fun getPaintCenterY(fontMetrics: FontMetrics?): Float =
        fontMetrics?.let { abs(it.top + it.bottom) / 2 } ?: 0f

    private fun tryComputeMaxWidth() {
        if (!computeMaxWidth) {
            return
        }
        selectorWheelPaint.textSize = textMaxSize
        var maxTextWidth = 0
        if (displayedValues.isEmpty()) {
            var maxDigitWidth = 0f
            for (i in 0..9) {
                val digitWidth = selectorWheelPaint.measureText(formatNumber(i))
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth
                }
            }
            var numberOfDigits = 0
            var current = maxValue
            while (current > 0) {
                numberOfDigits++
                current /= 10
            }
            maxTextWidth = (numberOfDigits * maxDigitWidth).toInt()
        } else {
            val valueCount = displayedValues.size
            for (i in 0 until valueCount) {
                val textWidth = selectorWheelPaint.measureText(displayedValues[i])
                if (textWidth > maxTextWidth) {
                    maxTextWidth = textWidth.toInt()
                }
            }
        }
        maxTextWidth += selectedText.paddingLeft + selectedText.paddingRight
        if (maxWidth != maxTextWidth) {
            maxWidth = if (maxTextWidth > minWidth) {
                maxTextWidth
            } else {
                minWidth
            }
            invalidate()
        }
    }

    /**
     * Whether or not the selector wheel should be wrapped is determined by user choice and whether
     * the choice is allowed. The former comes from [.setWrapSelectorWheel], the
     * latter is calculated based on min & max weight set vs selector's visual length. Therefore,
     * this method should be called any time any of the 3 values (i.e. user choice, min and max
     * weight) gets updated.
     */
    private fun updateWrapSelectorWheel() {
        val wrappingAllowed = maxValue - minValue >= selectorIndices.size
        wrapSelectorWheel = wrappingAllowed && wrapSelectorWheelPreferred
    }

    private fun getFadingEdgeStrength(isHorizontalMode: Boolean): Float =
        if (isHorizontalMode && fadingEdgeEnabled) fadingEdgeStrength else 0f

    private fun drawText(text: String, x: Float, y: Float, paint: Paint, canvas: Canvas) {
        var localY = y
        if (text.contains("\n")) {
            val lines = text.split("\n").toTypedArray()
            val height = (
                abs(paint.descent() + paint.ascent()) *
                    lineSpacingMultiplier
                )
            val diff = (lines.size - 1) * height / 2
            localY -= diff
            for (line in lines) {
                canvas.drawText(line, x, localY, paint)
                localY += height
            }
        } else {
            canvas.drawText(text, x, localY, paint)
        }
    }

    /**
     * Makes a measure spec that tries greedily to use the max weight.
     *
     * @param measureSpec The measure spec.
     * @param maxSize     The max weight for the size.
     * @return A measure spec greedily imposing the max size.
     */
    private fun makeMeasureSpec(measureSpec: Int, maxSize: Int): Int =
        if (maxSize == SIZE_UNSPECIFIED) {
            measureSpec
        } else when (val mode = MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY -> measureSpec
            MeasureSpec.AT_MOST -> MeasureSpec.makeMeasureSpec(
                min(MeasureSpec.getSize(measureSpec), maxSize),
                MeasureSpec.EXACTLY
            )
            MeasureSpec.UNSPECIFIED -> MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY)
            else -> throw IllegalArgumentException("Unknown measure mode: $mode")
        }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a MeasureSpec. Tries to respect the min size, unless a different size
     * is imposed by the constraints.
     *
     * @param minSize      The minimal desired size.
     * @param measuredSize The currently measured size.
     * @param measureSpec  The current measure spec.
     * @return The resolved size and state.
     */
    private fun resolveSizeAndStateRespectingMinSize(
        minSize: Int,
        measuredSize: Int,
        measureSpec: Int
    ): Int =
        if (minSize != SIZE_UNSPECIFIED) {
            pickerResolveSizeAndState(
                size = max(minSize, measuredSize),
                measureSpec = measureSpec,
                childMeasuredState = 0
            )
        } else {
            measuredSize
        }

    /**
     * Resets the selector indices and clear the cached string representation of
     * these indices.
     */
    private fun initializeSelectorWheelIndices() {
        selectorIndexToStringCache.clear()
        val selectorIndices = getSelectorIndices()
        for (i in this.selectorIndices.indices) {
            var selectorIndex = pickerValue + (i - wheelMiddleItemIndex)
            if (wrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex)
            }
            selectorIndices[i] = selectorIndex
            ensureCachedScrollSelectorValue(selectorIndices[i])
        }
    }

    /**
     * Updates the accessibility values of the view,
     * to the currently selected weight
     */
    private fun updateAccessibilityDescription() {
        this.contentDescription = pickerValue.toString()
    }

    /**
     * Changes the current weight by one which is increment or
     * decrement based on the passes argument.
     * decrement the current weight.
     *
     * @param increment True to increment, false to decrement.
     */
    private fun changeValueByOne(increment: Boolean) {
        if (!moveToFinalScrollerPosition(flingScroller)) {
            moveToFinalScrollerPosition(adjustScroller)
        }
        if (isHorizontalMode()) {
            previousScrollerX = 0
            if (increment) {
                flingScroller.startScroll(0, 0, -selectorElementSize, 0, SNAP_SCROLL_DURATION)
            } else {
                flingScroller.startScroll(0, 0, selectorElementSize, 0, SNAP_SCROLL_DURATION)
            }
        } else {
            previousScrollerY = 0
            if (increment) {
                flingScroller.startScroll(0, 0, 0, -selectorElementSize, SNAP_SCROLL_DURATION)
            } else {
                flingScroller.startScroll(0, 0, 0, selectorElementSize, SNAP_SCROLL_DURATION)
            }
        }
        invalidate()
    }

    private fun initializeSelectorWheel() {
        initializeSelectorWheelIndices()
        val selectorIndices = getSelectorIndices()
        val totalTextSize = (
            (selectorIndices.size - 1) * textSize.toInt() +
                selectedTextSize.toInt()
            )
        val textGapCount = selectorIndices.size.toFloat()
        if (isHorizontalMode()) {
            val totalTextGapWidth = (right - left - totalTextSize).toFloat()
            selectorTextGapWidth = (totalTextGapWidth / textGapCount).toInt()
            selectorElementSize = textMaxSize.toInt() + selectorTextGapWidth
            initialScrollOffset =
                selectedTextCenterX.toInt() - selectorElementSize * wheelMiddleItemIndex
        } else {
            val totalTextGapHeight = (bottom - top - totalTextSize).toFloat()
            selectorTextGapHeight = (totalTextGapHeight / textGapCount).toInt()
            selectorElementSize = textMaxSize.toInt() + selectorTextGapHeight
            initialScrollOffset =
                selectedTextCenterY.toInt() - selectorElementSize * wheelMiddleItemIndex
        }
        currentScrollOffset = initialScrollOffset
        updateInputTextView()
    }

    private fun initializeFadingEdges() {
        if (isHorizontalMode()) {
            isHorizontalFadingEdgeEnabled = true
            setFadingEdgeLength((right - left - textSize.toInt()) / 2)
        } else {
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength((bottom - top - textSize.toInt()) / 2)
        }
    }

    private fun onScrollerFinished(scroller: Scroller) {
        if (scroller === flingScroller) {
            ensureScrollWheelAdjusted()
            updateInputTextView()
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
        } else if (scrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            updateInputTextView()
        }
    }

    private fun onScrollStateChange(scrollState: Int) {
        if (this.scrollState == scrollState) {
            return
        }
        if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            selectedText.clearFocus()
        }
        this.scrollState = scrollState
        scrollListener?.onScrollStateChange(this, scrollState)
    }

    private fun fling(velocity: Int) {
        if (isHorizontalMode()) {
            previousScrollerX = 0
            if (velocity > 0) {
                flingScroller.fling(
                    startX = 0,
                    startY = 0,
                    velocityX = velocity,
                    velocityY = 0,
                    minX = 0,
                    maxX = Int.MAX_VALUE,
                    minY = 0,
                    maxY = 0
                )
            } else {
                flingScroller.fling(
                    startX = Int.MAX_VALUE,
                    startY = 0,
                    velocityX = velocity,
                    velocityY = 0,
                    minX = 0,
                    maxX = Int.MAX_VALUE,
                    minY = 0,
                    maxY = 0
                )
            }
        } else {
            previousScrollerY = 0
            if (velocity > 0) {
                flingScroller.fling(
                    startX = 0,
                    startY = 0,
                    velocityX = 0,
                    velocityY = velocity,
                    minX = 0,
                    maxX = 0,
                    minY = 0,
                    maxY = Int.MAX_VALUE
                )
            } else {
                flingScroller.fling(
                    startX = 0,
                    startY = Int.MAX_VALUE,
                    velocityX = 0,
                    velocityY = velocity,
                    minX = 0,
                    maxX = 0,
                    minY = 0,
                    maxY = Int.MAX_VALUE
                )
            }
        }
        invalidate()
    }

    private fun getWrappedSelectorIndex(selectorIndex: Int): Int {
        if (selectorIndex > maxValue) {
            return minValue + (selectorIndex - maxValue) % (maxValue - minValue) - 1
        } else if (selectorIndex < minValue) {
            return maxValue - (minValue - selectorIndex) % (maxValue - minValue) + 1
        }
        return selectorIndex
    }

    private fun getSelectorIndices(): IntArray {
        return selectorIndices
    }

    private fun incrementSelectorIndices(selectorIndices: IntArray) {
        for (i in 0 until selectorIndices.size - 1) {
            selectorIndices[i] = selectorIndices[i + 1]
        }
        var nextScrollSelectorIndex = selectorIndices[selectorIndices.size - 2] + 1
        if (wrapSelectorWheel && nextScrollSelectorIndex > maxValue) {
            nextScrollSelectorIndex = minValue
        }
        selectorIndices[selectorIndices.size - 1] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    private fun decrementSelectorIndices(selectorIndices: IntArray) {
        for (i in selectorIndices.size - 1 downTo 1) {
            selectorIndices[i] = selectorIndices[i - 1]
        }
        var nextScrollSelectorIndex = selectorIndices[1] - 1
        if (wrapSelectorWheel && nextScrollSelectorIndex < minValue) {
            nextScrollSelectorIndex = maxValue
        }
        selectorIndices[0] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    private fun ensureCachedScrollSelectorValue(selectorIndex: Int) {
        val cache = selectorIndexToStringCache
        var scrollSelectorValue = cache[selectorIndex]
        if (scrollSelectorValue != null) {
            return
        }
        scrollSelectorValue = if (selectorIndex < minValue || selectorIndex > maxValue) {
            ""
        } else {
            if (displayedValues.isNotEmpty()) {
                val displayedValueIndex = selectorIndex - minValue
                displayedValues[displayedValueIndex]
            } else {
                formatNumber(selectorIndex)
            }
        }
        cache.put(selectorIndex, scrollSelectorValue)
    }

    private fun formatNumber(value: Int): String =
        formatter?.format(value) ?: formatNumberWithLocale(value)

    private fun updateInputTextView(): Boolean {
        /*
         * If we don't have displayed values then use the current number else
         * find the correct weight in the displayed values for the current
         * number.
         */
        val text = if (displayedValues.isNotEmpty()) {
            displayedValues.getOrNull(pickerValue - minValue)
        } else {
            null
        }
        if (!TextUtils.isEmpty(text)) {
            val beforeText: CharSequence = selectedText.text
            if (text != beforeText.toString()) {
                selectedText.setText(text)
                return true
            }
        }
        return false
    }

    private fun notifyChange(previous: Int, current: Int) {
        if (valueChangeListeners.isNotEmpty()) {
            for (listener in valueChangeListeners) listener.onValueChange(this, previous, current)
        }
    }

    private fun postChangeCurrentByOneFromLongPress(
        increment: Boolean,
        delayMillis: Long = ViewConfiguration.getLongPressTimeout()
            .toLong()
    ) {
        if (changeCurrentByOneFromLongPressCommand == null) {
            changeCurrentByOneFromLongPressCommand = ChangeCurrentByOneFromLongPressCommand()
        } else {
            removeCallbacks(changeCurrentByOneFromLongPressCommand)
        }
        changeCurrentByOneFromLongPressCommand!!.setStep(increment)
        postDelayed(changeCurrentByOneFromLongPressCommand, delayMillis)
    }

    private fun removeChangeCurrentByOneFromLongPress() {
        changeCurrentByOneFromLongPressCommand?.let { removeCallbacks(it) }
    }

    private fun removeAllCallbacks() {
        changeCurrentByOneFromLongPressCommand?.let { removeCallbacks(it) }
        setSelectionCommand?.cancel()
    }

    /**
     * @return The selected index given its displayed `weight`.
     */
    private fun getSelectedPos(value: String): Int {
        if (displayedValues.isEmpty()) {
            runCatching { value.toInt() }.onSuccess { return it }
        } else {
            displayedValues.forEachIndexed { index, displayValue ->
                // Don't force the user to type in jan when ja will do
                if (displayValue.lowercase().startsWith(value.lowercase())) {
                    return minValue + index
                }
            }

            /*
             * The user might have typed in a number into the month field i.e.
             * 10 instead of OCT so support that too.
             */
            runCatching { value.toInt() }.onSuccess { return it }
        }
        return minValue
    }

    private fun postSetSelectionCommand(selectionStart: Int, selectionEnd: Int) {
        if (setSelectionCommand == null) {
            setSelectionCommand = SetSelectionCommand(selectedText)
        } else {
            setSelectionCommand!!.post(selectionStart, selectionEnd)
        }
    }

    private fun ensureScrollWheelAdjusted(): Boolean {
        // adjust to the closest weight
        var delta = initialScrollOffset - currentScrollOffset
        if (delta != 0) {
            if (abs(delta) > selectorElementSize / 2) {
                delta += if (delta > 0) -selectorElementSize else selectorElementSize
            }
            if (isHorizontalMode()) {
                previousScrollerX = 0
                adjustScroller.startScroll(0, 0, delta, 0, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
            } else {
                previousScrollerY = 0
                adjustScroller.startScroll(0, 0, 0, delta, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
            }
            invalidate()
            return true
        }
        return false
    }

    private fun formatNumberWithLocale(value: Int): String {
        return numberFormatter.format(value.toLong())
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun pxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }

    private fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            resources.displayMetrics
        )
    }

    private fun pxToSp(px: Float): Float {
        return px / resources.displayMetrics.scaledDensity
    }

    private fun stringToFormatter(formatter: String): Formatter? {
        return if (TextUtils.isEmpty(formatter)) {
            null
        } else object : Formatter {
            override fun format(value: Int): String {
                return String.format(Locale.getDefault(), formatter, value)
            }
        }
    }

    private fun setWidthAndHeight() {
        if (isHorizontalMode()) {
            minHeight = SIZE_UNSPECIFIED
            maxHeight = dpToPx(DEFAULT_MIN_WIDTH.toFloat()).toInt()
            minWidth = dpToPx(DEFAULT_MAX_HEIGHT.toFloat()).toInt()
            maxWidth = SIZE_UNSPECIFIED
        } else {
            minHeight = SIZE_UNSPECIFIED
            maxHeight = dpToPx(DEFAULT_MAX_HEIGHT.toFloat()).toInt()
            minWidth = dpToPx(DEFAULT_MIN_WIDTH.toFloat()).toInt()
            maxWidth = SIZE_UNSPECIFIED
        }
    }

    private fun pickerResolveSizeAndState(
        size: Int,
        measureSpec: Int,
        childMeasuredState: Int
    ): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        val result = when (specMode) {
            MeasureSpec.UNSPECIFIED -> size
            MeasureSpec.AT_MOST -> if (specSize < size) {
                specSize or MEASURED_STATE_TOO_SMALL
            } else {
                size
            }
            MeasureSpec.EXACTLY -> specSize
            else -> size
        }
        return result or (childMeasuredState and MEASURED_STATE_MASK)
    }

    /**
     * Filter for accepting only valid indices or prefixes of the string
     * representation of valid indices.
     */
    internal inner class InputTextFilter : NumberKeyListener() {
        // XXX This doesn't allow for range limits when controlled by a soft input method!
        override fun getInputType(): Int {
            return InputType.TYPE_CLASS_TEXT
        }

        override fun getAcceptedChars(): CharArray {
            return DIGIT_CHARACTERS
        }

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence {
            // We don't know what the output will be, so always cancel any
            // pending set selection command.
            if (setSelectionCommand != null) {
                setSelectionCommand!!.cancel()
            }
            return if (displayedValues.isEmpty()) {
                val filtered = super.filter(source, start, end, dest, dstart, dend)
                    ?: source.subSequence(start, end)
                val result = (
                    dest.subSequence(0, dstart).toString() + filtered +
                        dest.subSequence(dend, dest.length)
                    )
                if (result.isBlank()) {
                    return result
                }
                val localValue = getSelectedPos(result)

                /*
                 * Ensure the user can't type in a weight greater than the max
                 * allowed. We have to allow less than min as the user might
                 * want to delete some numbers and then type a new number.
                 * And prevent multiple-"0" that exceeds the length of upper
                 * bound number.
                 */if (localValue > maxValue || result.length > maxValue.toString().length) {
                    ""
                } else {
                    filtered
                }
            } else {
                val filtered: CharSequence = source.subSequence(start, end).toString()
                if (TextUtils.isEmpty(filtered)) {
                    return ""
                }
                val result = (
                    dest.subSequence(0, dstart).toString() + filtered +
                        dest.subSequence(dend, dest.length)
                    )
                displayedValues.forEach { displayedValue ->
                    if (displayedValue.lowercase(Locale.getDefault())
                        .startsWith(result.lowercase())
                    ) {
                        postSetSelectionCommand(result.length, displayedValue.length)
                        return displayedValue.subSequence(dstart, displayedValue.length)
                    }
                }
                ""
            }
        }
    }

    /**
     * Command for changing the current weight from a long press by one.
     */
    internal inner class ChangeCurrentByOneFromLongPressCommand : Runnable {
        private var mIncrement = false
        fun setStep(increment: Boolean) {
            mIncrement = increment
        }

        override fun run() {
            changeValueByOne(mIncrement)
            postDelayed(this, longPressUpdateInterval)
        }
    }

    /**
     * Command for setting the input text selection.
     */
    private class SetSelectionCommand(private val mInputText: EditText) : Runnable {
        private var mSelectionStart = 0
        private var mSelectionEnd = 0

        /**
         * Whether this runnable is currently posted.
         */
        private var mPosted = false
        fun post(selectionStart: Int, selectionEnd: Int) {
            mSelectionStart = selectionStart
            mSelectionEnd = selectionEnd
            if (!mPosted) {
                mInputText.post(this)
                mPosted = true
            }
        }

        fun cancel() {
            if (mPosted) {
                mInputText.removeCallbacks(this)
                mPosted = false
            }
        }

        override fun run() {
            mPosted = false
            mInputText.setSelection(mSelectionStart, mSelectionEnd)
        }
    }

    /**
     * Use a custom NumberPicker formatting callback to use two-digit minutes
     * strings like "01". Keeping a static formatter etc. is the most efficient
     * way to do this; it avoids creating temporary objects on every call to
     * format().
     */
    private class TwoDigitFormatter : Formatter {
        val stringBuilder = StringBuilder()
        val formatter: java.util.Formatter by lazy {
            Formatter(
                stringBuilder,
                Locale.getDefault()
            )
        }
        val args = arrayOfNulls<Any>(1)

        override fun format(value: Int): String {
            args[0] = value
            stringBuilder.delete(0, stringBuilder.length)
            return formatter.format("%02d", *args).toString()
        }
    }

    interface OnValueChangeListener {
        fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int)
    }

    interface OnScrollListener {
        @IntDef(SCROLL_STATE_IDLE, SCROLL_STATE_TOUCH_SCROLL, SCROLL_STATE_FLING)
        annotation class ScrollState

        fun onScrollStateChange(view: NumberPicker?, @ScrollState scrollState: Int)

        companion object {
            const val SCROLL_STATE_IDLE = 0
            const val SCROLL_STATE_TOUCH_SCROLL = 1
            const val SCROLL_STATE_FLING = 2
        }
    }

    interface Formatter {
        fun format(value: Int): String
    }
}
