package com.elta.android.presentation.widgets.picker

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
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
        isSaveEnabled = true
    }

    fun setValue(value: Double) {
        val left = value.toInt()
        val right = ((value - left) * TEN).toInt()
        leftPickerView.updateValue(left)
        rightPickerView.updateValue(right)
    }

    fun setValues(firstValue: Int, secondValue: Int) {
        leftPickerView.updateValue(firstValue)
        rightPickerView.updateValue(secondValue)
    }

    fun valueChanges(): Observable<Double> =
        Observables.combineLatest(
            ValueChangeObservable(leftPickerView),
            ValueChangeObservable(rightPickerView)
        ) { left: Int, right: Int ->
            config?.resultMappingFunction?.invoke(left, right) ?: 0.0
        }

    fun valueChangesFormatted(): Observable<String> =
        Observables.combineLatest(
            ValueChangeObservable(leftPickerView),
            ValueChangeObservable(rightPickerView)
        ) { left: Int, right: Int ->
            config?.formatter?.invoke(resources, left, right) ?: EMPTY_STRING
        }

    private fun initPicker() {
        config?.let { c ->
            leftPickerView.maxValue = c.firstPickerMaxValue
            leftPickerView.minValue = c.firstPickerMinValue
            rightPickerView.maxValue = c.secondPickerMaxValue
            rightPickerView.minValue = c.secondPickerMinValue
            comaView.toggleView(c.firstMeasureUnit == null)
            measurementFirstTextView.toggleView(c.firstMeasureUnit != null)
            c.firstMeasureUnit?.let { measurementFirstTextView.text = resources.getString(it) }
            measurementSecondTextView.toggleView(c.secondMeasureUnit != null)
            c.secondMeasureUnit?.let { measurementSecondTextView.text = resources.getString(it) }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        val savedState = state as? SavedState
        savedState?.let { setValues(it.leftValue, it.rightValue) }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.leftValue = leftPickerView.value
        savedState.rightValue = rightPickerView.value
        return savedState
    }

    companion object {
        private const val TEN = 10
        private const val EMPTY_STRING = ""
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
                view.removeOnValueChangedListener(valueListener)
            }
        }
    }

    private class SavedState : View.BaseSavedState {
        var leftValue: Int = 0
        var rightValue: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            leftValue = parcel.readInt()
            rightValue = parcel.readInt()
        }

        override fun writeToParcel(outParcel: Parcel, flags: Int) {
            super.writeToParcel(outParcel, flags)
            outParcel.writeInt(leftValue)
            outParcel.writeInt(rightValue)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}