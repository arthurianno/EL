package com.elta.android.presentation.widgets.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import com.elta.android.presentation.utils.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

class AnimatorEventObservable(
    private val animator: Animator
) : Observable<AnimatorEvent>() {

    override fun subscribeActual(observer: Observer<in AnimatorEvent>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(animator, observer)
        observer.onSubscribe(listener)
        animator.addListener(listener.animationListener)
    }

    class Listener(
        private val animator: Animator,
        observer: Observer<in AnimatorEvent>
    ) : MainThreadDisposable() {

        val animationListener = object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                if (!isDisposed) observer.onNext(AnimatorEvent.Cancel(animator))
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!isDisposed) observer.onNext(AnimatorEvent.End(animator))
            }

            override fun onAnimationRepeat(animation: Animator) {
                if (!isDisposed) observer.onNext(AnimatorEvent.Repeat(animator))
            }

            override fun onAnimationStart(animation: Animator) {
                if (!isDisposed) observer.onNext(AnimatorEvent.Start(animator))
            }

            override fun onAnimationPause(animation: Animator) {
                if (!isDisposed) observer.onNext(AnimatorEvent.Pause(animator))
            }

            override fun onAnimationResume(animation: Animator) {
                if (!isDisposed) observer.onNext(AnimatorEvent.Resume(animator))
            }
        }

        override fun onDispose() {
            animator.removeListener(animationListener)
        }
    }
}