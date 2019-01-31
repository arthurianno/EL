package com.elta.android.presentation.utils

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import com.elta.android.presentation.R
import com.nullgr.core.ui.animation.doOnCancel
import com.nullgr.core.ui.animation.doOnEnd
import io.reactivex.functions.Consumer

private val progressEvaluator by lazy(LazyThreadSafetyMode.NONE) {
    TypeEvaluator<Int> { fraction, startValue, endValue -> Math.round(startValue + (endValue - startValue) * fraction) }
}
private val progressInterpolator by lazy(LazyThreadSafetyMode.NONE) { AccelerateDecelerateInterpolator() }
private const val ANIMATION_DURATION_MILLIS = 600L
private const val TEXT_ANIMATION_DURATION_MILLIS = 300L

private const val FADING_OUT = 1
private const val FADING_IN = 2
private val fadingTag = R.id.fadeAnimationTag

fun ProgressBar.setProgressWithAnimation(to: Int, animate: Boolean) {
    when (animate) {
        true -> ValueAnimator.ofInt(0, to).apply {
            addUpdateListener { localAnimator ->
                progress = localAnimator.animatedValue as Int
            }
            doOnCancel {
                progress = to
            }
            interpolator = progressInterpolator
            setEvaluator(progressEvaluator)
            duration = ANIMATION_DURATION_MILLIS
            start()
        }
        else -> progress = to
    }
}

fun TextView.animateValue(value: Int, valueUnit: String? = null, animate: Boolean) {
    when (animate) {
        true -> ValueAnimator.ofInt(0, value)
            .apply {
                addUpdateListener { localAnimator ->
                    text = valueUnit?.let {
                        "${localAnimator.animatedValue}  $valueUnit"
                    } ?: localAnimator.animatedValue.toString()
                }
                interpolator = progressInterpolator
                setEvaluator(progressEvaluator)
                duration = ANIMATION_DURATION_MILLIS
                start()
            }
        else -> text = valueUnit?.let { "$value  $valueUnit" } ?: "$value"
    }
}

fun TextView.animateText(value: String) {
    this.animate()
        .alpha(0f)
        .setDuration(TEXT_ANIMATION_DURATION_MILLIS)
        .doOnEnd {
            this.text = value
            this.animate()
                .alpha(1f)
                .duration = TEXT_ANIMATION_DURATION_MILLIS
        }
}

fun View.fadeVisibility(visibilityWhenFalse: Int = View.GONE): Consumer<in Boolean> = Consumer {
    if (it) showViewWithFadeAnimation()
    else hideViewWithFadeAnimation(visibilityWhenFalse)
}

fun View.fadeVisibility(state: Boolean, visibilityWhenFalse: Int = View.GONE) {
    if (state) showViewWithFadeAnimation()
    else hideViewWithFadeAnimation(visibilityWhenFalse)
}

private fun View.showViewWithFadeAnimation() {
    if (visibility != View.VISIBLE && getTag(fadingTag) != FADING_OUT) {
        animate().cancel()
        visibility = View.VISIBLE
        setTag(fadingTag, FADING_OUT)
        animate()
            .alpha(1f)
            .start()
    }
}

private fun View.hideViewWithFadeAnimation(visibilityWhenFalse: Int = View.GONE) {
    if (visibility == View.VISIBLE && getTag(fadingTag) != FADING_IN) {
        animate().cancel()
        setTag(fadingTag, FADING_IN)
        animate()
            .alpha(0f)
            .withEndAction {
                visibility = visibilityWhenFalse
            }
            .start()
    }
}