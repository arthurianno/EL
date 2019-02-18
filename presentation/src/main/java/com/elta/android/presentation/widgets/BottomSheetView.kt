package com.elta.android.presentation.widgets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.checkMainThread
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.layout_bottom_sheet_view.view.*
import java.util.concurrent.TimeUnit

class BottomSheetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    private val compositeDisposable = CompositeDisposable()
    private val visibilityChanges = PublishRelay.create<Boolean>()
    private val showAction = PublishRelay.create<Unit>()
    private val hideAction = PublishRelay.create<Unit>()

    private val color1 = Color.TRANSPARENT
    private val color2 = Color.parseColor("#B3000000")
    private val inAnimator: ObjectAnimator
    private var outAnimator: ObjectAnimator
    private val colorEvaluator = ArgbEvaluator()
    private val inColorAnimationDuration: Long = 400
    private val outColorAnimationDuration: Long = 500
    private val debounce: Long = 400

    private var bottomSheetLayout: Int = 0
    private val behavior: BottomSheetBehavior<FrameLayout>
    private val isShowing: Boolean
        get() = behavior.state != BottomSheetBehavior.STATE_HIDDEN

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_view, this, true)

        attrs?.let {
            val array = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetView, 0, 0)
            bottomSheetLayout = array.getResourceId(R.styleable.BottomSheetView_bottom_sheet_layout, 0)
            array.recycle()
        }

        val sheet = LayoutInflater.from(context).inflate(bottomSheetLayout, bottomSheetContainer, false)

        bottomSheetContainer.addView(sheet)

        behavior = BottomSheetBehavior.from(bottomSheetContainer)
        behavior.peekHeight = 0
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        visibility = View.INVISIBLE

        touchOutsideView.setOnClickListener {
            if (isShowing) {
                hide()
            }
        }

        bottomSheetContainer.setOnTouchListener { _, _ ->
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

        Observables.combineLatest(
            BottomSheetStateObservable(behavior).filter { it is BottomSheetEvent.State },
            AnimationEventObservable(outAnimator).filter { it is AnimationEvent.End }
        )
            .filter { visibility == View.VISIBLE }
            .subscribe {
                val bottomSheetEvent = it.first
                val animationEvent = it.second

                if (bottomSheetEvent is BottomSheetEvent.State && animationEvent is AnimationEvent.End) {
                    val state = bottomSheetEvent.state
                    if (state == BottomSheetBehavior.STATE_HIDDEN || state == BottomSheetBehavior.STATE_COLLAPSED) {
                        visibility = View.INVISIBLE
                        visibilityChanges.accept(false)
                    }
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
        hideAction.accept(Unit)
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
        visibility = View.VISIBLE
        behavior.expand()

        inAnimator.cancel()
        outAnimator.cancel()

        inAnimator.start()
        visibilityChanges.accept(true)
    }

    private fun hideInternal(i: Unit) {
        behavior.hide()

        inAnimator.cancel()
        outAnimator.cancel()

        outAnimator.start()
        visibilityChanges.accept(false)
    }

    inline val View.backgroundColor: Int
        get() = (background as? ColorDrawable)?.color ?: Color.TRANSPARENT

    // bottom sheet

    inline fun <T : View> BottomSheetBehavior<T>.expand() {
        this.state = BottomSheetBehavior.STATE_EXPANDED
    }

    inline fun <T : View> BottomSheetBehavior<T>.collaps() {
        this.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    inline fun <T : View> BottomSheetBehavior<T>.hide() {
        this.state = BottomSheetBehavior.STATE_HIDDEN
    }

    inline fun Int.stateAsString(): String =
        when (this) {
            BottomSheetBehavior.STATE_HIDDEN -> "HIDDEN"
            BottomSheetBehavior.STATE_EXPANDED -> "EXPANDED"
            BottomSheetBehavior.STATE_COLLAPSED -> "COLLAPSED"
            BottomSheetBehavior.STATE_DRAGGING -> "DRAGGING"
            BottomSheetBehavior.STATE_HALF_EXPANDED -> "HALF_EXPANDED"
            BottomSheetBehavior.STATE_SETTLING -> "SETTLING"
            else -> "empty"
        }

    sealed class BottomSheetEvent(val view: View) {
        class Slide(view: View, val offset: Float) : BottomSheetEvent(view)
        class State(view: View, val state: Int) : BottomSheetEvent(view)
    }

    private class BottomSheetStateObservable(
        private val behavior: BottomSheetBehavior<out View>
    ) : Observable<BottomSheetEvent>() {

        override fun subscribeActual(observer: Observer<in BottomSheetEvent>) {
            if (!checkMainThread(observer)) {
                return
            }
            val listener = Listener(behavior, observer)
            observer.onSubscribe(listener)
            behavior.setBottomSheetCallback(listener.bottomSheetCallback)
        }

        class Listener(
            private val behavior: BottomSheetBehavior<out View>,
            observer: Observer<in BottomSheetEvent>
        ) : MainThreadDisposable() {

            val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(view: View, offset: Float) {
                    if (!isDisposed) observer.onNext(BottomSheetEvent.Slide(view, offset))
                }

                override fun onStateChanged(view: View, state: Int) {
                    if (!isDisposed) observer.onNext(BottomSheetEvent.State(view, state))
                }
            }

            override fun onDispose() {
                behavior.setBottomSheetCallback(null)
            }
        }
    }

    // animation

    sealed class AnimationEvent(val animator: Animator) {
        class Cancel(animator: Animator) : AnimationEvent(animator)
        class End(animator: Animator) : AnimationEvent(animator)
        class Repeat(animator: Animator) : AnimationEvent(animator)
        class Start(animator: Animator) : AnimationEvent(animator)
        class Pause(animator: Animator) : AnimationEvent(animator)
        class Resume(animator: Animator) : AnimationEvent(animator)
    }

    private class AnimationEventObservable(
        private val animator: Animator
    ) : Observable<AnimationEvent>() {

        override fun subscribeActual(observer: Observer<in AnimationEvent>) {
            if (!checkMainThread(observer)) {
                return
            }
            val listener = Listener(animator, observer)
            observer.onSubscribe(listener)
            animator.addListener(listener.animationListener)
        }

        class Listener(
            private val animator: Animator,
            observer: Observer<in AnimationEvent>
        ) : MainThreadDisposable() {

            val animationListener = object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    if (!isDisposed) observer.onNext(AnimationEvent.Cancel(animator))
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!isDisposed) observer.onNext(AnimationEvent.End(animator))
                }

                override fun onAnimationRepeat(animation: Animator) {
                    if (!isDisposed) observer.onNext(AnimationEvent.Repeat(animator))
                }

                override fun onAnimationStart(animation: Animator) {
                    if (!isDisposed) observer.onNext(AnimationEvent.Start(animator))
                }

                override fun onAnimationPause(animation: Animator) {
                    if (!isDisposed) observer.onNext(AnimationEvent.Pause(animator))
                }

                override fun onAnimationResume(animation: Animator) {
                    if (!isDisposed) observer.onNext(AnimationEvent.Resume(animator))
                }
            }

            override fun onDispose() {
                animator.removeListener(animationListener)
            }
        }
    }
}