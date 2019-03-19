package com.elta.android.presentation.widgets.date_picker

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.date_picker.adapter.DatePickerDelegatesFactory
import com.elta.android.presentation.widgets.date_picker.adapter.items.DatePickerItem
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.adapter.RxDiffCalculator
import com.nullgr.core.date.withoutTime
import com.nullgr.core.rx.schedulers.ComputationToMainSchedulersFacade
import com.nullgr.core.ui.extensions.getDisplaySize
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_horizontal_date_picker.view.*
import java.util.Date

class HorizontalDatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var initialDate: Date? = null
        set(value) {
            field = value?.withoutTime()
            setUpDatePicker()
        }

    private val adapter: DynamicAdapter
    private val snapHelper by lazy { LinearSnapHelper() }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_horizontal_date_picker, this, true)
        val diffCalculator = RxDiffCalculator(ComputationToMainSchedulersFacade())
        val delegatesFactory = DatePickerDelegatesFactory()
        adapter = DynamicAdapter(delegatesFactory, diffCalculator)

        dateItemsView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        dateItemsView.adapter = adapter
        datePickerSelectorView.layoutParams.width = getSelectorWidth()
        dateItemsView.attachSnapHelperWithListener(
            snapHelper,
            SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL,
            object : OnSnapPositionChangeListener {
                override fun onSnapPositionChange(position: Int) {
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onPickerItemScrolled(position)
                }
            })
    }

    fun dateChanged(): Observable<Date> =
        PageChangeListener(dateItemsView, snapHelper)
            .map {
                val item = adapter.items[it] as DatePickerItem
                item.date
            }

    private fun setUpDatePicker() {
        initialDate?.let {
            adapter.updateData(DatePickerDataProvider.buildDatePickerDates(it))
            postDelayed({ scrollToDate(it) }, INVALIDATE_RECYCLER_VIEW_DELAY)
        }
    }

    private fun onPickerItemScrolled(position: Int) {
        dateItemsView.layoutManager?.let {
            it as LinearLayoutManager
            val firstVisiblePosition = it.findFirstCompletelyVisibleItemPosition()
            val lastVisiblePosition = it.findLastVisibleItemPosition()
            for (i in firstVisiblePosition..lastVisiblePosition) {
                dateItemsView.findViewHolderForAdapterPosition(i)?.itemView?.isSelected =
                    i == position
            }
        }
    }

    private fun scrollToDate(date: Date) {
        val datePosition = adapter.items.indexOfFirst {
            it is DatePickerItem && it.date == date
        }
        dateItemsView.layoutManager?.scrollToPosition(datePosition + CENTER_OFFSET)
    }

    private fun getSelectorWidth() =
        (getDisplaySize(context).first / ITEMS_ON_SCREEN_COUNT * SELECTOR_WIDTH_MULTIPLIER).toInt()

    companion object {
        const val ITEMS_ON_SCREEN_COUNT = 7
        private const val SELECTOR_WIDTH_MULTIPLIER = 0.8
        private const val CENTER_OFFSET = 3
        private const val INVALIDATE_RECYCLER_VIEW_DELAY = 100L // millis
    }
}