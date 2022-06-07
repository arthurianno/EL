package com.elta.android.presentation.widgets.bottom_sheet

import android.view.View
import com.elta.android.presentation.utils.checkMainThread
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

class BottomSheetStateObservable(
    private val behavior: BottomSheetBehavior<out View>
) : Observable<BottomSheetEvent>() {

    override fun subscribeActual(observer: Observer<in BottomSheetEvent>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(behavior, observer)
        observer.onSubscribe(listener)
        behavior.addBottomSheetCallback(listener.bottomSheetCallback)
    }

    class Listener(
        private val behavior: BottomSheetBehavior<out View>,
        observer: Observer<in BottomSheetEvent>
    ) : MainThreadDisposable() {

        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(view: View, offset: Float) {
                if (!isDisposed) observer.onNext(BottomSheetEvent.Slide(view, offset))
            }

            override fun onStateChanged(view: View, state: Int) {
                if (!isDisposed) observer.onNext(BottomSheetEvent.StateChanged(view, state))
            }
        }

        override fun onDispose() {
            behavior.removeBottomSheetCallback(bottomSheetCallback)
        }
    }
}
