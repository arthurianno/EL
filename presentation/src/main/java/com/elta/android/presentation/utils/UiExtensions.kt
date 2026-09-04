package com.elta.android.presentation.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.util.TypedValue
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
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
    setImageResource(
        when (isSecure) {
            true -> R.drawable.ic_show_password
            else -> R.drawable.ic_password_hide
        }
    )
}

fun View.applyInsetsToContentView(fitsSystemWindows: Boolean) {
    this.fitsSystemWindows = fitsSystemWindows
    ViewCompat.requestApplyInsets(this)
}

/**
 * Keeps content clear of the status bar, gesture navigation, and the on-screen keyboard.
 * The initial padding is captured once, so receiving a new insets dispatch never
 * accumulates extra space after configuration changes.
 */
fun View.applySystemBarsInsetsPadding(applyStatusBarInset: Boolean) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusTop = if (applyStatusBarInset) {
            insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        } else {
            0
        }
        val navigationBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        view.updatePadding(
            left = initialLeft,
            top = initialTop + statusTop,
            right = initialRight,
            bottom = initialBottom + maxOf(navigationBottom, imeBottom)
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun View.applyStatusBarInsetsPadding(
    onApplyInsets: (View) -> Unit = {},
    applyNavigationBarInset: Boolean = false,
    applyImeInset: Boolean = false
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
        val bottomInset = when {
            applyImeInset && isImeVisible -> ime.bottom
            applyNavigationBarInset -> navigationBars.bottom
            else -> 0
        }
        view.updatePadding(
            left = initialLeft,
            top = initialTop + statusBars.top,
            right = initialRight,
            bottom = initialBottom + bottomInset
        )
        onApplyInsets(view)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun View.applyWindowInsetsForChildrenView() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val params = v.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = insets.systemWindowInsetTop
        insets.consumeSystemWindowInsets()
    }
}

fun NestedScrollView.scrollToBottom() {
    post { scrollTo(0, bottom) }
}

inline fun Activity.findAndClearFocus() = currentFocus?.clearFocus()

inline fun View.applyWindowBottomInsetsListener(listener: OnApplyWindowInsetsListener) {
    ViewCompat.setOnApplyWindowInsetsListener(this, listener)
}

inline fun View.removeWindowBottomInsetsListener(listener: OnApplyWindowInsetsListener) {
    ViewCompat.setOnApplyWindowInsetsListener(this, null)
}

object WindowBottomInsetsForViewListenerFactory {
    fun instance(vararg views: View, callback: (Int) -> Unit = {}) =
        OnApplyBottomWindowInsetsListener(views.toList(), callback)
}

class OnApplyBottomWindowInsetsListener(
    private val views: List<View>,
    private val callback: (Int) -> Unit
) : OnApplyWindowInsetsListener {

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val offset = getBottomOffset(insets)
        views.forEach { applyBottomOffsetToView(it, offset) }
        callback(offset)
        return insets
    }

    private fun getBottomOffset(insets: WindowInsetsCompat): Int {
        val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        val rawBottom = Math.max(systemBars, Math.max(navBars, ime))
        if (rawBottom > 0) return rawBottom

        return when {
            insets.systemWindowInsetBottom < insets.stableInsetBottom -> insets.systemWindowInsetBottom
            else -> insets.systemWindowInsetBottom
        }
    }

    private fun applyBottomOffsetToView(view: View, offset: Int) {
        val params = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        params.bottomMargin = offset
        view.layoutParams = params
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
    return when (val drawable = ContextCompat.getDrawable(this, drawableId)) {
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

inline fun <reified ViewClass> Activity.lostFocusOnClickOutside(event: MotionEvent, viewRoot: View): Boolean {
    if (event.action == MotionEvent.ACTION_DOWN) {
        currentFocus?.let {
            if (it::class.java == ViewClass::class.java) {
                val outRect = Rect()
                it.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    it.clearFocus()
                    val imm =
                        getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(it.windowToken, 0)
                    viewRoot.requestFocus()
                }
            }
        }
    }
    return true
}

fun Context.convertDpToPx(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        this.resources.displayMetrics
    )
}

fun Activity.isKeyboardOpen(viewRoot: View): Boolean {
    val visibleBounds = Rect()
    viewRoot.getWindowVisibleDisplayFrame(visibleBounds)
    val heightDiff = viewRoot.height - visibleBounds.height()
    val marginOfError = Math.round(this.convertDpToPx(50F))
    return heightDiff > marginOfError
}
