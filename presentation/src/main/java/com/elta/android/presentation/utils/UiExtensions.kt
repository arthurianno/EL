package com.elta.android.presentation.utils

import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.elta.android.presentation.R
import com.jakewharton.rxbinding2.view.longClicks
import com.jakewharton.rxbinding2.view.touches
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import java.util.concurrent.TimeUnit

const val SEQUENCE_CLICKS_PERIOD = 150L

fun ImageView.toggleSecureIcon(isSecure: Boolean) {
    setImageResource(when (isSecure) {
        true -> R.drawable.ic_show_password
        else -> R.drawable.ic_password_hide
    })
}

fun View.applyInsetsToContentView(fitsSystemWindows: Boolean) {
    this.fitsSystemWindows = fitsSystemWindows
    ViewCompat.requestApplyInsets(this)
}

fun View.applyWindowInsetsForChildrenView() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val params = v.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = insets.systemWindowInsetTop
        insets.consumeSystemWindowInsets()
    }
}

fun Toolbar.menuClicks(): Observable<Int> = ToolbarMenuClickObservable(this)

fun Toolbar.menuClicks(id: Int): Observable<Unit> =
    ToolbarMenuClickObservable(this)
        .filter { it == id }
        .map { Unit }

fun View.sequenceClicks(period: Long = SEQUENCE_CLICKS_PERIOD): Observable<Unit> =
    longClicks()
        .flatMap {
            Observable.interval(period, TimeUnit.MILLISECONDS)
                .takeUntil(
                    touches()
                        .filter { it.action == MotionEvent.ACTION_UP }
                        .doOnNext { isPressed = false }
                )
        }
        .map { Unit }

class ToolbarMenuClickObservable(private val toolbar: Toolbar) : Observable<Int>() {

    override fun subscribeActual(observer: Observer<in Int>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(toolbar, observer)
        observer.onSubscribe(listener)
        toolbar.setOnMenuItemClickListener(listener)
    }

    internal class Listener(
        private val toolbar: Toolbar,
        private val observer: Observer<in Int>
    ) : MainThreadDisposable(), Toolbar.OnMenuItemClickListener {

        override fun onMenuItemClick(item: MenuItem): Boolean {
            if (!isDisposed) {
                observer.onNext(item.itemId)
            }
            return true
        }

        override fun onDispose() {
            toolbar.setOnMenuItemClickListener(null)
        }
    }
}