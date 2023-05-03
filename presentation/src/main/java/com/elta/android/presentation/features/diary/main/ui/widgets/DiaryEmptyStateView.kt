package com.elta.android.presentation.features.diary.main.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.stateview.StateData
import com.elta.android.presentation.core.ui.stateview.StateView
import com.jakewharton.rxbinding2.view.visibility
import io.reactivex.Observable
import io.reactivex.functions.Consumer

class DiaryEmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), StateView {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_diary_empty_state, this, true)
    }

    override fun state() = Consumer<StateData> { }

    override fun clicks(): Observable<Unit> = Observable.empty()

    override fun enable() = Consumer<Boolean> {}

    override fun visibility(): Consumer<in Boolean> = this.visibility(View.GONE)
}
