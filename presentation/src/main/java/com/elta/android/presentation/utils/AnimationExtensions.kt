package com.elta.android.presentation.utils

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import com.elta.android.presentation.R
import com.nullgr.core.ui.animation.doOnCancel
import io.reactivex.functions.Consumer

private val progressEvaluator by lazy(LazyThreadSafetyMode.NONE) {
    TypeEvaluator<Int> { fraction, startValue, endValue -> Math.round(startValue + (endValue - startValue) * fraction) }
}
private val progressInterpolator by lazy(LazyThreadSafetyMode.NONE) { AccelerateDecelerateInterpolator() }
private const val ANIMATION_DURATION_MILLIS = 600L

private const val FADING_OUT = 1
private const val FADING_IN = 2
private val FADING_TAG = R.id.fadeAnimationTag

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

fun View.fadeVisibility(visibilityWhenFalse: Int = View.GONE): Consumer<in Boolean> = Consumer {
    if (it) {
        if (visibility != View.VISIBLE && getTag(FADING_TAG) != FADING_OUT) {
            animate().cancel()
            visibility = View.VISIBLE
            setTag(FADING_TAG, FADING_OUT)
            animate()
                .alpha(1f)
                .start()
        }
    } else {
        if (visibility == View.VISIBLE && getTag(FADING_TAG) != FADING_IN) {
            animate().cancel()
            setTag(FADING_TAG, FADING_IN)
            animate()
                .alpha(0f)
                .withEndAction {
                    visibility = visibilityWhenFalse
                }
                .start()
        }
    }
}