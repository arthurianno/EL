package com.elta.android.presentation.widgets

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Suppress("LongMethod", "MagicNumber")
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

    private var isInitialized = false
    private var wasAnimatingWhenDetached = false
    private var wasAnimatingWhenNotShown = false
    private var autoPlay = true

    private val animators = arrayListOf<Animator>()

    private val time = 1000L
    private val frames = 60
    private val frameDuration = time / frames.toFloat()

    private val smallAngleValues = Values()
    private val largeAngleValues = Values()

    private val xRValues = Values()
    private val yRValues = Values()

    private val xRLargeValues = Values()
    private val yRLargeValues = Values()

    init {
        initializeValues(0f, 2f, 8000f, frameDuration, smallAngleValues.values, false)
        initializeValues(0.65f, 0.8f, 4000f, frameDuration, largeAngleValues.values, true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (autoPlay || wasAnimatingWhenDetached) {
            playAnimation()
            autoPlay = false
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // This is needed to mimic newer platform behavior.
            // https://stackoverflow.com/a/53625860/715633
            onVisibilityChanged(this, visibility)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isAnimating()) {
            cancelAnimation()
            wasAnimatingWhenDetached = true
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        if (isShown) {
            if (wasAnimatingWhenNotShown) {
                resumeAnimation()
                wasAnimatingWhenNotShown = false
            }
        } else {
            if (isAnimating()) {
                pauseAnimation()
                wasAnimatingWhenNotShown = true
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

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

        initializeValues(smallOvalSize.xMin, smallOvalSize.xMax, 2500f, frameDuration, xRValues.values, true)
        initializeValues(smallOvalSize.yMin, smallOvalSize.yMax, 2000f, frameDuration, yRValues.values, true)

        initializeValues(largeOvalSize.xMin, largeOvalSize.xMax, 2500f, frameDuration, xRLargeValues.values, true)
        initializeValues(largeOvalSize.yMin, largeOvalSize.yMax, 2000f, frameDuration, yRLargeValues.values, true)

        // 60fps
        val timer = ValueAnimator.ofInt(0, frames).apply {
            duration = time
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {

                val value = smallAngleValues.next()
                oval1.angle = value
                oval2.angle = value
                oval3.angle = largeAngleValues.next()

                val oval_1_2_X = xRValues.next()
                oval1.radius.x = oval_1_2_X
                oval2.radius.x = oval_1_2_X

                val oval_1_2_Y = yRValues.next()
                oval1.radius.y = oval_1_2_Y
                oval2.radius.y = oval_1_2_Y

                oval3.radius.x = xRLargeValues.next()
                oval3.radius.y = yRLargeValues.next()

                createOvalPath(oval1, oval2, oval3)
                invalidate()
            }
        }

        animators.map { it.cancel() }
        animators.clear()

        animators.add(timer)

        isInitialized = true

        playAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        oval1.path.addPath(oval2.path)
        canvas.drawPath(oval3.path, largeOvalPaint)
        canvas.drawPath(oval1.path, smallOvalPaint)
    }

    fun playAnimation() {
        if (!isInitialized) return

        if (isShown) {
            animators.map { it.start() }
        } else {
            wasAnimatingWhenNotShown = true
        }
    }

    fun resumeAnimation() {
        if (!isInitialized) return

        if (isShown) {
            animators.map { it.resume() }
        } else {
            wasAnimatingWhenNotShown = true
        }
    }

    fun cancelAnimation() {
        if (!isInitialized) return

        wasAnimatingWhenNotShown = false
        animators.map { it.cancel() }
    }

    fun pauseAnimation() {
        if (!isInitialized) return

        autoPlay = false
        wasAnimatingWhenDetached = false
        wasAnimatingWhenNotShown = false
        animators.map { it.pause() }
    }

    private fun isAnimating(): Boolean = animators.lastOrNull()?.isRunning ?: false

    private fun createOvalPath(vararg ovals: Oval) {
        ovals.forEach { it.path.reset() }

        val start = 0f
        val end = END

        var i = start
        while (i <= end) {

            val sinI = sin(i)
            val cosI = cos(i)

            ovals.forEach {
                val path = it.path
                val anglePi = it.rotateOffsetPiFloat * it.angle

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
        ovals.forEach { it.path.close() }
    }

    private fun initializeValues(min: Float, max: Float, duration: Float, frameDuration: Float, values: MutableList<Float>, mirror: Boolean) {
        values.clear()

        val delta = max - min
        val valuePerFrame = delta * frameDuration / duration
        var total: Float = min

        var time: Float = 0f

        while (time <= duration) {
            total += valuePerFrame
            time += frameDuration
            values.add(total)
        }

        if (mirror) {
            values.addAll(values.reversed())
        }
    }

    companion object {
        val PI_FLOAT = PI.toFloat()
        val END = 2 * PI_FLOAT
    }
}

class Oval(
    val path: Path = Path(),
    val rotateOffset: Float = 1f,
    var center: PointF = PointF(),
    var radius: PointF = PointF(),
    var angle: Float = 0f,
    val rotateOffsetPiFloat: Float = rotateOffset * MorphView.PI_FLOAT
) {

    fun setCenter(cx: Float, cy: Float) {
        center.x = cx
        center.y = cy
    }
}

@Suppress("UseDataClass")
class OvalSize(
    var xMin: Float = 0f,
    var xMax: Float = 0f,
    var yMin: Float = 0f,
    var yMax: Float = 0f
)

class Values {

    private var index: Int = 0

    val values = mutableListOf<Float>()

    fun next(): Float = values[index++ % values.size]
}