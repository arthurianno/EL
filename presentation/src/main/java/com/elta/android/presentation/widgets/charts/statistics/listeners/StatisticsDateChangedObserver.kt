package com.elta.android.presentation.widgets.charts.statistics.listeners

import com.elta.android.presentation.utils.checkMainThread
import com.elta.android.presentation.widgets.charts.statistics.StatisticsChartView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import java.util.Date

class StatisticsDateChangedObserver(
    private val view: StatisticsChartView
) : Observable<Date>() {

    override fun subscribeActual(observer: Observer<in Date>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        listener.valueListener?.let {
            view.setOnStatisticsDateChangedListener(it)
        }
    }

    class Listener(
        private val view: StatisticsChartView,
        observer: Observer<in Date>
    ) : MainThreadDisposable() {

        var valueListener: OnStatisticsDateChangedListener? = object : OnStatisticsDateChangedListener {
            override fun onDateChanged(date: Date) {
                if (!isDisposed)
                    observer.onNext(date)
            }
        }

        override fun onDispose() {
            view.setOnStatisticsDateChangedListener(null)
            valueListener = null
        }
    }
}
