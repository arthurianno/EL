package com.elta.android.presentation.widgets.simple_date_picker

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

class RightLeftDrawable(
    private val circleColor: Int,
    private val rectColor: Int,
    private val atLeft: Boolean
) : Drawable() {

    private val state = ConstantState()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePath = Path()
    private val rectPath = Path()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val cx = bounds.centerX().toFloat()
        val cy = bounds.centerY().toFloat()
        val radius = (bounds.height() / 2).toFloat()

        circlePath.reset()
        circlePath.addCircle(cx, cy, radius, Path.Direction.CW)

        rectPath.reset()
        with(bounds) {
            when (atLeft) {
                true -> rectPath.addRect(
                    left + radius,
                    top.toFloat(),
                    right.toFloat(),
                    bottom.toFloat(),
                    Path.Direction.CW
                )
                false -> rectPath.addRect(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat() - radius,
                    bottom.toFloat(),
                    Path.Direction.CW
                )
            }
        }
    }

    override fun draw(canvas: Canvas) {
        paint.color = rectColor
        canvas.drawPath(rectPath, paint)

        paint.color = circleColor
        canvas.drawPath(circlePath, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter) {
        paint.colorFilter = colorFilter
    }

    override fun getConstantState(): ConstantState = state

    inner class ConstantState : Drawable.ConstantState() {
        override fun newDrawable(): Drawable = this@RightLeftDrawable
        override fun getChangingConfigurations(): Int = 0
    }
}