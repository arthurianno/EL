package com.elta.android.presentation.widgets.picker

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.layout_two_section_picker.view.*

@Suppress("UnnecessaryParentheses")
class TwoSectionPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_two_section_picker, this, true)
    }

    fun setValue(value: Double) {
        val left = value.toInt()
        val right = ((value - left) * TEN).toInt()
        leftPickerView.value = left
        rightPickerView.value = right
    }

    fun valueChanges(): Observable<Double> =
        Observables.combineLatest(
            ValueChangeObservable(leftPickerView),
            ValueChangeObservable(rightPickerView)
        ) { left: Int, right: Int -> left + right.toDouble() / TEN }

    companion object {
        private const val TEN = 10
    }

    private class ValueChangeObservable(
        private val view: NumberPicker
    ) : Observable<Int>() {

        override fun subscribeActual(observer: Observer<in Int>) {
            if (!checkMainThread(observer)) {
                return
            }
            val listener = Listener(view, observer)
            observer.onSubscribe(listener)
            listener.valueListener.onValueChange(view, view.value, view.value)
            view.setOnValueChangedListener(listener.valueListener)
        }

        class Listener(
            private val view: NumberPicker,
            observer: Observer<in Int>
        ) : MainThreadDisposable() {

            val valueListener = NumberPicker.OnValueChangeListener { _, _, newValue ->
                if (!isDisposed) {
                    observer.onNext(newValue)
                }
            }

            override fun onDispose() {
                view.setOnValueChangedListener(null)
            }
        }
    }
}