package com.elta.android.presentation.widgets.picker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.checkMainThread
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.extensions.toggleView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.layout_form_picker.view.*

@Suppress("UnnecessaryParentheses")
class FormPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var config: FormMeasurementConfig? = null
        set(value) {
            field = value
            initPicker()
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_form_picker, this, true)
    }

    fun setValue(value: Double) {
        val left = value.toInt()
        val right = ((value - left) * TEN).toInt()
        leftPickerView.value = left
        rightPickerView.value = right
    }

    fun setValues(firstValue: Int, secondValue: Int) {
        leftPickerView.value = firstValue
        rightPickerView.value = secondValue
    }

    fun valueChanges(): Observable<Double> =
        Observables.combineLatest(
            ValueChangeObservable(leftPickerView),
            ValueChangeObservable(rightPickerView)
        ) { left: Int, right: Int ->
            checkNotNull(config).resultMappingFunction.invoke(left, right)
        }

    private fun initPicker() {
        val c = checkNotNull(config)
        leftPickerView.maxValue = c.firstPickerMaxValue
        leftPickerView.minValue = c.firstPickerMinValue
        rightPickerView.maxValue = c.secondPickerMaxValue
        rightPickerView.minValue = c.secondPickerMinValue
        comaView.toggleView(c.firstMeasureUnit.isNullOrEmpty())
        measurementFirstTextView.toggleView(!c.firstMeasureUnit.isNullOrEmpty())
        c.firstMeasureUnit?.let { measurementFirstTextView.text = it }
        measurementSecondTextView.toggleView(!c.secondMeasureUnit.isNullOrEmpty())
        c.secondMeasureUnit?.let { measurementSecondTextView.text = it }
    }

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