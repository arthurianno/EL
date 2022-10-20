package com.elta.android.presentation.widgets.datePicker

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.LayoutHorizontalDatePickerBinding
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.elta.android.presentation.widgets.datePicker.adapter.DateAdapter
import com.elta.android.presentation.widgets.datePicker.model.DatePickerItem
import com.nullgr.core.collections.replace
import com.nullgr.core.ui.extensions.getDisplaySize
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import org.threeten.bp.LocalDate

class HorizontalDatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var date: LocalDate? = null

    private var selectedPosition: Int = 0
    private var needToPerformHapticFeedBack = false

    private val items = mutableListOf<DatePickerItem>()
    private val adapter: DateAdapter by lazy { DateAdapter() }
    private val snapHelper by lazy { LinearSnapHelper() }

    private val binding: LayoutHorizontalDatePickerBinding by lazy {
        LayoutHorizontalDatePickerBinding.bind(this)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_horizontal_date_picker, this, true)

        with(binding) {
            dateItemsView.layoutManager =
                FixedLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL)
            dateItemsView.adapter = adapter
            datePickerSelectorView.layoutParams.width = getSelectorWidth()
            dateItemsView.attachSnapHelperWithListener(
                snapHelper,
                SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL,
                object : OnSnapPositionChangeListener {
                    override fun onSnapPositionChange(position: Int) {
                        if (needToPerformHapticFeedBack) {
                            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        }
                        onPickerItemScrolled(position)
                    }
                }
            )
        }
    }

    fun date(): Consumer<LocalDate> = Consumer {
        if (date != it) {
            needToPerformHapticFeedBack = false
            date = it
            setUpDatePicker(it)
        }
    }

    fun dateChanged(): Observable<LocalDate> =
        PageChangeListener(binding.dateItemsView, snapHelper)
            .map {
                val item = adapter.currentList[it] as DatePickerItem
                item.date
            }
            .doOnNext { date = it }

    private fun setUpDatePicker(date: LocalDate) {
        if (date !in items) {
            items.replace(DatePickerDataProvider.buildDatePickerDates(date))
            adapter.submitList(items)
            adapter.notifyDataSetChanged()
        }
        postDelayed({ scrollToDate(date) }, INVALIDATE_RECYCLER_VIEW_DELAY)
        postDelayed({ needToPerformHapticFeedBack = true }, ENABLE_HAPTIC_FEEDBACK_DELAY)
    }

    private fun onPickerItemScrolled(position: Int) = with(binding) {
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

    private fun scrollToDate(date: LocalDate) = with(binding) {
        val datePosition = items.indexOfFirst { it.date == date }
        val scrollPosition = datePosition - CENTER_OFFSET
        dateItemsView.linearLayoutManager?.scrollToPositionWithOffset(scrollPosition, SCROLL_OFFSET)
        onPickerItemScrolled(datePosition)
    }

    private fun getSelectorWidth() =
        (getDisplaySize(context).first / ITEMS_ON_SCREEN_COUNT * SELECTOR_WIDTH_MULTIPLIER).toInt()

    private val RecyclerView.linearLayoutManager: LinearLayoutManager?
        get() = layoutManager as? LinearLayoutManager

    operator fun List<DatePickerItem>.contains(date: LocalDate) =
        this.any { it.date == date && it.isAvailable }

    companion object {
        const val ITEMS_ON_SCREEN_COUNT = 7
        private const val SELECTOR_WIDTH_MULTIPLIER = 0.8
        private const val SCROLL_OFFSET = 0
        private const val CENTER_OFFSET = 3
        private const val INVALIDATE_RECYCLER_VIEW_DELAY = 50L // millis
        private const val ENABLE_HAPTIC_FEEDBACK_DELAY = 150L // millis
    }
}
