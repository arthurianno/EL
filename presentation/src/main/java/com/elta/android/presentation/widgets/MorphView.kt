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
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
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
    private val ovals = arrayListOf<Oval>().apply {
        add(oval1)
        add(oval2)
        add(oval3)
    }

    private val ovalsRange = 0 until ovals.size

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

    private val keys = mutableSetOf<String>()
    private val holders = mutableListOf<PathHolder>()
    private var holdersIterator: Iterator<PathHolder>? = null

    private val initializeValuesAction = PublishRelay.create<Unit>()
    private val compositeDisposable = CompositeDisposable()

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

        initializeValuesAction
            .switchMapCompletable {
                Completable.fromCallable { initializeValues() }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete { playAnimation() }
            }
            .retry()
            .subscribe()
            .addTo(compositeDisposable)
    }

    override fun onDetachedFromWindow() {
        compositeDisposable.clear()

        if (isAnimating()) {
            cancelAnimation()
            wasAnimatingWhenDetached = true
        }

        super.onDetachedFromWindow()
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

        isInitialized = false

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

        initializeValuesAction.accept(Unit)

        // 60fps
        val timer = ValueAnimator.ofInt(0, frames).apply {
            duration = time
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                holdersIterator?.let {
                    val holder = it.next()
                    oval1.path = holder.p1
                    oval2.path = holder.p2
                    oval3.path = holder.p3
                    invalidate()
                }
            }
        }

        animators.map { it.cancel() }
        animators.clear()

        animators.add(timer)
    }

    override fun onDraw(canvas: Canvas) {
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

    private fun initializeValues() {

        initializeValues(0f, 2f, 8000f, frameDuration, smallAngleValues.values, false)
        initializeValues(0.65f, 0.8f, 4000f, frameDuration, largeAngleValues.values, true)

        initializeValues(smallOvalSize.xMin, smallOvalSize.xMax, 2500f, frameDuration, xRValues.values, true)
        initializeValues(smallOvalSize.yMin, smallOvalSize.yMax, 2000f, frameDuration, yRValues.values, true)

        initializeValues(largeOvalSize.xMin, largeOvalSize.xMax, 2500f, frameDuration, xRLargeValues.values, true)
        initializeValues(largeOvalSize.yMin, largeOvalSize.yMax, 2000f, frameDuration, yRLargeValues.values, true)

        val max0 = max(smallAngleValues.values.size, largeAngleValues.values.size)
        val max1 = max(xRValues.values.size, yRValues.values.size)
        val max2 = max(xRLargeValues.values.size, yRLargeValues.values.size)

        val max = maxOf(max0, max1, max2)

        var added = 0

        (0 until max).forEach {

            val smallAngleValue = smallAngleValues.next()
            val largeAngleValue = largeAngleValues.next()

            val xRValue = xRValues.next()
            val yRValue = yRValues.next()

            val xRLargeValue = xRLargeValues.next()
            val yRLargeValue = yRLargeValues.next()

            val key = "$smallAngleValue-$largeAngleValue-$xRValue-$yRValue-$xRLargeValue-$yRLargeValue"
            keys.add(key).also {
                if (it) {
                    added++

                    oval1.angle = smallAngleValue
                    oval2.angle = smallAngleValue
                    oval3.angle = largeAngleValue

                    oval1.radius.x = xRValue
                    oval2.radius.x = xRValue

                    oval1.radius.y = yRValue
                    oval2.radius.y = yRValue

                    oval3.radius.x = xRLargeValue
                    oval3.radius.y = yRLargeValue

                    holders.add(getOvalPaths(ovals))
                }
            }
        }

        println(added)

        holdersIterator = holders.forwardBackwardIterator()

        isInitialized = true
    }

    private fun getOvalPaths(ovals: List<Oval>): PathHolder {
        val start = 0f
        val end = END

        val holder = PathHolder()

        var i = start
        while (i <= end) {

            val sinI = sin(i)
            val cosI = cos(i)

            for (index in ovalsRange) {
                val oval = ovals[index]

                val path = holder.getPath(index)
                val anglePi = oval.rotateOffsetPiFloat * oval.angle

                val sinAnglePi = sin(anglePi)
                val cosAnglePi = cos(anglePi)

                val xR = oval.radius.x
                val yR = oval.radius.y

                val cx = oval.center.x
                val cy = oval.center.y

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

        holder.p1.addPath(holder.p2)

        return holder
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
    var path: Path = Path(),
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

class PathHolder(
    val p1: Path = Path(),
    val p2: Path = Path(),
    val p3: Path = Path()
) {
    inline fun getPath(index: Int): Path = if (index == 0) p1 else if (index == 1) p2 else p3
}

fun <T> List<T>.forwardBackwardIterator(count: Int = -1): Iterator<T> =
    ForwardBackwardIterator(this, count)

class ForwardBackwardIterator<T>(private val list: List<T>, private val count: Int = -1) : Iterator<T> {

    private val size = list.size
    private var current = -1
    private var isForward = true
    private var iteration = 0

    override fun hasNext(): Boolean = if (count == -1) true else iteration < count

    override fun next(): T {
        if (isForward) {
            current += 1
            if (current == size - 1) {
                isForward = false
            }
        } else {
            current -= 1
            if (current == 0) {
                isForward = true
                iteration += 1
            }
        }

        return list[current]
    }
}