package com.elta.android.presentation.widgets.indicators

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.indicators.listeners.SimpleAdapterDataObserver
import com.nullgr.core.ui.extensions.dpToPx

@Suppress("TooManyFunctions", "LargeClass")
class IndicatorsView : View {

    private var selectedDrawable: Drawable? = null
    private var unSelectedDrawable: Drawable? = null

    private lateinit var unSelectedBitmap: Bitmap
    private lateinit var selectedBitmap: Bitmap

    private lateinit var rect: Rect
    private val tempRect: Rect by lazy { Rect() }

    private var indicatorWidth = DEFAULT_INDICATOR_SIZE
    private var indicatorHeight = DEFAULT_INDICATOR_SIZE

    private var paddingBetweenIndicators = DEFAULT_PADDING_BETWEEN_INDICATORS

    private var numOfIndicators = 1
    private var selectedIndicator = 0

    private var leftBound: Int = 0
    private var topBound: Int = 0
    private var totalWidthWeNeed: Int = 0

    private var smoothTransitionEnabled: Boolean = false
    private var currentPositionOffset: Float = 0.toFloat()
    private var currentPosition: Int = 0

    private var recyclerView: RecyclerView? = null
    private var onScrollListener: RecyclerView.OnScrollListener? = null
    private var dataObserver: RecyclerView.AdapterDataObserver? = null

    constructor (context: Context) : super(context) {
        if (!isInEditMode) {
            init(context, null)
        }
    }

    constructor (context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (!isInEditMode) {
            init(context, attrs)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        var desiredWidth = indicatorWidth * numOfIndicators + paddingBetweenIndicators * (numOfIndicators - 1)
        var desiredHeight = indicatorHeight

        desiredWidth += paddingLeft + paddingRight
        desiredHeight += paddingTop + paddingBottom

        var width = desiredWidth
        var height = desiredHeight

        // Measure Width
        if (widthMode == View.MeasureSpec.EXACTLY) {
            width = widthSize // Must be this size (match_parent or exactly value)
        } else if (widthMode == View.MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize) // (wrap_content)
        }

        // Measure Height
        if (heightMode == View.MeasureSpec.EXACTLY) {
            height = heightSize // Must be this size (match_parent or exactly value)
        } else if (heightMode == View.MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize) // (wrap_content)
        }

        updateIndicatorSizes(
            width = width - paddingLeft - paddingRight,
            height = height - paddingTop - paddingBottom
        )

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val hCenter = (width - paddingLeft - paddingRight) / 2
        val vCenter = (height - paddingTop - paddingBottom) / 2

        val diffWithSelectedSize = 0
        val allIndicatorsWidth = indicatorWidth * numOfIndicators + diffWithSelectedSize
        val paddingBetweenIndicators = (numOfIndicators - 1) * paddingBetweenIndicators

        totalWidthWeNeed = allIndicatorsWidth + paddingBetweenIndicators
        leftBound = hCenter - totalWidthWeNeed / 2 + paddingLeft
        topBound = vCenter - indicatorHeight / 2 + paddingTop

        rect.offsetTo(leftBound, topBound)
        for (i in 0 until numOfIndicators) {

            if (i != selectedIndicator || smoothTransitionEnabled) {
                canvas.drawBitmap(unSelectedBitmap, null, rect, null)
            } else {
                canvas.drawBitmap(selectedBitmap, null, rect, null)
            }

            if (i == currentPosition && smoothTransitionEnabled) {
                tempRect.set(rect)
            }

            rect.offset(indicatorWidth + this.paddingBetweenIndicators, 0)
        }

        if (smoothTransitionEnabled) {
            val offset = Math.round((indicatorWidth + this.paddingBetweenIndicators) * currentPositionOffset)
            tempRect.offset(offset, 0)
            canvas.drawBitmap(selectedBitmap, null, tempRect, null)
        }
    }

    // Control
    fun setSelected(selectedIndicator: Int) {
        this.selectedIndicator = selectedIndicator
        invalidate()
    }

    fun setSelectedDrawable(drawable: Drawable) {
        selectedDrawable = drawable
        prepareIndicators()
        invalidate()
    }

    fun setUnSelectedDrawable(drawable: Drawable) {
        unSelectedDrawable = drawable
        prepareIndicators()
        invalidate()
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        onScrollListener?.let { scroll ->

            dataObserver?.let {
                if (this.recyclerView == recyclerView) {
                    return
                }

                if (this.recyclerView != null) {
                    this.recyclerView?.removeOnScrollListener(scroll)
                    this.recyclerView?.adapter?.unregisterAdapterDataObserver(it)
                }

                this.recyclerView = recyclerView
                if (this.recyclerView != null) {
                    this.recyclerView?.addOnScrollListener(scroll)
                    this.recyclerView?.adapter?.registerAdapterDataObserver(it)
                }
                invalidate()
            }
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        initDefaultValues(context)
        readAttributes(context, attrs)
        prepareIndicators()

        onScrollListener = object : RecyclerView.OnScrollListener() {

            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (smoothTransitionEnabled) {
                    currentPositionOffset = dx.toFloat()
                    currentPosition = findVisiblePosition()
                    invalidate()
                }
            }

            override fun onScrollStateChanged(@NonNull recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = findVisiblePosition()
                    setSelected(position)
                }
            }
        }

        dataObserver = object : SimpleAdapterDataObserver() {
            override fun onAny() {
                notifyIndicatorsCountChanged()
            }
        }
    }

    private fun initDefaultValues(context: Context) {
        indicatorWidth = indicatorWidth.toFloat().dpToPx(resources).toInt()
        indicatorHeight = indicatorHeight.toFloat().dpToPx(resources).toInt()
        paddingBetweenIndicators = paddingBetweenIndicators.toFloat().dpToPx(resources).toInt()

        selectedDrawable = ContextCompat.getDrawable(context, R.drawable.indicator_selected_dot)
        unSelectedDrawable = ContextCompat.getDrawable(context, R.drawable.indicator_unselected_dot)
    }

    private fun readAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.IndicatorsView, 0, 0)

            // Get indicators if exist
            val selectedDrawable = a.getDrawable(R.styleable.IndicatorsView_selectedDrawable)
            if (selectedDrawable != null) {
                this.selectedDrawable = selectedDrawable
            }
            val unSelectedDrawable = a.getDrawable(R.styleable.IndicatorsView_unSelectedDrawable)
            if (unSelectedDrawable != null) {
                this.unSelectedDrawable = unSelectedDrawable
            }

            // Get indicator size
            indicatorWidth = a.getDimension(R.styleable.IndicatorsView_indicatorWidth, indicatorWidth.toFloat()).toInt()
            indicatorHeight = a.getDimension(R.styleable.IndicatorsView_indicatorHeight, indicatorHeight.toFloat()).toInt()

            // Get padding between indicators
            paddingBetweenIndicators = a.getDimension(
                R.styleable.IndicatorsView_paddingBetweenIndicators,
                paddingBetweenIndicators.toFloat()
            ).toInt()

            // Get number of indicators
            numOfIndicators = a.getInteger(R.styleable.IndicatorsView_numberOfIndicators, numOfIndicators)

            // Get selected indicator
            selectedIndicator = a.getInteger(R.styleable.IndicatorsView_selectedIndicator, selectedIndicator)

            a.recycle()
        }
    }

    private fun prepareIndicators() {
        rect = Rect(0, 0, indicatorWidth, indicatorHeight)
        unSelectedBitmap = unSelectedDrawable.toBitmap(indicatorWidth, indicatorHeight)
        selectedBitmap = selectedDrawable.toBitmap(indicatorWidth, indicatorHeight)
    }

    private fun updateIndicatorSizes(width: Int, height: Int) {
        var tempWidth = width
        var isIndicatorSizeChanged = false

        // if width is not wide enough
        if (indicatorWidth * numOfIndicators + (numOfIndicators - 1) * paddingBetweenIndicators > width) {
            tempWidth -= paddingBetweenIndicators * (numOfIndicators - 1)
            indicatorWidth = tempWidth / numOfIndicators
            isIndicatorSizeChanged = true
        }

        // if height is not high enough
        if (indicatorHeight > height) {
            indicatorHeight = height
            isIndicatorSizeChanged = true
        }

        if (isIndicatorSizeChanged) {
            prepareIndicators()
        }
    }

    private fun findVisiblePosition(): Int {
        val layoutManager = recyclerView?.layoutManager as LinearLayoutManager
        return layoutManager.findFirstCompletelyVisibleItemPosition()
    }

    private fun notifyIndicatorsCountChanged() {
        numOfIndicators = recyclerView?.adapter?.itemCount ?: numOfIndicators
        requestLayout()
    }

    private fun Drawable?.toBitmap(width: Int, height: Int): Bitmap {
        if (this is BitmapDrawable) {
            return this.bitmap
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this?.setBounds(0, 0, canvas.width, canvas.height)
        this?.draw(canvas)
        return bitmap
    }

    companion object {
        private const val DEFAULT_INDICATOR_SIZE = 13
        private const val DEFAULT_PADDING_BETWEEN_INDICATORS = 7
    }
}
