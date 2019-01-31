package com.elta.android.presentation.utils

import android.os.Looper
import android.support.annotation.CheckResult
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import io.reactivex.disposables.Disposables

fun RecyclerView.pageScrolled(): Observable<Int> =
    this.scrollStateChanges()
        .filter { it == RecyclerView.SCROLL_STATE_IDLE }
        .map { this.firstVisiblePosition() }

@CheckResult
fun RecyclerView.scrollStateChanges(): Observable<Int> =
    RecyclerViewScrollStateChangeObservable(this)

private class RecyclerViewScrollStateChangeObservable(
    private val view: RecyclerView
) : Observable<Int>() {

    override fun subscribeActual(observer: Observer<in Int>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(
            view, observer)
        observer.onSubscribe(listener)
        view.addOnScrollListener(listener.scrollListener)
    }

    class Listener(
        private val recyclerView: RecyclerView,
        observer: Observer<in Int>
    ) : MainThreadDisposable() {

        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!isDisposed) {
                    observer.onNext(newState)
                }
            }
        }

        override fun onDispose() {
            recyclerView.removeOnScrollListener(scrollListener)
        }
    }
}

fun checkMainThread(observer: Observer<*>): Boolean {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        observer.onSubscribe(Disposables.empty())
        observer.onError(IllegalStateException(
            "Expected to be called on the main thread but was " + Thread.currentThread().name))
        return false
    }
    return true
}

fun RecyclerView.firstVisiblePosition() =
    (this.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition() ?: 0