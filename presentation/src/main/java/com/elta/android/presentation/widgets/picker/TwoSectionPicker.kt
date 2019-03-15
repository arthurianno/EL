package com.elta.android.presentation.widgets.picker

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.layout_two_section_picker.view.*
import java.util.concurrent.TimeUnit

@Suppress("UnnecessaryParentheses")
class TwoSectionPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val disposable = CompositeDisposable()

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_two_section_picker, this, true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        valueChanges()
            .skip(PICKERS_COUNT)
            .throttleFirst(DEBOUNCE, TimeUnit.MILLISECONDS)
            .subscribe {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            .addTo(disposable)
    }

    override fun onDetachedFromWindow() {
        disposable.clear()
        super.onDetachedFromWindow()
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
        private const val DEBOUNCE = 100L
        private const val PICKERS_COUNT = 2L
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
            view.addOnValueChangedListener(listener.valueListener)
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
                view.addOnValueChangedListener(null)
            }
        }
    }
}