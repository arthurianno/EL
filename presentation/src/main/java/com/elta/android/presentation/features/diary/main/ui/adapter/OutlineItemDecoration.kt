package com.elta.android.presentation.features.diary.main.ui.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.ui.adapter.holder.ItemRecordViewHolder
import com.elta.android.presentation.features.main.records.ui.adapter.holder.ItemRecordsGroupViewHolder

class OutlineItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val strokeWidth: Int = context.resources.getDimensionPixelSize(R.dimen.divider_height)
    private val cornerRadius: Int = context.resources.getDimensionPixelSize(R.dimen.card_corner)
    private val topMargin: Int = context.resources.getDimensionPixelSize(R.dimen.home_between_margin)

    private val paint: Paint = Paint()
    private val bg: Paint = Paint()
    private val path = Path()
    private val bottomRadii = floatArrayOf(
        0f, 0f,
        0f, 0f,
        cornerRadius.toFloat(), cornerRadius.toFloat(),
        cornerRadius.toFloat(), cornerRadius.toFloat(),
    )

    init {
        paint.color = ContextCompat.getColor(context, R.color.diary_divider_color)
        bg.color = ContextCompat.getColor(context, R.color.white)
        bg.style = Paint.Style.FILL
        paint.strokeWidth = strokeWidth.toFloat()
        paint.style = Paint.Style.STROKE
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val currentChild = parent.getChildAt(i)
            val currentViewHolder = parent.getChildViewHolder(currentChild)

            val nextChild = parent.getChildAt(i + 1)
            val nextViewHolder = try { parent.getChildViewHolder(nextChild) } catch (e: Exception) { null }

            when {
                currentViewHolder is ItemRecordsGroupViewHolder &&
                        (nextViewHolder is ItemRecordsGroupViewHolder || nextViewHolder == null) -> {
                            currentChild.setBackgroundResource(R.drawable.bg_record_group)
                        }
                currentViewHolder is ItemRecordsGroupViewHolder &&
                        nextViewHolder is ItemRecordViewHolder -> currentChild.setBackgroundResource(R.drawable.bg_record_group_rounded_top)
                currentViewHolder is ItemRecordViewHolder && (nextViewHolder is ItemRecordsGroupViewHolder || nextViewHolder == null) -> drawRoundRectBottomLeftRight(c, currentChild)
                currentViewHolder is ItemRecordViewHolder -> {
                    currentChild.background = null
                    drawRect(c, currentChild)
                }
                else -> {}
            }
        }
    }

    private fun drawRect(canvas: Canvas, view: View) {
        val rect = RectF(
            view.left.toFloat(),
            view.top.toFloat(),
            view.right.toFloat(),
            view.bottom.toFloat()
        )

        val rectBg = RectF(
            view.left.toFloat() + strokeWidth/2,
            view.top.toFloat() + strokeWidth/2,
            view.right.toFloat() - strokeWidth/2,
            view.bottom.toFloat()
        )

        canvas.drawRoundRect(rect, 0f, 0f, paint)
        canvas.drawRoundRect(rectBg, 0f, 0f, bg)
    }

    private fun drawRoundRectBottomLeftRight(canvas: Canvas, view: View) {
        val rect = RectF(
            view.left.toFloat(),
            view.top.toFloat(),
            view.right.toFloat(),
            view.bottom.toFloat(),
        )

        path.addRoundRect(rect, bottomRadii, Path.Direction.CW)

        canvas.drawPath(path, bg)
        canvas.drawPath(path, paint)
        path.reset()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val viewHolder = parent.getChildViewHolder(view)

        if (viewHolder is ItemRecordsGroupViewHolder) {
            outRect.top = topMargin
        } else {
            outRect.top = 0
        }

        outRect.left = 0
        outRect.right = 0
        outRect.bottom = 0
    }
}