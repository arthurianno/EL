package com.elta.android.presentation.widgets

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import timber.log.Timber

class FixedLinearLayoutManager(
    context: Context,
    orientation: Int = LinearLayoutManager.VERTICAL,
    reverseLayout: Boolean = false
) : LinearLayoutManager(context, orientation, reverseLayout) {

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            // this is ugly solution to fix bug with inconsistency detected
            Timber.i("Inconsistency detected. Invalid view holder adapter.")
        }
    }
}