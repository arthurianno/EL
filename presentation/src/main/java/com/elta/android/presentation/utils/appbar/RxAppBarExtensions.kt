package com.elta.android.presentation.utils.appbar

import android.support.design.widget.AppBarLayout
import com.elta.android.presentation.utils.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

fun AppBarLayout.observeState(): Observable<AppBarState> =
    AppBarLayoutStateChangeObservable(this)

fun AppBarLayout.offsetChanges(): Observable<Pair<AppBarLayout, Int>> =
    AppBarOffsetChangeObservable(this)

@Suppress("MagicNumber")
fun AppBarLayout.collapseProgress(): Observable<Int> =
    AppBarOffsetChangeObservable(this)
        .map {
            val totalRange = it.first.totalScrollRange
            if (totalRange > 0) {
                it.second * 100 / totalRange
            } else {
                0
            }
        }

private class AppBarLayoutStateChangeObservable(
    private val view: AppBarLayout
) : Observable<AppBarState>() {

    override fun subscribeActual(observer: Observer<in AppBarState>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        view.addOnOffsetChangedListener(listener.stateListener)
    }

    class Listener(
        private val view: AppBarLayout,
        observer: Observer<in AppBarState>
    ) : MainThreadDisposable() {

        val stateListener = object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: AppBarState) {
                if (!isDisposed)
                    observer.onNext(state)
            }
        }

        override fun onDispose() {
            view.removeOnOffsetChangedListener(stateListener)
        }
    }
}

private class AppBarOffsetChangeObservable(
    private val view: AppBarLayout
) : Observable<Pair<AppBarLayout, Int>>() {

    override fun subscribeActual(observer: Observer<in Pair<AppBarLayout, Int>>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        view.addOnOffsetChangedListener(listener.offsetListener)
    }

    class Listener(
        private val view: AppBarLayout,
        observer: Observer<in Pair<AppBarLayout, Int>>
    ) : MainThreadDisposable() {

        val offsetListener = AppBarLayout.OnOffsetChangedListener { appBar, offset ->
            if (!isDisposed)
                observer.onNext(appBar to offset)
        }

        override fun onDispose() {
            view.removeOnOffsetChangedListener(offsetListener)
        }
    }
}