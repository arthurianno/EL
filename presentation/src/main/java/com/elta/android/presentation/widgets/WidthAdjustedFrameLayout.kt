package com.elta.android.presentation.widgets

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.elta.android.presentation.R

class WidthAdjustedFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val leftOffset: Int
    private val rightOffset: Int
    private val screenWidth: Int

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.WidthAdjustedFrameLayout, defStyleAttr, 0)

        with(attributes) {
            leftOffset = getDimensionPixelSize(R.styleable.WidthAdjustedFrameLayout_leftOffset, 0)
            rightOffset = getDimensionPixelSize(R.styleable.WidthAdjustedFrameLayout_rightOffset, 0)

            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val screenSize = Point()
            display.getSize(screenSize)
            screenWidth = screenSize.x
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(screenWidth - leftOffset - rightOffset, View.MeasureSpec.EXACTLY), heightMeasureSpec)
    }
}