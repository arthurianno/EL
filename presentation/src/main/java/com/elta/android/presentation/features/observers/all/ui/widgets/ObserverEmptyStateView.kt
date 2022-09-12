package com.elta.android.presentation.features.observers.all.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.state_view.StateData
import com.elta.android.presentation.core.ui.state_view.StateView
import com.jakewharton.rxbinding2.view.visibility
import io.reactivex.Observable
import io.reactivex.functions.Consumer

class ObserverEmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), StateView {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_observer_empty_state, this, true)
    }

    override fun state() = STATE_CONSUMER_STUB

    override fun clicks(): Observable<Unit> = Observable.empty()

    override fun enable() = CONSUMER_STUB

    override fun visibility(): Consumer<in Boolean> = this.visibility(View.GONE)

    private companion object {
        val CONSUMER_STUB = Consumer<Boolean> {}
        val STATE_CONSUMER_STUB = Consumer<StateData> { }
    }
}
