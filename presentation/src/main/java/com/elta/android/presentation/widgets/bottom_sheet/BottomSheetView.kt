package com.elta.android.presentation.widgets.bottom_sheet

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.LayoutBottomSheetViewBinding
import com.elta.android.presentation.widgets.animation.AnimatorEvent
import com.elta.android.presentation.widgets.animation.AnimatorEventObservable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class BottomSheetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    private val compositeDisposable = CompositeDisposable()
    private val visibilityChanges = PublishRelay.create<Boolean>()
    private val showAction = PublishRelay.create<Unit>()
    private val hideAction = PublishRelay.create<Boolean>()

    private val color1 = Color.TRANSPARENT
    private val color2 = Color.parseColor("#B3000000")
    private val inAnimator: ObjectAnimator
    private var outAnimator: ObjectAnimator
    private val colorEvaluator = ArgbEvaluator()
    private val inColorAnimationDuration: Long = IN_ANIMATION_DURATION
    private val outColorAnimationDuration: Long = OUT_ANIMATION_DURATION
    private val debounce: Long = DEBOUNCE_MILLIS

    private var bottomSheetLayout: Int = 0
    private val behavior: BottomSheetBehavior<FrameLayout>
    private val isShowing: Boolean
        get() = behavior.state != BottomSheetBehavior.STATE_HIDDEN
    private var isOutAnimationEnded: Boolean = false
    private var isInAnimationStarted: Boolean = false
    private val binding: LayoutBottomSheetViewBinding by lazy {
        LayoutBottomSheetViewBinding.bind(this)
    }

    init {
        val inflater = LayoutInflater.from(context)

        inflater.inflate(R.layout.layout_bottom_sheet_view, this, true)

        attrs?.let {
            val array = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetView, 0, 0)
            bottomSheetLayout =
                array.getResourceId(R.styleable.BottomSheetView_bottom_sheet_layout, 0)
            array.recycle()
        }

        val sheetView = inflater.inflate(bottomSheetLayout, binding.bottomSheetContainer, false)

        binding.bottomSheetContainer.addView(sheetView)

        behavior = BottomSheetBehavior.from(binding.bottomSheetContainer)
        behavior.peekHeight = 0
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        setBackgroundColor(color1)
        visibility = View.INVISIBLE

        binding.touchOutsideView.setOnClickListener {
            if (isShowing) {
                hideInternal(false)
            }
        }

        binding.bottomSheetContainer.setOnTouchListener { _, _ ->
            // Consume the event and prevent it from falling through
            true
        }

        inAnimator = ObjectAnimator.ofInt(this, "backgroundColor", color1, color2)
        inAnimator.setEvaluator(colorEvaluator)
        inAnimator.duration = inColorAnimationDuration

        outAnimator = ObjectAnimator.ofInt(this, "backgroundColor", color2, color1)
        outAnimator.setEvaluator(colorEvaluator)
        outAnimator.duration = outColorAnimationDuration
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        showAction
            .throttleFirst(debounce, TimeUnit.MILLISECONDS)
            .subscribe(::showInternal)
            .addTo(compositeDisposable)

        hideAction
            .throttleFirst(debounce, TimeUnit.MILLISECONDS)
            .subscribe(::hideInternal)
            .addTo(compositeDisposable)

        AnimatorEventObservable(inAnimator)
            .subscribe {
                isInAnimationStarted = it is AnimatorEvent.Start
                changeVisibility()
            }.addTo(compositeDisposable)

        AnimatorEventObservable(outAnimator)
            .subscribe {
                isOutAnimationEnded = it is AnimatorEvent.End
                changeVisibility()
            }.addTo(compositeDisposable)

        BottomSheetStateObservable(behavior)
            .subscribe {
                changeVisibility()
                if (it is BottomSheetEvent.StateChanged && it.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    hideInternal(true)
                }
            }.addTo(compositeDisposable)
    }

    override fun onDetachedFromWindow() {
        compositeDisposable.clear()
        super.onDetachedFromWindow()
    }

    fun show() {
        showAction.accept(Unit)
    }

    fun hide() {
        hideAction.accept(false)
    }

    fun visibilityChanges(): Observable<Boolean> = visibilityChanges

    fun handleBack(): Boolean {
        if (isShowing) {
            hide()
            return true
        }
        return false
    }

    private fun showInternal(i: Unit) {
        inAnimator.cancel()
        outAnimator.cancel()

        inAnimator.start()
        behavior.expand()

        visibilityChanges.accept(true)
    }

    private fun hideInternal(fast: Boolean) {
        inAnimator.cancel()
        outAnimator.cancel()

        outAnimator.duration = if (fast) OUT_FAST_ANIMATION_DURATION else OUT_ANIMATION_DURATION

        outAnimator.start()
        behavior.hide()

        visibilityChanges.accept(false)
    }

    private fun changeVisibility() {
        if (!isShowing && isOutAnimationEnded) {
            visibility = View.INVISIBLE
        } else if (isInAnimationStarted) {
            visibility = View.VISIBLE
        }
    }

    companion object {
        private const val IN_ANIMATION_DURATION = 400L
        private const val OUT_ANIMATION_DURATION = 500L
        private const val OUT_FAST_ANIMATION_DURATION = 300L
        private const val DEBOUNCE_MILLIS = 300L
    }
}
