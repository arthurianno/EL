package com.elta.android.presentation.widgets.charts.statistics.listeners

import com.elta.android.presentation.utils.checkMainThread
import com.elta.android.presentation.widgets.charts.statistics.StatisticsChartView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import org.threeten.bp.LocalDate

class StatisticsDateChangedObserver(
    private val view: StatisticsChartView
) : Observable<StatisticsSelectionResult>() {

    override fun subscribeActual(observer: Observer<in StatisticsSelectionResult>) {
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
        observer: Observer<in StatisticsSelectionResult>
    ) : MainThreadDisposable() {

        var valueListener: OnStatisticsDateChangedListener? =
            object : OnStatisticsDateChangedListener {
                override fun onDateChanged(date: LocalDate) {
                    if (!isDisposed)
                        observer.onNext(StatisticsSelectionResult(date))
                }

                override fun onUnselectedAll() {
                    if (!isDisposed)
                        observer.onNext(StatisticsSelectionResult(null))
                }
            }

        override fun onDispose() {
            view.setOnStatisticsDateChangedListener(null)
            valueListener = null
        }
    }
}
