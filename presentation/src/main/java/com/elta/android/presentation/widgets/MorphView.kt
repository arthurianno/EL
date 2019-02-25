package com.elta.android.presentation.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Region
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MorphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val smallOvalPaint: Paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }

    private val largeOvalPaint: Paint = Paint().apply {
        color = Color.parseColor("#99ffffff")
        isAntiAlias = true
    }

    private val oval1 = Oval(rotateOffset = 0f)
    private val oval2 = Oval(rotateOffset = 0.5f)
    private val oval3 = Oval(rotateOffset = 1f)

    private val smallOvalSize = OvalSize()
    private val largeOvalSize = OvalSize()

    private val clip: Region = Region()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        clip.set(0, 0, measuredWidth, measuredHeight)

        val cx = measuredWidth / 2f
        val cy = measuredHeight / 2f

        oval1.setCenter(cx, cy)
        oval2.setCenter(cx, cy)
        oval3.setCenter(cx, cy)

        smallOvalSize.xMin = cx * 0.75f
        smallOvalSize.xMax = cx * 0.85f
        smallOvalSize.yMin = cy * 0.85f
        smallOvalSize.yMax = cy * 0.9f


        largeOvalSize.xMin = cx * 0.60f
        largeOvalSize.xMax = cx * 0.70f
        largeOvalSize.yMin = cy * 0.95f
        largeOvalSize.yMax = cy

        oval1.radius.x = smallOvalSize.xMin
        oval1.radius.y = smallOvalSize.yMin

        oval2.radius.x = smallOvalSize.xMin
        oval2.radius.y = smallOvalSize.yMin

        oval3.radius.x = largeOvalSize.xMin
        oval3.radius.y = largeOvalSize.yMin


        createOvalPath(oval1, oval2, oval3)

        val timer = ValueAnimator.ofInt(0, 60).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                createOvalPath(oval1, oval2, oval3)
                invalidate()
            }
        }

        val smallAngleAnimator = ValueAnimator.ofFloat(0.0f, 2.0f).apply {
            duration = 8000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                oval1.angle = it.animatedValue as Float
                oval2.angle = it.animatedValue as Float

            }
        }

        val largeAngleAnimator = ValueAnimator.ofFloat(0.65f, 0.8f, 0.65f).apply {
            duration = 8000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                oval3.angle = it.animatedValue as Float
            }
        }

        val xRanimator = ValueAnimator.ofFloat(smallOvalSize.xMin, smallOvalSize.xMax, smallOvalSize.xMin).apply {
            duration = 5000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                oval1.radius.x = it.animatedValue as Float
                oval2.radius.x = it.animatedValue as Float
            }
        }

        val yRanimator = ValueAnimator.ofFloat(smallOvalSize.yMin, smallOvalSize.yMax, smallOvalSize.yMin).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                oval1.radius.y = it.animatedValue as Float
                oval2.radius.y = it.animatedValue as Float
            }
        }

        val xRlargeAnimator = ValueAnimator.ofFloat(largeOvalSize.xMin, largeOvalSize.xMax, largeOvalSize.xMin).apply {
            duration = 5000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                oval3.radius.x = it.animatedValue as Float
            }
        }

        val yRlargeAnimator = ValueAnimator.ofFloat(largeOvalSize.yMin, largeOvalSize.yMax, largeOvalSize.yMin).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                oval3.radius.y = it.animatedValue as Float
            }
        }

        smallAngleAnimator.start()
        xRanimator.start()
        yRanimator.start()

        largeAngleAnimator.start()
        xRlargeAnimator.start()
        yRlargeAnimator.start()

        timer.start()
    }

    override fun onDraw(canvas: Canvas) {
        oval1.path.addPath(oval2.path)
        canvas.drawPath(oval3.path, largeOvalPaint)
        canvas.drawPath(oval1.path, smallOvalPaint)
    }

    private fun createOvalPath(vararg ovals: Oval) {
        ovals.map { it.path.reset() }

        val start = 0f
        val end = 2 * PI.toFloat()

        var i = start
        while (i <= end) {

            val sinI = sin(i)
            val cosI = cos(i)

            ovals.forEach {
                val path = it.path
                val anglePi = it.rotateOffset * it.angle * PI.toFloat()

                val sinAnglePi = sin(anglePi)
                val cosAnglePi = cos(anglePi)

                val xR = it.radius.x
                val yR = it.radius.y

                val cx = it.center.x
                val cy = it.center.y

                val xPos = cx - xR * sinI * sinAnglePi + yR * cosI * cosAnglePi
                val yPos = cy + yR * cosI * sinAnglePi + xR * sinI * cosAnglePi

                if (i == 0f) {
                    path.moveTo(xPos, yPos)
                } else {
                    path.lineTo(xPos, yPos)
                }
            }

            i += 0.01f
        }
        ovals.map { it.path.close() }
    }

    fun start() {

    }
}

class Oval(
    val path: Path = Path(),
    val rotateOffset: Float = 1f,
    var center: PointF = PointF(),
    var radius: PointF = PointF(),
    var angle: Float = 0f
) {

    fun setCenter(cx: Float, cy: Float) {
        center.x = cx
        center.y = cy
    }
}

class OvalSize(
    var xMin: Float = 0f,
    var xMax: Float = 0f,
    var yMin: Float = 0f,
    var yMax: Float = 0f
)