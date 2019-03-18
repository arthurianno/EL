package com.elta.android.presentation.widgets.date_picker.adapter.decoration

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.widgets.date_picker.HorizontalDatePickerView
import com.nullgr.core.ui.extensions.getDisplaySize

class DatePickerItemDecoration(val context: Context) : RecyclerView.ItemDecoration() {

    private val screenWidth by lazy { getDisplaySize(context).first }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (screenWidth == 0)
            super.onDrawOver(c, parent, state)
        else
            for (i in 0 until parent.childCount) {
                val view = parent.getChildAt(i)
                view.layoutParams.width = screenWidth / HorizontalDatePickerView.ITEMS_ON_SCREEN_COUNT
            }
    }
}