package com.elta.android.presentation.widgets.decoration

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.support.annotation.DimenRes
import android.support.v7.widget.RecyclerView
import android.view.View

class MainScreenMarginItemDecoration(
    context: Context,
    overlapItemDimen: Int
) : RecyclerView.ItemDecoration() {

    private var overlapItem = getPixelSize(context.resources, overlapItemDimen)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        when (position) {
            1 -> outRect.top = -overlapItem
        }
    }

    private fun getPixelSize(resources: Resources, @DimenRes margin: Int): Int =
        if (margin == 0) 0 else resources.getDimensionPixelSize(margin)
}