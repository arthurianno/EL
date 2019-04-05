package com.elta.android.presentation.widgets.range_bar.listeners

import com.elta.android.presentation.utils.checkMainThread
import com.elta.android.presentation.widgets.range_bar.RangeBarView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

class RangeValuesChangedObserver(
    private val view: RangeBarView
) : Observable<Pair<Double, Double>>() {

    override fun subscribeActual(observer: Observer<in Pair<Double, Double>>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        listener.valueListener?.onValuesChanged(view.startValue, view.endValue)
        listener.valueListener?.let {
            view.addOnValuesChangeListener(it)
        }
    }

    class Listener(
        private val view: RangeBarView,
        observer: Observer<in Pair<Double, Double>>
    ) : MainThreadDisposable() {

        var valueListener: OnRageBarValuesChangeListener? = object : OnRageBarValuesChangeListener {
            override fun onValuesChanged(start: Double, end: Double) {
                if (!isDisposed)
                    observer.onNext(start to end)
            }
        }

        override fun onDispose() {
            valueListener?.let { view.removeOnValuesChangeListener(it) }
            valueListener = null
        }
    }
}
