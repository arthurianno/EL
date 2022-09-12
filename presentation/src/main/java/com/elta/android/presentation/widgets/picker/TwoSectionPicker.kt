package com.elta.android.presentation.widgets.picker

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.LayoutTwoSectionPickerBinding
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

@Suppress("UnnecessaryParentheses")
class TwoSectionPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val disposable = CompositeDisposable()

    private val binding: LayoutTwoSectionPickerBinding by lazy {
        LayoutTwoSectionPickerBinding.bind(this)
    }

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

    fun setValue(value: Double) = with(binding) {
        val left = value.toInt()
        val right = ((value - left) * TEN).toInt()
        leftPickerView.pickerValue = left
        rightPickerView.pickerValue = right
    }

    fun valueChanges(): Observable<Double> =
        Observables.combineLatest(
            ValueChangeObservable(binding.leftPickerView),
            ValueChangeObservable(binding.rightPickerView)
        ) { left: Int, right: Int -> left + right.toDouble() / TEN }

    companion object {
        private const val TEN = 10
        private const val DEBOUNCE = 100L
        private const val PICKERS_COUNT = 2L
    }
}
