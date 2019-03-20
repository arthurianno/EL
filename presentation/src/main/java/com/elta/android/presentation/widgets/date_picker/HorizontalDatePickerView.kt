package com.elta.android.presentation.widgets.date_picker

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.date_picker.adapter.DatePickerDelegatesFactory
import com.elta.android.presentation.widgets.date_picker.adapter.items.DatePickerItem
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.adapter.RxDiffCalculator
import com.nullgr.core.collections.replace
import com.nullgr.core.date.withoutTime
import com.nullgr.core.rx.schedulers.ComputationToMainSchedulersFacade
import com.nullgr.core.ui.extensions.getDisplaySize
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.layout_horizontal_date_picker.view.*
import java.util.Date

class HorizontalDatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var date: Date? = null
        set(value) {
            field = value?.withoutTime()
        }

    private var selectedPosition: Int = 0

    private val items = arrayListOf<DatePickerItem>()
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

    fun date(): Consumer<Date> = Consumer {
        if (date != it) {
            date = it
            setUpDatePicker()
        }
    }

    fun dateChanged(): Observable<Date> =
        PageChangeListener(dateItemsView, snapHelper)
            .map {
                val item = adapter.items[it] as DatePickerItem
                item.date
            }
            .doOnNext { date = it }

    private fun setUpDatePicker() {
        date?.let {
            if (it !in items) {
                items.replace(DatePickerDataProvider.buildDatePickerDates(it))
                adapter.updateData(items, false)
            }
            postDelayed({ scrollToDate(it) }, INVALIDATE_RECYCLER_VIEW_DELAY)
        }
    }

    private fun onPickerItemScrolled(position: Int) {
        dateItemsView.linearLayoutManager?.let {
            val firstVisiblePosition = it.findFirstCompletelyVisibleItemPosition()
            val lastVisiblePosition = it.findLastVisibleItemPosition()
            for (i in firstVisiblePosition..lastVisiblePosition) {
                dateItemsView.findViewHolderForAdapterPosition(i)?.itemView?.isSelected =
                    i == position
            }
        }
        selectedPosition = position
    }

    private fun scrollToDate(date: Date) {
        val datePosition = items.indexOfFirst { it.date == date }
        val scrollPosition = datePosition - CENTER_OFFSET
        dateItemsView.linearLayoutManager?.scrollToPositionWithOffset(scrollPosition, SCROLL_OFFSET)
        onPickerItemScrolled(datePosition)
    }

    private fun getSelectorWidth() =
        (getDisplaySize(context).first / ITEMS_ON_SCREEN_COUNT * SELECTOR_WIDTH_MULTIPLIER).toInt()

    private val RecyclerView.linearLayoutManager: LinearLayoutManager?
        get() = layoutManager as? LinearLayoutManager

    operator fun List<DatePickerItem>.contains(date: Date): Boolean {
        return this.any { it.date == date && it.isAvailable }
    }

    companion object {
        const val ITEMS_ON_SCREEN_COUNT = 7
        private const val SELECTOR_WIDTH_MULTIPLIER = 0.8
        private const val SCROLL_OFFSET = 0
        private const val CENTER_OFFSET = 3
        private const val INVALIDATE_RECYCLER_VIEW_DELAY = 50L // millis
    }
}