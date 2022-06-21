package com.elta.android.presentation.widgets.datePicker

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.elta.android.presentation.utils.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

class SnapOnScrollListener(
    private val snapHelper: SnapHelper,
    private var behavior: Behavior = Behavior.NOTIFY_ON_SCROLL,
    private var onSnapPositionChangeListener: OnSnapPositionChangeListener? = null
) : RecyclerView.OnScrollListener() {

    enum class Behavior {
        NOTIFY_ON_SCROLL,
        NOTIFY_ON_SCROLL_STATE_IDLE
    }

    private var snapPosition = RecyclerView.NO_POSITION

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (behavior == Behavior.NOTIFY_ON_SCROLL) {
            maybeNotifySnapPositionChange(recyclerView)
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (behavior == Behavior.NOTIFY_ON_SCROLL_STATE_IDLE &&
            newState == RecyclerView.SCROLL_STATE_IDLE
        ) {
            maybeNotifySnapPositionChange(recyclerView)
        }
    }

    private fun maybeNotifySnapPositionChange(recyclerView: RecyclerView) {
        val snapPosition = snapHelper.getSnapPosition(recyclerView)
        val snapPositionChanged = this.snapPosition != snapPosition
        if (snapPositionChanged) {
            onSnapPositionChangeListener?.onSnapPositionChange(snapPosition)
            this.snapPosition = snapPosition
        }
    }
}

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    recyclerView.layoutManager?.let { layoutManger ->
        findSnapView(layoutManger)?.let {
            return layoutManger.getPosition(it)
        }
    }
    return RecyclerView.NO_POSITION
}

fun RecyclerView.attachSnapHelperWithListener(
    snapHelper: SnapHelper,
    behavior: SnapOnScrollListener.Behavior = SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL,
    onSnapPositionChangeListener: OnSnapPositionChangeListener
) {
    snapHelper.attachToRecyclerView(this)
    val snapOnScrollListener =
        SnapOnScrollListener(snapHelper, behavior, onSnapPositionChangeListener)
    addOnScrollListener(snapOnScrollListener)
}

class PageChangeListener(
    private val view: RecyclerView,
    private val snapHelper: SnapHelper
) : Observable<Int>() {

    override fun subscribeActual(observer: Observer<in Int>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(view, snapHelper, observer)
        observer.onSubscribe(listener)
        view.addOnScrollListener(listener.snapPositionChangeListener)
    }

    class Listener(
        private val view: RecyclerView,
        snapHelper: SnapHelper,
        observer: Observer<in Int>
    ) : MainThreadDisposable() {

        val snapPositionChangeListener = SnapOnScrollListener(
            snapHelper,
            SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_STATE_IDLE,
            object : OnSnapPositionChangeListener {
                override fun onSnapPositionChange(position: Int) {
                    if (!isDisposed)
                        observer.onNext(position)
                }
            }
        )

        override fun onDispose() {
            view.removeOnScrollListener(snapPositionChangeListener)
        }
    }
}
