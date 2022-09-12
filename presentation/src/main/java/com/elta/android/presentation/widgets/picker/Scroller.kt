package com.elta.android.presentation.widgets.picker

import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.view.ViewConfiguration
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

private const val INFLEXION = 0.35f // Tension lines cross at (INFLEXION, 1)
private const val START_TENSION = 0.5f
private const val END_TENSION = 1.0f
private const val P1 = START_TENSION * INFLEXION
private const val P2 = 1.0f - END_TENSION * (1.0f - INFLEXION)
private const val NB_SAMPLES = 100
private const val DEFAULT_DURATION = 250
private const val SCROLL_MODE = 0
private const val FLING_MODE = 1
private val DECELERATION_RATE = (ln(0.78) / ln(0.9)).toFloat()
private val SPLINE_POSITION = FloatArray(NB_SAMPLES + 1)
private val SPLINE_TIME = FloatArray(NB_SAMPLES + 1)
private const val VISCOUS_FLUID_SCALE = 8.0f

/**
 *
 * This class encapsulates scrolling. You can use scrollers ([Scroller]
 * to collect the data you need to produce a scrolling animationfor
 * example, in response to a fling gesture. Scrollers track scroll offsets
 * for you over time, but they don't automatically apply those positions
 * to your view. It's your responsibility to get and apply new coordinates
 * at a rate that will make the scrolling animation look smooth.
 *
 *
 * Here is a simple example:
 *
 * <pre> private Scroller mScroller = new Scroller(context);
 * ...
 * public void zoomIn() {
 * // Revert any animation currently in progress
 * mScroller.forceFinished(true);
 * // Start scrolling by providing a starting point and
 * // the distance to travel
 * mScroller.startScroll(0, 0, 100, 0);
 * // Invalidate to request a redraw
 * invalidate();
 * }</pre>
 *
 *
 * To track the changing positions of the x/y coordinates, use
 * [.computeScrollOffset]. The method returns a boolean to indicate
 * whether the scroller is finished. If it isn't, it means that a fling or
 * programmatic pan operation is still in progress. You can use this method to
 * find the current offsets of the x and y coordinates, for example:
 *
 * <pre>if (mScroller.computeScrollOffset()) {
 * // Get current x and y positions
 * int currX = mScroller.getCurrX();
 * int currY = mScroller.getCurrY();
 * ...
 * }</pre>
 */
class Scroller @JvmOverloads constructor(
    context: Context,
    interpolator: Interpolator? = null,
    private val flywheel: Boolean =
        context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.HONEYCOMB
) {
    private var interpolator: Interpolator = interpolator ?: ViscousFluidInterpolator()
    private var mode = 0
    var startX = 0
        private set
    var startY = 0
        private set
    var finalX = 0
        private set
    var finalY = 0
        private set
    private var minX = 0
    private var maxX = 0
    private var minY = 0
    private var maxY = 0
    var currX = 0
        private set
    var currY = 0
        private set
    private var startTime: Long = 0
    var duration = 0
        private set
    private var durationReciprocal = 0f
    private var deltaX = 0f
    private var deltaY = 0f
    var isFinished = true
        private set
    private var velocity = 0f
    private var currVelocity = 0f
    private var distance = 0
    private var flingFriction = ViewConfiguration.getScrollFriction()
    private var deceleration: Float = computeDeceleration(ViewConfiguration.getScrollFriction())
    private val ppi: Float = context.resources.displayMetrics.density * 160.0f

    // A context-specific coefficient adjusted to physical values.
    private val physicalCoeff: Float = computeDeceleration(0.84f) // look and feel tuning

    init {
        setupValues()
    }

    private fun setupValues() {
        var xMin = 0.0f
        var yMin = 0.0f
        for (i in 0 until NB_SAMPLES) {
            val alpha = i.toFloat() / NB_SAMPLES
            var xMax = 1.0f
            var x: Float
            var tx: Float
            var coef: Float
            while (true) {
                x = xMin + (xMax - xMin) / 2.0f
                coef = 3.0f * x * (1.0f - x)
                tx = coef * ((1.0f - x) * P1 + x * P2) + x * x * x
                if (abs(tx - alpha) < 1E-5) break
                if (tx > alpha) {
                    xMax = x
                } else {
                    xMin = x
                }
            }
            SPLINE_POSITION[i] =
                coef * ((1.0f - x) * START_TENSION + x) + x * x * x
            var yMax = 1.0f
            var y: Float
            var dy: Float
            while (true) {
                y = yMin + (yMax - yMin) / 2.0f
                coef = 3.0f * y * (1.0f - y)
                dy = coef * ((1.0f - y) * START_TENSION + y) + y * y * y
                if (abs(dy - alpha) < 1E-5) break
                if (dy > alpha) {
                    yMax = y
                } else {
                    yMin = y
                }
            }
            SPLINE_TIME[i] = coef * ((1.0f - y) * P1 + y * P2) + y * y * y
        }
        SPLINE_TIME[NB_SAMPLES] = 1.0f
        SPLINE_POSITION[NB_SAMPLES] = SPLINE_TIME[NB_SAMPLES]
    }

    fun setFriction(friction: Float) {
        deceleration = computeDeceleration(friction)
        flingFriction = friction
    }

    private fun computeDeceleration(friction: Float): Float {
        return (
            SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.37f * // inch/meter
                ppi // pixels per inch
                * friction
            )
    }

    fun forceFinished(finished: Boolean) {
        this.isFinished = finished
    }

    fun getCurrVelocity(): Float {
        return if (mode == FLING_MODE) currVelocity else velocity - deceleration * timePassed() / 2000.0f
    }

    fun computeScrollOffset(): Boolean {
        if (isFinished) {
            return false
        }
        val timePassed = (AnimationUtils.currentAnimationTimeMillis() - startTime).toInt()
        if (timePassed < duration) {
            when (mode) {
                SCROLL_MODE -> {
                    val x = interpolator.getInterpolation(timePassed * durationReciprocal)
                    currX = startX + (x * deltaX).roundToInt()
                    currY = startY + (x * deltaY).roundToInt()
                }
                FLING_MODE -> {
                    val t = timePassed.toFloat() / duration
                    val index = (NB_SAMPLES * t).toInt()
                    var distanceCoef = 1f
                    var velocityCoef = 0f
                    if (index < NB_SAMPLES) {
                        val tInf = index.toFloat() / NB_SAMPLES
                        val tSup = (index + 1).toFloat() / NB_SAMPLES
                        val dInf = SPLINE_POSITION[index]
                        val dSup = SPLINE_POSITION[index + 1]
                        velocityCoef = (dSup - dInf) / (tSup - tInf)
                        distanceCoef = dInf + (t - tInf) * velocityCoef
                    }
                    currVelocity = velocityCoef * distance / duration * 1000.0f
                    currX = startX + (distanceCoef * (finalX - startX)).roundToInt()
                    // Pin to mMinX <= mCurrX <= mMaxX
                    currX = min(currX, maxX)
                    currX = max(currX, minX)
                    currY = startY + (distanceCoef * (finalY - startY)).roundToInt()
                    // Pin to mMinY <= mCurrY <= mMaxY
                    currY = min(currY, maxY)
                    currY = max(currY, minY)
                    if (currX == finalX && currY == finalY) {
                        isFinished = true
                    }
                }
            }
        } else {
            currX = finalX
            currY = finalY
            isFinished = true
        }
        return true
    }

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     * The scroll will use the default value of 250 milliseconds for the
     * duration.
     *
     * @param startX Starting horizontal scroll offset in pixels. Positive
     * numbers will scroll the content to the left.
     * @param startY Starting vertical scroll offset in pixels. Positive numbers
     * will scroll the content up.
     * @param dx     Horizontal distance to travel. Positive numbers will scroll the
     * content to the left.
     * @param dy     Vertical distance to travel. Positive numbers will scroll the
     * content up.
     */
    fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int = DEFAULT_DURATION) {
        mode = SCROLL_MODE
        isFinished = false
        this.duration = duration
        startTime = AnimationUtils.currentAnimationTimeMillis()
        this.startX = startX
        this.startY = startY
        finalX = startX + dx
        finalY = startY + dy
        deltaX = dx.toFloat()
        deltaY = dy.toFloat()
        durationReciprocal = 1.0f / this.duration.toFloat()
    }

    /**
     * Start scrolling based on a fling gesture. The distance travelled will
     * depend on the initial velocity of the fling.
     *
     * @param startX    Starting point of the scroll (X)
     * @param startY    Starting point of the scroll (Y)
     * @param velocityX Initial velocity of the fling (X) measured in pixels per
     * second.
     * @param velocityY Initial velocity of the fling (Y) measured in pixels per
     * second
     * @param minX      Minimum X value. The scroller will not scroll past this
     * point.
     * @param maxX      Maximum X value. The scroller will not scroll past this
     * point.
     * @param minY      Minimum Y value. The scroller will not scroll past this
     * point.
     * @param maxY      Maximum Y value. The scroller will not scroll past this
     * point.
     */
    fun fling(
        startX: Int,
        startY: Int,
        velocityX: Int,
        velocityY: Int,
        minX: Int,
        maxX: Int,
        minY: Int,
        maxY: Int
    ) {
        // Continue a scroll or fling in progress
        var localVelocityX = velocityX
        var localVelocityY = velocityY
        if (flywheel && !isFinished) {
            val oldVel = getCurrVelocity()
            val dx = (finalX - this.startX).toFloat()
            val dy = (finalY - this.startY).toFloat()
            val hyp = hypot(dx.toDouble(), dy.toDouble()).toFloat()
            val ndx = dx / hyp
            val ndy = dy / hyp
            val oldVelocityX = ndx * oldVel
            val oldVelocityY = ndy * oldVel
            if (sign(localVelocityX.toFloat()) == sign(oldVelocityX) &&
                sign(localVelocityY.toFloat()) == sign(oldVelocityY)
            ) {
                localVelocityX += oldVelocityX.toInt()
                localVelocityY += oldVelocityY.toInt()
            }
        }
        mode = FLING_MODE
        isFinished = false
        val velocity = hypot(localVelocityX.toDouble(), localVelocityY.toDouble()).toFloat()
        this.velocity = velocity
        duration = getSplineFlingDuration(velocity)
        startTime = AnimationUtils.currentAnimationTimeMillis()
        this.startX = startX
        this.startY = startY
        val coeffX = if (velocity == 0f) 1.0f else localVelocityX / velocity
        val coeffY = if (velocity == 0f) 1.0f else localVelocityY / velocity
        val totalDistance = getSplineFlingDistance(velocity)
        distance = (totalDistance * sign(velocity)).toInt()
        this.minX = minX
        this.maxX = maxX
        this.minY = minY
        this.maxY = maxY
        finalX = startX + (totalDistance * coeffX).roundToInt()
        // Pin to mMinX <= mFinalX <= mMaxX
        finalX = min(finalX, this.maxX)
        finalX = max(finalX, this.minX)
        finalY = startY + (totalDistance * coeffY).roundToInt()
        // Pin to mMinY <= mFinalY <= mMaxY
        finalY = min(finalY, this.maxY)
        finalY = max(finalY, this.minY)
    }

    private fun getSplineDeceleration(velocity: Float): Double =
        ln((INFLEXION * abs(velocity) / (flingFriction * physicalCoeff)).toDouble())

    private fun getSplineFlingDuration(velocity: Float): Int {
        val l = getSplineDeceleration(velocity)
        val decelMinusOne = DECELERATION_RATE - 1.0
        return (1000.0 * exp(l / decelMinusOne)).toInt()
    }

    private fun getSplineFlingDistance(velocity: Float): Double {
        val l = getSplineDeceleration(velocity)
        val decelMinusOne = DECELERATION_RATE - 1.0
        return flingFriction * physicalCoeff * exp(DECELERATION_RATE / decelMinusOne * l)
    }

    fun abortAnimation() {
        currX = finalX
        currY = finalY
        isFinished = true
    }

    /**
     * Extend the scroll animation. This allows a running animation to scroll
     * further and longer, when used with [.setFinalX] or [.setFinalY].
     *
     * @param extend Additional time to scroll in milliseconds.
     * @see .setFinalX
     * @see .setFinalY
     */
    fun extendDuration(extend: Int) {
        val passed = timePassed()
        duration = passed + extend
        durationReciprocal = 1.0f / duration
        isFinished = false
    }

    /**
     * Returns the time elapsed since the beginning of the scrolling.
     *
     * @return The elapsed time in milliseconds.
     */
    fun timePassed(): Int =
        (AnimationUtils.currentAnimationTimeMillis() - startTime).toInt()

    fun setFinalX(newX: Int) {
        finalX = newX
        deltaX = (finalX - startX).toFloat()
        isFinished = false
    }

    fun setFinalY(newY: Int) {
        finalY = newY
        deltaY = (finalY - startY).toFloat()
        isFinished = false
    }

    fun isScrollingInDirection(xvel: Float, yvel: Float): Boolean =
        !isFinished && sign(xvel) == sign((finalX - startX).toFloat()) &&
            sign(yvel) == sign((finalY - startY).toFloat())

    internal class ViscousFluidInterpolator : Interpolator {

        override fun getInterpolation(input: Float): Float {
            val interpolated = 1.0f / viscousFluid(1.0f) * viscousFluid(input)
            return if (interpolated > 0) {
                interpolated + (1.0f - 1.0f / viscousFluid(1.0f) * viscousFluid(1.0f))
            } else interpolated
        }

        private fun viscousFluid(x: Float): Float {
            var localX = x
            localX *= VISCOUS_FLUID_SCALE
            if (localX < 1.0f) {
                localX -= 1.0f - exp(-localX.toDouble()).toFloat()
            } else {
                val start = 0.36787944117f // 1/e == exp(-1)
                localX = 1.0f - exp((1.0f - localX).toDouble()).toFloat()
                localX = start + localX * (1.0f - start)
            }
            return localX
        }
    }
}
