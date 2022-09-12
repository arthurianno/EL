package com.elta.android.presentation.widgets.simple_date_picker

import android.graphics.drawable.Drawable
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class BackgroundDecorator(private val drawable: Drawable?) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = true

    override fun decorate(view: DayViewFacade) {
        drawable?.let { view.setSelectionDrawable(it) }
    }
}

class RangeDecorator(
    private val drawable: Drawable?,
    condition: (dates: List<CalendarDay>, day: CalendarDay) -> Boolean
) : BaseRangeDecorator(condition) {
    override fun decorate(view: DayViewFacade) {
        drawable?.let { view.setSelectionDrawable(it) }
    }
}

class RangeSpanDecorator(
    private val span: ForegroundColorSpan,
    condition: (dates: List<CalendarDay>, day: CalendarDay) -> Boolean
) : BaseRangeDecorator(condition) {
    override fun decorate(view: DayViewFacade) {
        view.addSpan(span)
    }
}

abstract class BaseRangeDecorator(
    protected val condition: (dates: List<CalendarDay>, day: CalendarDay) -> Boolean
) : DayViewDecorator {

    protected val dates = mutableListOf<CalendarDay>()

    override fun shouldDecorate(day: CalendarDay): Boolean =
        when {
            dates.isNotEmpty() && dates.contains(day) -> condition.invoke(dates, day)
            else -> false
        }

    fun updateDates(newDates: List<CalendarDay>) {
        dates.clear()
        dates.addAll(newDates)
    }
}
