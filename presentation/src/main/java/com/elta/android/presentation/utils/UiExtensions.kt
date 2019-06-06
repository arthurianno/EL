package com.elta.android.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.support.v4.content.ContextCompat
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
import com.nullgr.core.ui.extensions.dpToPx
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

fun View.getBitmapFromView(size: Float): Bitmap {
    val sizePx = size.dpToPx(context).toInt()
    val measureSpec = View.MeasureSpec.makeMeasureSpec(sizePx, View.MeasureSpec.EXACTLY)
    measure(measureSpec, measureSpec)
    layout(0, 0, measuredWidth, measuredHeight)
    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
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
                        .filter { it.action == MotionEvent.ACTION_UP || it.action == MotionEvent.ACTION_CANCEL }
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

fun Context.decodeBitmap(drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(this, drawableId)
    return when (drawable) {
        is BitmapDrawable -> BitmapFactory.decodeResource(resources, drawableId)
        is VectorDrawable -> drawable.toBitmap()
        else -> throw IllegalArgumentException("unsupported drawable type")
    }
}

fun VectorDrawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}