package com.elta.android.presentation.widgets.indicators.listeners

import android.support.v7.widget.RecyclerView

abstract class SimpleAdapterDataObserver : RecyclerView.AdapterDataObserver() {

    abstract fun onAny()

    override fun onChanged() {
        onAny()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        onAny()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        onAny()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        onAny()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        onAny()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        onAny()
    }
}