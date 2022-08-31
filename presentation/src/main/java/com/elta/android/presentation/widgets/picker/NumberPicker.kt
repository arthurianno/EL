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
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.elta.android.presentation.R
import java.text.NumberFormat
import java.util.Formatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

    var minValue = DEFAULT_MIN_VALUE
        set(value) {
            field = value
            if (value > mValue) {
                mValue = value
            }
            val wrapSelectorWheel = maxValue - minValue > mSelectorIndices.size
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
            if (value < mValue) {
                mValue = value
            }
            updateWrapSelectorWheel()
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }
    private var selectedText: EditText = EditText(context)
    private var selectedTextCenterX = 0f
    private var selectedTextCenterY = 0f
    private var minHeight = 0
    private var maxHeight = 0
    private var minWidth = 0

    private var maxWidth = 0
    private var computeMaxWidth: Boolean = true

    @Align
    private var selectedTextAlign = DEFAULT_TEXT_ALIGN
    private var selectedTextColor = DEFAULT_TEXT_COLOR
    private var selectedTextSize = DEFAULT_TEXT_SIZE
    private var selectedTextStrikeThrough: Boolean = false
    private var selectedTextUnderline: Boolean = false
    private var textAlign = DEFAULT_TEXT_ALIGN
    private var textColor = DEFAULT_TEXT_COLOR
    private var textSize = DEFAULT_TEXT_SIZE
    private var textStrikeThrough: Boolean = false
    private var textUnderline: Boolean = false
    private var typeface: Typeface = Typeface.DEFAULT
    private var selectorTextGapWidth = 0
    private var selectorTextGapHeight = 0
    private var mDisplayedValues: List<String> = emptyList()
    private var mValue: Int = 0
    private var clickListener: OnClickListener? = null
    private val valueChangeListeners: MutableList<OnValueChangeListener> = ArrayList()
    private var scrollListener: OnScrollListener? = null
    private var mFormatter: Formatter?
    private var longPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL
    private val mSelectorIndexToStringCache = SparseArray<String>()
    private var mWheelItemCount = DEFAULT_WHEEL_ITEM_COUNT
    private var mRealWheelItemCount = DEFAULT_WHEEL_ITEM_COUNT
    private var mWheelMiddleItemIndex = mWheelItemCount / 2
    private var mSelectorIndices = IntArray(mWheelItemCount)
    private var selectorWheelPaint: Paint = Paint()
    private var mSelectorElementSize = 0
    private var mInitialScrollOffset = Int.MIN_VALUE
    private var mCurrentScrollOffset = 0
    private val mFlingScroller: Scroller
    private val mAdjustScroller: Scroller
    private var mPreviousScrollerX = 0
    private var mPreviousScrollerY = 0
    private var mSetSelectionCommand: SetSelectionCommand? = null
    private var mChangeCurrentByOneFromLongPressCommand: ChangeCurrentByOneFromLongPressCommand? =
        null
    private var mLastDownEventX = 0f
    private var mLastDownEventY = 0f
    private var mLastDownOrMoveEventX = 0f
    private var mLastDownOrMoveEventY = 0f
    private var mVelocityTracker: VelocityTracker? = null
    private val mTouchSlop: Int
    private val mMinimumFlingVelocity: Int
    private var mMaximumFlingVelocity: Int
    private var mWrapSelectorWheel: Boolean = true
    private var mWrapSelectorWheelPreferred = true
    private var mDividerDrawable: Drawable? = null
    private var dividerColor = DEFAULT_DIVIDER_COLOR
    private var mDividerDistance: Int
    private var mDividerThickness: Int
    private var mTopDividerTop = 0
    private var mBottomDividerBottom = 0
    private var mLeftDividerLeft = 0
    private var mRightDividerRight = 0
    private var mScrollState = OnScrollListener.SCROLL_STATE_IDLE
    private var mLastHandledDownDpadKeyCode = -1
    private val mHideWheelUntilFocused: Boolean
    private val mWidth: Float
    private val mHeight: Float
    private var mOrientation: Int
    private var order: Int = ASCENDING
    private var fadingEdgeEnabled = true
    private var mFadingEdgeStrength = DEFAULT_FADING_EDGE_STRENGTH
    private var scrollerEnabled = true
    private var mLineSpacingMultiplier = DEFAULT_LINE_SPACING_MULTIPLIER
    private var mMaxFlingVelocityCoefficient = DEFAULT_MAX_FLING_VELOCITY_COEFFICIENT
    private var mNumberFormatter: NumberFormat
    private val mViewConfiguration: ViewConfiguration

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
            val dividerDistance = 2 * mDividerThickness + mDividerDistance
            if (isHorizontalMode()) {
                mLeftDividerLeft = (width - mDividerDistance) / 2 - mDividerThickness
                mRightDividerRight = mLeftDividerLeft + dividerDistance
            } else {
                mTopDividerTop = (height - mDividerDistance) / 2 - mDividerThickness
                mBottomDividerBottom = mTopDividerTop + dividerDistance
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
            var amountToScroll = scroller.getFinalX() - scroller.getCurrX()
            val futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementSize
            var overshootAdjustment = mInitialScrollOffset - futureScrollOffset
            if (overshootAdjustment != 0) {
                if (Math.abs(overshootAdjustment) > mSelectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= mSelectorElementSize
                    } else {
                        overshootAdjustment += mSelectorElementSize
                    }
                }
                amountToScroll += overshootAdjustment
                scrollBy(amountToScroll, 0)
                return true
            }
        } else {
            var amountToScroll = scroller.getFinalY() - scroller.getCurrY()
            val futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementSize
            var overshootAdjustment = mInitialScrollOffset - futureScrollOffset
            if (overshootAdjustment != 0) {
                if (Math.abs(overshootAdjustment) > mSelectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= mSelectorElementSize
                    } else {
                        overshootAdjustment += mSelectorElementSize
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
                    mLastDownEventX = event.x
                    mLastDownOrMoveEventX = mLastDownEventX
                    when {
                        !mFlingScroller.isFinished() -> {
                            mFlingScroller.forceFinished(true)
                            mAdjustScroller.forceFinished(true)
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                        }
                        !mAdjustScroller.isFinished() -> {
                            mFlingScroller.forceFinished(true)
                            mAdjustScroller.forceFinished(true)
                        }
                        mLastDownEventX >= mLeftDividerLeft &&
                            mLastDownEventX <= mRightDividerRight -> clickListener?.onClick(this)
                        mLastDownEventX < mLeftDividerLeft ->
                            postChangeCurrentByOneFromLongPress(false)
                        mLastDownEventX > mRightDividerRight ->
                            postChangeCurrentByOneFromLongPress(true)
                    }
                } else {
                    mLastDownEventY = event.y
                    mLastDownOrMoveEventY = mLastDownEventY
                    when {
                        !mFlingScroller.isFinished() -> {
                            mFlingScroller.forceFinished(true)
                            mAdjustScroller.forceFinished(true)
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                        }
                        !mAdjustScroller.isFinished() -> {
                            mFlingScroller.forceFinished(true)
                            mAdjustScroller.forceFinished(true)
                        }
                        mLastDownEventY >= mTopDividerTop &&
                            mLastDownEventY <= mBottomDividerBottom ->
                            clickListener?.onClick(this)
                        mLastDownEventY < mTopDividerTop ->
                            postChangeCurrentByOneFromLongPress(false)
                        mLastDownEventY > mBottomDividerBottom ->
                            postChangeCurrentByOneFromLongPress(true)
                    }
                }
                return true
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        if (!scrollerEnabled) {
            return false
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {
                if (isHorizontalMode()) {
                    val currentMoveX = event.x
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        val deltaDownX = abs(currentMoveX - mLastDownEventX).toInt()
                        if (deltaDownX > mTouchSlop) {
                            removeAllCallbacks()
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                    } else {
                        val deltaMoveX = (currentMoveX - mLastDownOrMoveEventX).toInt()
                        scrollBy(deltaMoveX, 0)
                        invalidate()
                    }
                    mLastDownOrMoveEventX = currentMoveX
                } else {
                    val currentMoveY = event.y
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        val deltaDownY = abs(currentMoveY - mLastDownEventY).toInt()
                        if (deltaDownY > mTouchSlop) {
                            removeAllCallbacks()
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                    } else {
                        val deltaMoveY = (currentMoveY - mLastDownOrMoveEventY).toInt()
                        scrollBy(0, deltaMoveY)
                        invalidate()
                    }
                    mLastDownOrMoveEventY = currentMoveY
                }
            }
            MotionEvent.ACTION_UP -> {
                removeChangeCurrentByOneFromLongPress()
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, mMaximumFlingVelocity.toFloat())
                if (isHorizontalMode()) {
                    val initialVelocity = velocityTracker.xVelocity.toInt()
                    if (abs(initialVelocity) > mMinimumFlingVelocity) {
                        fling(initialVelocity)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    } else {
                        val eventX = event.x.toInt()
                        val deltaMoveX = abs(eventX - mLastDownEventX).toInt()
                        if (deltaMoveX <= mTouchSlop) {
                            val selectorIndexOffset = (
                                eventX / mSelectorElementSize -
                                    mWheelMiddleItemIndex
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
                    if (abs(initialVelocity) > mMinimumFlingVelocity) {
                        fling(initialVelocity)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    } else {
                        val eventY = event.y.toInt()
                        val deltaMoveY = abs(eventY - mLastDownEventY).toInt()
                        if (deltaMoveY <= mTouchSlop) {
                            val selectorIndexOffset = (
                                eventY / mSelectorElementSize -
                                    mWheelMiddleItemIndex
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
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
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
                KeyEvent.ACTION_DOWN -> if (mWrapSelectorWheel || (if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) value < maxValue else value > minValue)) {
                    requestFocus()
                    mLastHandledDownDpadKeyCode = keyCode
                    removeAllCallbacks()
                    if (mFlingScroller.isFinished()) {
                        changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                    }
                    return true
                }
                KeyEvent.ACTION_UP -> if (mLastHandledDownDpadKeyCode == keyCode) {
                    mLastHandledDownDpadKeyCode = -1
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

    override fun computeScroll() {
        if (!scrollerEnabled) {
            return
        }
        var scroller = mFlingScroller
        if (scroller.isFinished()) {
            scroller = mAdjustScroller
            if (scroller.isFinished()) {
                return
            }
        }
        scroller.computeScrollOffset()
        if (isHorizontalMode()) {
            val currentScrollerX = scroller.getCurrX()
            if (mPreviousScrollerX == 0) {
                mPreviousScrollerX = scroller.getStartX()
            }
            scrollBy(currentScrollerX - mPreviousScrollerX, 0)
            mPreviousScrollerX = currentScrollerX
        } else {
            val currentScrollerY = scroller.getCurrY()
            if (mPreviousScrollerY == 0) {
                mPreviousScrollerY = scroller.getStartY()
            }
            scrollBy(0, currentScrollerY - mPreviousScrollerY)
            mPreviousScrollerY = currentScrollerY
        }
        if (scroller.isFinished()) {
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
        val startScrollOffset = mCurrentScrollOffset
        val gap: Int
        if (isHorizontalMode()) {
            if (isAscendingOrder()) {
                if (!mWrapSelectorWheel && x > 0 && selectorIndices[mWheelMiddleItemIndex] <= minValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
                if (!mWrapSelectorWheel && x < 0 && selectorIndices[mWheelMiddleItemIndex] >= maxValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
            } else {
                if (!mWrapSelectorWheel && x > 0 && selectorIndices[mWheelMiddleItemIndex] >= maxValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
                if (!mWrapSelectorWheel && x < 0 && selectorIndices[mWheelMiddleItemIndex] <= minValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
            }
            mCurrentScrollOffset += x
            gap = selectorTextGapWidth
        } else {
            if (isAscendingOrder()) {
                if (!mWrapSelectorWheel && y > 0 && selectorIndices[mWheelMiddleItemIndex] <= minValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
                if (!mWrapSelectorWheel && y < 0 && selectorIndices[mWheelMiddleItemIndex] >= maxValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
            } else {
                if (!mWrapSelectorWheel && y > 0 && selectorIndices[mWheelMiddleItemIndex] >= maxValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
                if (!mWrapSelectorWheel && y < 0 && selectorIndices[mWheelMiddleItemIndex] <= minValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
            }
            mCurrentScrollOffset += y
            gap = selectorTextGapHeight
        }
        while (mCurrentScrollOffset - mInitialScrollOffset > gap) {
            mCurrentScrollOffset -= mSelectorElementSize
            if (isAscendingOrder()) {
                decrementSelectorIndices(selectorIndices)
            } else {
                incrementSelectorIndices(selectorIndices)
            }
            setValueInternal(selectorIndices[mWheelMiddleItemIndex], true)
            if (!mWrapSelectorWheel && selectorIndices[mWheelMiddleItemIndex] < minValue) {
                mCurrentScrollOffset = mInitialScrollOffset
            }
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -gap) {
            mCurrentScrollOffset += mSelectorElementSize
            if (isAscendingOrder()) {
                incrementSelectorIndices(selectorIndices)
            } else {
                decrementSelectorIndices(selectorIndices)
            }
            setValueInternal(selectorIndices[mWheelMiddleItemIndex], true)
            if (!mWrapSelectorWheel && selectorIndices[mWheelMiddleItemIndex] > maxValue) {
                mCurrentScrollOffset = mInitialScrollOffset
            }
        }
        if (startScrollOffset != mCurrentScrollOffset) {
            if (isHorizontalMode()) {
                onScrollChanged(mCurrentScrollOffset, 0, startScrollOffset, 0)
            } else {
                onScrollChanged(0, mCurrentScrollOffset, 0, startScrollOffset)
            }
        }
    }

    private fun computeScrollOffset(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) mCurrentScrollOffset else 0
    }

    private fun computeScrollRange(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) (maxValue - minValue + 1) * mSelectorElementSize else 0
    }

    private fun computeScrollExtent(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) width else height
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
        mNumberFormatter = NumberFormat.getInstance()
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

    fun updateValue(value: Int) {
        setValueInternal(value, true)
    }

    private val maxTextSize: Float
        get() = max(textSize, selectedTextSize)

    private fun getPaintCenterY(fontMetrics: FontMetrics?): Float =
        fontMetrics?.let { abs(it.top + it.bottom) / 2 } ?: 0f

    private fun tryComputeMaxWidth() {
        if (!computeMaxWidth) {
            return
        }
        selectorWheelPaint.textSize = maxTextSize
        var maxTextWidth = 0
        if (mDisplayedValues.isEmpty()) {
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
            val valueCount = mDisplayedValues.size
            for (i in 0 until valueCount) {
                val textWidth = selectorWheelPaint.measureText(mDisplayedValues[i])
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

    fun getWrapSelectorWheel(): Boolean {
        return mWrapSelectorWheel
    }

    /**
     * Sets whether the selector wheel shown during flinging/scrolling should
     * wrap around the [NumberPicker.getMinValue] and
     * [NumberPicker.getMaxValue] values.
     *
     *
     * By default if the range (max - min) is more than the number of items shown
     * on the selector wheel the selector wheel wrapping is enabled.
     *
     *
     *
     * **Note:** If the number of items, i.e. the range (
     * [.getMaxValue] - [.getMinValue]) is less than
     * the number of items shown on the selector wheel, the selector wheel will
     * not wrap. Hence, in such a case calling this method is a NOP.
     *
     *
     * @param wrapSelectorWheel Whether to wrap.
     */
    fun setWrapSelectorWheel(wrapSelectorWheel: Boolean) {
        mWrapSelectorWheelPreferred = wrapSelectorWheel
        updateWrapSelectorWheel()
    }

    /**
     * Whether or not the selector wheel should be wrapped is determined by user choice and whether
     * the choice is allowed. The former comes from [.setWrapSelectorWheel], the
     * latter is calculated based on min & max weight set vs selector's visual length. Therefore,
     * this method should be called any time any of the 3 values (i.e. user choice, min and max
     * weight) gets updated.
     */
    private fun updateWrapSelectorWheel() {
        val wrappingAllowed = maxValue - minValue >= mSelectorIndices.size
        mWrapSelectorWheel = wrappingAllowed && mWrapSelectorWheelPreferred
    }

    fun setOnLongPressUpdateInterval(intervalMillis: Long) {
        longPressUpdateInterval = intervalMillis
    }

    /**
     * Returns the weight of the picker.
     *
     * @return The weight.
     *
     * Set the current weight for the number picker.
     *
     *
     * If the argument is less than the [NumberPicker.getMinValue] and
     * [NumberPicker.getWrapSelectorWheel] is `false` the
     * current weight is set to the [NumberPicker.getMinValue] weight.
     *
     *
     *
     * If the argument is less than the [NumberPicker.getMinValue] and
     * [NumberPicker.getWrapSelectorWheel] is `true` the
     * current weight is set to the [NumberPicker.getMaxValue] weight.
     *
     *
     *
     * If the argument is less than the [NumberPicker.getMaxValue] and
     * [NumberPicker.getWrapSelectorWheel] is `false` the
     * current weight is set to the [NumberPicker.getMaxValue] weight.
     *
     *
     *
     * If the argument is less than the [NumberPicker.getMaxValue] and
     * [NumberPicker.getWrapSelectorWheel] is `true` the
     * current weight is set to the [NumberPicker.getMinValue] weight.
     *
     *
     * @param value The current weight.
     * @see .setWrapSelectorWheel
     * @see .setMinValue
     * @see .setMaxValue
     */
    var value: Int
        get() = mValue
        set(value) {
            setValueInternal(value, false)
        }

    fun setDisplayedValues(displayedValues: List<String>) {
        if (mDisplayedValues == displayedValues) {
            return
        }
        mDisplayedValues = displayedValues.toList()
        if (mDisplayedValues.isNotEmpty()) {
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

    private fun getFadingEdgeStrength(isHorizontalMode: Boolean): Float =
        if (isHorizontalMode && fadingEdgeEnabled) mFadingEdgeStrength else 0f

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
        val selectionDivider = mDividerDrawable
        if (selectionDivider != null && selectionDivider.isStateful &&
            selectionDivider.setState(drawableState)
        ) {
            invalidateDrawable(selectionDivider)
        }
    }

    @CallSuper
    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        mDividerDrawable?.jumpToCurrentState()
    }

    override fun onDraw(canvas: Canvas) {
        // save canvas
        canvas.save()
        val showSelectorWheel = if (mHideWheelUntilFocused) hasFocus() else true
        var x: Float
        var y: Float
        if (isHorizontalMode()) {
            x = mCurrentScrollOffset.toFloat()
            y = (selectedText.baseline + selectedText.top).toFloat()
            if (mRealWheelItemCount < DEFAULT_WHEEL_ITEM_COUNT) {
                canvas.clipRect(mLeftDividerLeft, 0, mRightDividerRight, bottom)
            }
        } else {
            x = ((right - left) / 2).toFloat()
            y = mCurrentScrollOffset.toFloat()
            if (mRealWheelItemCount < DEFAULT_WHEEL_ITEM_COUNT) {
                canvas.clipRect(0, mTopDividerTop, right, mBottomDividerBottom)
            }
        }

        // draw the selector wheel
        val selectorIndices = getSelectorIndices()
        for (i in selectorIndices.indices) {
            if (i == mWheelMiddleItemIndex) {
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
            val scrollSelectorValue = mSelectorIndexToStringCache[selectorIndex]
            // Do not draw the middle item if input is visible since the input
            // is shown only if the wheel is static and it covers the middle
            // item. Otherwise, if the user starts editing the text via the
            // IME he may see a dimmed version of the old weight intermixed
            // with the new one.
            if (showSelectorWheel && i != mWheelMiddleItemIndex ||
                i == mWheelMiddleItemIndex && selectedText.visibility != VISIBLE
            ) {
                var textY = y
                if (!isHorizontalMode()) {
                    textY += getPaintCenterY(selectorWheelPaint.fontMetrics)
                }
                drawText(scrollSelectorValue, x, textY, selectorWheelPaint, canvas)
            }
            if (isHorizontalMode()) {
                x += mSelectorElementSize.toFloat()
            } else {
                y += mSelectorElementSize.toFloat()
            }
        }

        // restore canvas
        canvas.restore()

        // draw the dividers
        if (showSelectorWheel && mDividerDrawable != null) {
            if (isHorizontalMode()) {
                val bottom = bottom

                // draw the left divider
                val leftOfLeftDivider = mLeftDividerLeft
                val rightOfLeftDivider = leftOfLeftDivider + mDividerThickness
                mDividerDrawable!!.setBounds(leftOfLeftDivider, 0, rightOfLeftDivider, bottom)
                mDividerDrawable!!.draw(canvas)

                // draw the right divider
                val rightOfRightDivider = mRightDividerRight
                val leftOfRightDivider = rightOfRightDivider - mDividerThickness
                mDividerDrawable!!.setBounds(leftOfRightDivider, 0, rightOfRightDivider, bottom)
                mDividerDrawable!!.draw(canvas)
            } else {
                val right = right

                // draw the top divider
                val topOfTopDivider = mTopDividerTop
                val bottomOfTopDivider = topOfTopDivider + mDividerThickness
                mDividerDrawable!!.setBounds(0, topOfTopDivider, right, bottomOfTopDivider)
                mDividerDrawable!!.draw(canvas)

                // draw the bottom divider
                val bottomOfBottomDivider = mBottomDividerBottom
                val topOfBottomDivider = bottomOfBottomDivider - mDividerThickness
                mDividerDrawable!!.setBounds(0, topOfBottomDivider, right, bottomOfBottomDivider)
                mDividerDrawable!!.draw(canvas)
            }
        }
    }

    private fun drawText(text: String, x: Float, y: Float, paint: Paint, canvas: Canvas) {
        var localY = y
        if (text.contains("\n")) {
            val lines = text.split("\n").toTypedArray()
            val height = (
                abs(paint.descent() + paint.ascent()) *
                    mLineSpacingMultiplier
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

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = NumberPicker::class.java.name
        event.isScrollable = scrollerEnabled
        val scroll = (minValue + mValue) * mSelectorElementSize
        val maxScroll = (maxValue - minValue) * mSelectorElementSize
        if (isHorizontalMode()) {
            event.scrollX = scroll
            event.maxScrollX = maxScroll
        } else {
            event.scrollY = scroll
            event.maxScrollY = maxScroll
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
            resolveSizeAndState(
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
        mSelectorIndexToStringCache.clear()
        val selectorIndices = getSelectorIndices()
        for (i in mSelectorIndices.indices) {
            var selectorIndex = value + (i - mWheelMiddleItemIndex)
            if (mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex)
            }
            selectorIndices[i] = selectorIndex
            ensureCachedScrollSelectorValue(selectorIndices[i])
        }
    }

    /**
     * Sets the current weight of this NumberPicker.
     *
     * @param current      The new weight of the NumberPicker.
     * @param notifyChange Whether to notify if the current weight changed.
     */
    private fun setValueInternal(current: Int, notifyChange: Boolean) {
        var localValue = current
        if (mValue == localValue) {
            return
        }
        // Wrap around the values if we go past the start or end
        if (mWrapSelectorWheel) {
            localValue = getWrappedSelectorIndex(localValue)
        } else {
            localValue = max(localValue, minValue)
            localValue = min(localValue, maxValue)
        }
        val previous = mValue
        mValue = localValue
        // If we're flinging, we'll update the text view at the end when it becomes visible
        if (mScrollState != OnScrollListener.SCROLL_STATE_FLING) {
            updateInputTextView()
        }
        if (notifyChange) {
            notifyChange(previous, localValue)
        }
        initializeSelectorWheelIndices()
        updateAccessibilityDescription()
        invalidate()
    }

    /**
     * Updates the accessibility values of the view,
     * to the currently selected weight
     */
    private fun updateAccessibilityDescription() {
        this.contentDescription = value.toString()
    }

    /**
     * Changes the current weight by one which is increment or
     * decrement based on the passes argument.
     * decrement the current weight.
     *
     * @param increment True to increment, false to decrement.
     */
    private fun changeValueByOne(increment: Boolean) {
        if (!moveToFinalScrollerPosition(mFlingScroller)) {
            moveToFinalScrollerPosition(mAdjustScroller)
        }
        if (isHorizontalMode()) {
            mPreviousScrollerX = 0
            if (increment) {
                mFlingScroller.startScroll(0, 0, -mSelectorElementSize, 0, SNAP_SCROLL_DURATION)
            } else {
                mFlingScroller.startScroll(0, 0, mSelectorElementSize, 0, SNAP_SCROLL_DURATION)
            }
        } else {
            mPreviousScrollerY = 0
            if (increment) {
                mFlingScroller.startScroll(0, 0, 0, -mSelectorElementSize, SNAP_SCROLL_DURATION)
            } else {
                mFlingScroller.startScroll(0, 0, 0, mSelectorElementSize, SNAP_SCROLL_DURATION)
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
            mSelectorElementSize = maxTextSize.toInt() + selectorTextGapWidth
            mInitialScrollOffset =
                selectedTextCenterX.toInt() - mSelectorElementSize * mWheelMiddleItemIndex
        } else {
            val totalTextGapHeight = (bottom - top - totalTextSize).toFloat()
            selectorTextGapHeight = (totalTextGapHeight / textGapCount).toInt()
            mSelectorElementSize = maxTextSize.toInt() + selectorTextGapHeight
            mInitialScrollOffset =
                selectedTextCenterY.toInt() - mSelectorElementSize * mWheelMiddleItemIndex
        }
        mCurrentScrollOffset = mInitialScrollOffset
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
        if (scroller === mFlingScroller) {
            ensureScrollWheelAdjusted()
            updateInputTextView()
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
        } else if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            updateInputTextView()
        }
    }

    private fun onScrollStateChange(scrollState: Int) {
        if (mScrollState == scrollState) {
            return
        }
        mScrollState = scrollState
        scrollListener?.onScrollStateChange(this, scrollState)
    }

    private fun fling(velocity: Int) {
        if (isHorizontalMode()) {
            mPreviousScrollerX = 0
            if (velocity > 0) {
                mFlingScroller.fling(
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
                mFlingScroller.fling(
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
            mPreviousScrollerY = 0
            if (velocity > 0) {
                mFlingScroller.fling(
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
                mFlingScroller.fling(
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
        return mSelectorIndices
    }

    private fun incrementSelectorIndices(selectorIndices: IntArray) {
        for (i in 0 until selectorIndices.size - 1) {
            selectorIndices[i] = selectorIndices[i + 1]
        }
        var nextScrollSelectorIndex = selectorIndices[selectorIndices.size - 2] + 1
        if (mWrapSelectorWheel && nextScrollSelectorIndex > maxValue) {
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
        if (mWrapSelectorWheel && nextScrollSelectorIndex < minValue) {
            nextScrollSelectorIndex = maxValue
        }
        selectorIndices[0] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    private fun ensureCachedScrollSelectorValue(selectorIndex: Int) {
        val cache = mSelectorIndexToStringCache
        var scrollSelectorValue = cache[selectorIndex]
        if (scrollSelectorValue != null) {
            return
        }
        scrollSelectorValue = if (selectorIndex < minValue || selectorIndex > maxValue) {
            ""
        } else {
            if (mDisplayedValues.isNotEmpty()) {
                val displayedValueIndex = selectorIndex - minValue
                mDisplayedValues[displayedValueIndex]
            } else {
                formatNumber(selectorIndex)
            }
        }
        cache.put(selectorIndex, scrollSelectorValue)
    }

    private fun formatNumber(value: Int): String {
        return if (mFormatter != null) mFormatter!!.format(value) else formatNumberWithLocale(value)
    }

    private fun updateInputTextView(): Boolean {
        /*
         * If we don't have displayed values then use the current number else
         * find the correct weight in the displayed values for the current
         * number.
         */
        val text = if (mDisplayedValues.isNotEmpty()) {
            mDisplayedValues.getOrNull(mValue - minValue)
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
            for (listener in valueChangeListeners) listener.onValueChange(this, previous, mValue)
        }
    }

    private fun postChangeCurrentByOneFromLongPress(
        increment: Boolean,
        delayMillis: Long = ViewConfiguration.getLongPressTimeout()
            .toLong()
    ) {
        if (mChangeCurrentByOneFromLongPressCommand == null) {
            mChangeCurrentByOneFromLongPressCommand = ChangeCurrentByOneFromLongPressCommand()
        } else {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
        mChangeCurrentByOneFromLongPressCommand!!.setStep(increment)
        postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis)
    }

    private fun removeChangeCurrentByOneFromLongPress() {
        mChangeCurrentByOneFromLongPressCommand?.let { removeCallbacks(it) }
    }

    private fun removeAllCallbacks() {
        mChangeCurrentByOneFromLongPressCommand?.let { removeCallbacks(it) }
        mSetSelectionCommand?.cancel()
    }

    /**
     * @return The selected index given its displayed `weight`.
     */
    private fun getSelectedPos(value: String): Int {
        var localValue = value
        if (mDisplayedValues.isEmpty()) {
            try {
                return localValue.toInt()
            } catch (e: NumberFormatException) {
                // Ignore as if it's not a number we don't care
            }
        } else {
            for (i in mDisplayedValues.indices) {
                // Don't force the user to type in jan when ja will do
                localValue = localValue.lowercase(Locale.getDefault())
                if (mDisplayedValues[i].lowercase(Locale.getDefault()).startsWith(localValue)) {
                    return minValue + i
                }
            }

            /*
             * The user might have typed in a number into the month field i.e.
             * 10 instead of OCT so support that too.
             */
            runCatching { localValue.toInt() }
                .onSuccess { return it }
        }
        return minValue
    }

    private fun postSetSelectionCommand(selectionStart: Int, selectionEnd: Int) {
        if (mSetSelectionCommand == null) {
            mSetSelectionCommand = SetSelectionCommand(selectedText)
        } else {
            mSetSelectionCommand!!.post(selectionStart, selectionEnd)
        }
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
            if (mSetSelectionCommand != null) {
                mSetSelectionCommand!!.cancel()
            }
            return if (mDisplayedValues.isEmpty()) {
                var filtered = super.filter(source, start, end, dest, dstart, dend)
                if (filtered == null) {
                    filtered = source.subSequence(start, end)
                }
                val result = (
                    dest.subSequence(0, dstart).toString() + filtered +
                        dest.subSequence(dend, dest.length)
                    )
                if ("" == result) {
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
                val str = result.lowercase(Locale.getDefault())
                mDisplayedValues.forEach { displayedValue ->
                    if (displayedValue.lowercase(Locale.getDefault()).startsWith(str)) {
                        postSetSelectionCommand(result.length, displayedValue.length)
                        return displayedValue.subSequence(dstart, displayedValue.length)
                    }
                }
                ""
            }
        }
    }

    private fun ensureScrollWheelAdjusted(): Boolean {
        // adjust to the closest weight
        var delta = mInitialScrollOffset - mCurrentScrollOffset
        if (delta != 0) {
            if (abs(delta) > mSelectorElementSize / 2) {
                delta += if (delta > 0) -mSelectorElementSize else mSelectorElementSize
            }
            if (isHorizontalMode()) {
                mPreviousScrollerX = 0
                mAdjustScroller.startScroll(0, 0, delta, 0, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
            } else {
                mPreviousScrollerY = 0
                mAdjustScroller.startScroll(0, 0, 0, delta, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
            }
            invalidate()
            return true
        }
        return false
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

    private fun formatNumberWithLocale(value: Int): String {
        return mNumberFormatter.format(value.toLong())
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

    fun setDividerColor(@ColorInt color: Int) {
        dividerColor = color
        mDividerDrawable = ColorDrawable(color)
    }

    fun setDividerColorResource(@ColorRes colorId: Int) {
        setDividerColor(ContextCompat.getColor(context, colorId))
    }

    fun setDividerDistance(distance: Int) {
        mDividerDistance = distance
    }

    fun setDividerDistanceResource(@DimenRes dimenId: Int) {
        setDividerDistance(resources.getDimensionPixelSize(dimenId))
    }

    fun setDividerThickness(thickness: Int) {
        mDividerThickness = thickness
    }

    fun setDividerThicknessResource(@DimenRes dimenId: Int) {
        setDividerThickness(resources.getDimensionPixelSize(dimenId))
    }

    override fun setOrientation(@Orientation orientation: Int) {
        mOrientation = orientation
        setWidthAndHeight()
    }

    fun setWheelItemCount(count: Int) {
        require(count >= 1) { "Wheel item count must be >= 1" }
        mRealWheelItemCount = count
        mWheelItemCount = if (count < DEFAULT_WHEEL_ITEM_COUNT) DEFAULT_WHEEL_ITEM_COUNT else count
        mWheelMiddleItemIndex = mWheelItemCount / 2
        mSelectorIndices = IntArray(mWheelItemCount)
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
        mFadingEdgeStrength = strength
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
        mLineSpacingMultiplier = multiplier
    }

    fun setMaxFlingVelocityCoefficient(coefficient: Int) {
        mMaxFlingVelocityCoefficient = coefficient
        mMaximumFlingVelocity = (
            mViewConfiguration.scaledMaximumFlingVelocity /
                mMaxFlingVelocityCoefficient
            )
    }

    fun isHorizontalMode(): Boolean {
        return orientation == HORIZONTAL
    }

    fun isAscendingOrder(): Boolean {
        return order == ASCENDING
    }

    override fun getOrientation(): Int {
        return mOrientation
    }

    /**
     * Set the formatter to be used for formatting the current weight.
     *
     *
     * Note: If you have provided alternative values for the values this
     * formatter is never invoked.
     *
     *
     * @param formatter The formatter object. If formatter is `null`,
     * [String.valueOf] will be used.
     * @see .setDisplayedValues
     */
    var formatter: Formatter?
        get() = mFormatter
        set(formatter) {
            if (formatter === mFormatter) {
                return
            }
            mFormatter = formatter
            initializeSelectorWheelIndices()
            updateInputTextView()
        }

    companion object {
        const val VERTICAL = LinearLayout.VERTICAL
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val ASCENDING = 0
        const val DESCENDING = 1
        const val RIGHT = 0
        const val CENTER = 1
        const val LEFT = 2

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
        private const val DEFAULT_TEXT_ALIGN = CENTER
        private const val DEFAULT_TEXT_COLOR = -0x1000000
        private const val DEFAULT_TEXT_SIZE = 25f
        private const val DEFAULT_LINE_SPACING_MULTIPLIER = 1f
        private val sTwoDigitFormatter = TwoDigitFormatter()
        val twoDigitFormatter: Formatter
            get() = sTwoDigitFormatter

        /**
         * Utility to reconcile a desired size and state, with constraints imposed
         * by a MeasureSpec.  Will take the desired size, unless a different size
         * is imposed by the constraints.  The returned weight is a compound integer,
         * with the resolved size in the [.MEASURED_SIZE_MASK] bits and
         * optionally the bit [.MEASURED_STATE_TOO_SMALL] set if the resulting
         * size is smaller than the size the view wants to be.
         *
         * @param size        How big the view wants to be
         * @param measureSpec Constraints imposed by the parent
         * @return Size information bit mask as defined by
         * [.MEASURED_SIZE_MASK] and [.MEASURED_STATE_TOO_SMALL].
         */
        fun resolveSizeAndState(size: Int, measureSpec: Int, childMeasuredState: Int): Int {
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
         * The numbers accepted by the input text's [Filter]
         */
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
    }
    /**
     * Create a new number picker
     *
     * @param context  the application environment.
     * @param attrs    a collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    /**
     * Create a new number picker.
     *
     * @param context The application environment.
     * @param attrs   A collection of attributes.
     */
    /**
     * Create a new number picker.
     *
     * @param context The application environment.
     */
    init {
        mNumberFormatter = NumberFormat.getInstance()
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
            mDividerDrawable = selectionDivider
        } else {
            dividerColor = attributes.getColor(
                R.styleable.NumberPicker_np_dividerColor,
                dividerColor
            )
            setDividerColor(dividerColor)
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
        mDividerDistance = attributes.getDimensionPixelSize(
            R.styleable.NumberPicker_np_dividerDistance,
            defDividerDistance
        )
        mDividerThickness = attributes.getDimensionPixelSize(
            R.styleable.NumberPicker_np_dividerThickness,
            defDividerThickness
        )
        order = attributes.getInt(R.styleable.NumberPicker_np_order, ASCENDING)
        mOrientation = attributes.getInt(R.styleable.NumberPicker_np_orientation, VERTICAL)
        mWidth = attributes.getDimensionPixelSize(
            R.styleable.NumberPicker_np_width,
            SIZE_UNSPECIFIED
        ).toFloat()
        mHeight = attributes.getDimensionPixelSize(
            R.styleable.NumberPicker_np_height,
            SIZE_UNSPECIFIED
        ).toFloat()
        setWidthAndHeight()
        mValue = attributes.getInt(R.styleable.NumberPicker_np_value, mValue)
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
        mFormatter =
            stringToFormatter(attributes.getString(R.styleable.NumberPicker_np_formatter).orEmpty())
        fadingEdgeEnabled = attributes.getBoolean(
            R.styleable.NumberPicker_np_fadingEdgeEnabled,
            fadingEdgeEnabled
        )
        mFadingEdgeStrength = attributes.getFloat(
            R.styleable.NumberPicker_np_fadingEdgeStrength,
            mFadingEdgeStrength
        )
        scrollerEnabled = attributes.getBoolean(
            R.styleable.NumberPicker_np_scrollerEnabled,
            scrollerEnabled
        )
        mWheelItemCount = attributes.getInt(
            R.styleable.NumberPicker_np_wheelItemCount,
            mWheelItemCount
        )
        mLineSpacingMultiplier = attributes.getFloat(
            R.styleable.NumberPicker_np_lineSpacingMultiplier,
            mLineSpacingMultiplier
        )
        mMaxFlingVelocityCoefficient = attributes.getInt(
            R.styleable.NumberPicker_np_max_fling_velocity_coefficient,
            mMaxFlingVelocityCoefficient
        )
        mHideWheelUntilFocused = attributes.getBoolean(
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
        selectedText = findViewById(R.id.np__numberpicker_input)
        selectedText.isEnabled = true
        selectedText.isFocusable = true
        selectedText.imeOptions = EditorInfo.IME_ACTION_DONE

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
        formatter = mFormatter
        updateInputTextView()
        value = mValue
        setWheelItemCount(mWheelItemCount)
        mWrapSelectorWheel = attributes.getBoolean(
            R.styleable.NumberPicker_np_wrapSelectorWheel,
            mWrapSelectorWheel
        )
        setWrapSelectorWheel(mWrapSelectorWheel)
        if (mWidth != SIZE_UNSPECIFIED.toFloat() && mHeight != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = mWidth / minWidth
            scaleY = mHeight / maxHeight
        } else if (mWidth != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = mWidth / minWidth
            scaleY = mWidth / minWidth
        } else if (mHeight != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = mHeight / maxHeight
            scaleY = mHeight / maxHeight
        }

        // initialize constants
        mViewConfiguration = ViewConfiguration.get(context)
        mTouchSlop = mViewConfiguration.scaledTouchSlop
        mMinimumFlingVelocity = mViewConfiguration.scaledMinimumFlingVelocity
        mMaximumFlingVelocity = (
            mViewConfiguration.scaledMaximumFlingVelocity /
                mMaxFlingVelocityCoefficient
            )

        // create the fling and adjust scrollers
        mFlingScroller = Scroller(context, null, true)
        mAdjustScroller = Scroller(context, DecelerateInterpolator(2.5f))
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
}
