package com.elta.android.presentation.utils.appbar

import android.support.design.widget.AppBarLayout
import com.elta.android.presentation.utils.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

fun AppBarLayout.observeState(): Observable<AppBarState> =
    AppBarLayoutStateChangeObservable(this)

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
