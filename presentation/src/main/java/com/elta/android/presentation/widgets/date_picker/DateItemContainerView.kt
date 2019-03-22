package com.elta.android.presentation.widgets.date_picker

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.nullgr.core.ui.extensions.getDisplaySize

class DateItemContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val screenWidth by lazy { getDisplaySize(context).first }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpec = MeasureSpec.makeMeasureSpec(
            screenWidth / HorizontalDatePickerView.ITEMS_ON_SCREEN_COUNT,
            MeasureSpec.EXACTLY
        )
        super.onMeasure(widthSpec, heightMeasureSpec)
    }
}