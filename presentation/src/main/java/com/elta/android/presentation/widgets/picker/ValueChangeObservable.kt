package com.elta.android.presentation.widgets.picker

import com.elta.android.presentation.utils.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

class ValueChangeObservable(
    private val view: NumberPicker
) : Observable<Int>() {

    override fun subscribeActual(observer: Observer<in Int>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        listener.valueListener?.onValueChange(view, view.value, view.value)
        view.addOnValueChangedListener(listener.valueListener)
    }

    class Listener(
        private val view: NumberPicker,
        observer: Observer<in Int>
    ) : MainThreadDisposable() {

        var valueListener: NumberPicker.OnValueChangeListener? =
            NumberPicker.OnValueChangeListener { _, _, newValue ->
                if (!isDisposed) {
                    observer.onNext(newValue)
                }
            }

        override fun onDispose() {
            view.removeOnValueChangedListener(valueListener)
            valueListener = null
        }
    }
}
