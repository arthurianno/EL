package com.elta.android.presentation.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.os.Build
import android.os.Handler
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

    private val smallAngleValues = Values(mirror = false)
    private val largeAngleValues = Values(mirror = true)

    private val xRValues = Values(mirror = true)
    private val yRValues = Values(mirror = true)

    private val xRLargeValues = Values(mirror = true)
    private val yRLargeValues = Values(mirror = true)

    private val keys = mutableSetOf<String>()
    private val holders = mutableListOf<PathHolder>()
    private var holdersIterator: Iterator<PathHolder>? = null

    private val time = 1000L
    private val frames = 60
    private val frameDuration = time / frames.toFloat()
    private val frameDelay = 16L

    private var isInitialized = false
    private var wasAnimatingWhenDetached = false
    private var wasAnimatingWhenNotShown = false
    private var autoPlay = true

    private val initializeValuesAction = PublishRelay.create<Unit>()
    private val compositeDisposable = CompositeDisposable()

    private var isAnimating = false
    private val animationHandler = Handler()
    private val animation = object : Runnable {
        override fun run() {
            holdersIterator?.let {
                val holder = it.next()
                holder?.let { h ->
                    oval1.path = h.p1
                    oval2.path = h.p2
                    oval3.path = h.p3
                    invalidate()
                    animationHandler.postDelayed(this, frameDelay)
                }
            }
        }
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
        if (isAnimating) {
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
            if (isAnimating) {
                pauseAnimation()
                wasAnimatingWhenNotShown = true
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureOvals()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(oval3.path, largeOvalPaint)
        canvas.drawPath(oval2.path, smallOvalPaint)
        canvas.drawPath(oval1.path, smallOvalPaint)
    }

    fun playAnimation() {
        if (!isInitialized) return

        if (isShown) {
            isAnimating = true
            animationHandler.removeCallbacks(animation)
            animationHandler.post(animation)
        } else {
            wasAnimatingWhenNotShown = true
        }
    }

    fun resumeAnimation() {
        if (!isInitialized) return

        if (isShown) {
            isAnimating = true
            animationHandler.removeCallbacks(animation)
            animationHandler.post(animation)
        } else {
            wasAnimatingWhenNotShown = true
        }
    }

    fun cancelAnimation() {
        if (!isInitialized) return

        wasAnimatingWhenNotShown = false
        isAnimating = false
        animationHandler.removeCallbacks(animation)
    }

    fun pauseAnimation() {
        if (!isInitialized) return

        autoPlay = false
        wasAnimatingWhenDetached = false
        wasAnimatingWhenNotShown = false
        isAnimating = false
        animationHandler.removeCallbacks(animation)
    }

    private fun measureOvals() {
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
    }

    private fun initializeValues() {

        initializeValues(0f, 2f, 8000f, frameDuration, smallAngleValues.list)
        initializeValues(0.65f, 0.8f, 4000f, frameDuration, largeAngleValues.list)

        initializeValues(smallOvalSize.xMin, smallOvalSize.xMax, 2500f, frameDuration, xRValues.list)
        initializeValues(smallOvalSize.yMin, smallOvalSize.yMax, 2000f, frameDuration, yRValues.list)

        initializeValues(largeOvalSize.xMin, largeOvalSize.xMax, 2500f, frameDuration, xRLargeValues.list)
        initializeValues(largeOvalSize.yMin, largeOvalSize.yMax, 2000f, frameDuration, yRLargeValues.list)

        val max0 = max(smallAngleValues.list.size, largeAngleValues.list.size)
        val max1 = max(xRValues.list.size, yRValues.list.size)
        val max2 = max(xRLargeValues.list.size, yRLargeValues.list.size)

        val max = maxOf(max0, max1, max2)

        (0 until max).forEach {

            val smallAngleValue = smallAngleValues.next()
            val largeAngleValue = largeAngleValues.next()

            val xRValue = xRValues.next()
            val yRValue = yRValues.next()

            val xRLargeValue = xRLargeValues.next()
            val yRLargeValue = yRLargeValues.next()

            val key = "$smallAngleValue-$largeAngleValue-$xRValue-$yRValue-$xRLargeValue-$yRLargeValue"
            keys.add(key).also { added ->
                if (added) {
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

            i += 0.05f
        }
        return holder
    }

    private fun initializeValues(
        min: Float,
        max: Float,
        duration: Float,
        frameDuration: Float,
        values: MutableList<Float>
    ) {
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

    class Values(val mirror: Boolean) {

        private var endlessIterator: Iterator<Float>? = null
        private var forwardBackwardIterator: Iterator<Float>? = null

        val list = mutableListOf<Float>()

        fun next(): Float {

            if (mirror && forwardBackwardIterator == null) {
                forwardBackwardIterator = list.forwardBackwardIterator()
            }

            if (!mirror && endlessIterator == null) {
                endlessIterator = list.endlessIterator()
            }

            return if (mirror) forwardBackwardIterator?.next() ?: 0f else endlessIterator?.next()
                ?: 0f
        }
    }

    class PathHolder(
        val p1: Path = Path(),
        val p2: Path = Path(),
        val p3: Path = Path()
    ) {
        inline fun getPath(index: Int): Path = if (index == 0) p1 else if (index == 1) p2 else p3
    }

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

    class EndlessIterator<T>(private val list: List<T>, private val count: Int = -1) : Iterator<T> {

        private val size = list.size
        private var current = 0
        private var iteration = 0

        override fun hasNext(): Boolean = if (count == -1) true else iteration < count

        override fun next(): T {
            val item = list[current++]
            if (current == size) {
                current = 0
                iteration++
            }
            return item
        }
    }

    companion object {
        val PI_FLOAT = PI.toFloat()
        val END = 2 * PI_FLOAT
    }
}

fun <T> List<T>.forwardBackwardIterator(count: Int = -1): Iterator<T> =
    MorphView.ForwardBackwardIterator(this, count)

fun <T> List<T>.endlessIterator(count: Int = -1): Iterator<T> =
    MorphView.EndlessIterator(this, count)