package com.elta.android.presentation.widgets

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.support.annotation.DimenRes
import android.support.v7.widget.RecyclerView
import android.view.View

class MainScreenMarginItemDecoration(
    context: Context,
    @DimenRes
    private val marginTopDimen: Int,
    @DimenRes
    private val marginBottomDimen: Int,
    @DimenRes
    private var marginBetweenDimen: Int,
    @DimenRes
    private var overlapItemDimen: Int
) : RecyclerView.ItemDecoration() {

    private var marginStart: Int
    private var marginEnd: Int
    private var marginBetween: Int
    private var overlapItem: Int

    init {
        marginStart = getPixelSize(context.resources, marginTopDimen)
        marginEnd = getPixelSize(context.resources, marginBottomDimen)
        marginBetween = getPixelSize(context.resources, marginBetweenDimen)
        marginBetween = if (marginBetween != 0) marginBetween / 2 else 0
        overlapItem = getPixelSize(context.resources, overlapItemDimen)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val last = state.itemCount.minus(1)
        when (position) {
            0 -> outRect.bottom = marginBetween
            1 -> {
                outRect.top = -overlapItem
                outRect.bottom = marginBetween
            }
            last -> {
                outRect.top = marginBetween
                outRect.bottom = marginEnd
            }
            else -> {
                outRect.top = marginBetween
                outRect.bottom = marginBetween
            }
        }
    }

    private fun getPixelSize(resources: Resources, @DimenRes margin: Int): Int =
        if (margin == 0) 0 else resources.getDimensionPixelSize(margin)
}