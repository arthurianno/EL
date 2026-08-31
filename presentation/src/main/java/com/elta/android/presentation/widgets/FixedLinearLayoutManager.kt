package com.elta.android.presentation.widgets

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class FixedLinearLayoutManager(
    context: Context,
    orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false,
    private val isScrollEnabled: Boolean = true
) : LinearLayoutManager(context, orientation, reverseLayout) {

    override fun canScrollVertically(): Boolean =
        isScrollEnabled && super.canScrollVertically()

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
