package com.elta.android.presentation.features.statistic.report.ui

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.style.ForegroundColorSpan
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.features.statistic.report.pm.ReportPeriodChooserPm
import com.elta.android.presentation.widgets.simple_date_picker.BackgroundDecorator
import com.elta.android.presentation.widgets.simple_date_picker.RangeDecorator
import com.elta.android.presentation.widgets.simple_date_picker.RangeSpanDecorator
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.android.synthetic.main.fragment_base_settings_dialog.*
import kotlinx.android.synthetic.main.layout_settings_dialog_hemoglobin.*
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters

class ReportPeriodChooserFragment : BaseBottomSheetFragment<ReportPeriodChooserPm>() {

    override val screenLayout: Int = R.layout.fragment_statistic_report_period_chooser
    override val classToken: Class<ReportPeriodChooserPm> = ReportPeriodChooserPm::class.java

    private val daySelectionRelay = PublishRelay.create<LocalDate>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(calendarView.state().edit()) {
            setMaximumDate(LocalDate.now())
            commit()
        }

        val white = ContextCompat.getColor(view.context, R.color.white)
        val blueLight = ContextCompat.getColor(view.context, R.color.shade_blue3_20)

        val inset = view.resources.getDimensionPixelSize(R.dimen.calendar_day_padding)

        val firstInRangeNotLastInRow = { dates: List<CalendarDay>, day: CalendarDay ->
            dates.size > 1 && day.isFirstIn(dates) && !day.isLastInRow()
        }

        val firstInRangeLastInRow = { dates: List<CalendarDay>, day: CalendarDay ->
            dates.size > 1 && day.isFirstIn(dates) && day.isLastInRow()
        }

        val lastInRangeNotFirstInRow = { dates: List<CalendarDay>, day: CalendarDay ->
            dates.size > 1 && day.isLastIn(dates) && !day.isFirstInRow()
        }

        val lastInRangeFirstInRow = { dates: List<CalendarDay>, day: CalendarDay ->
            dates.size > 1 && day.isLastIn(dates) && day.isFirstInRow()
        }

        val notBoundaryFirstInRow = { dates: List<CalendarDay>, day: CalendarDay ->
            dates.size > 2 && !day.isBoundary(dates) && day.isFirstInRow()
        }

        val notBoundaryLastInRow = { dates: List<CalendarDay>, day: CalendarDay ->
            dates.size > 2 && !day.isBoundary(dates) && day.isLastInRow()
        }

        val otherDays = { dates: List<CalendarDay>, day: CalendarDay ->
            dates.size > 2 && !day.isBoundary(dates) && !day.isFirstInRow() && !day.isLastInRow()
        }

        val firstOrLastInRange = { dates: List<CalendarDay>, day: CalendarDay -> day.isBoundary(dates) }

        val decorators = arrayListOf(
            RangeDecorator(drawable(R.drawable.bg_calendar_date_blue_layer, inset), firstInRangeNotLastInRow),
            RangeDecorator(drawable(R.drawable.bg_calendar_date_blue, inset), firstInRangeLastInRow),
            RangeDecorator(drawable(R.drawable.bg_calendar_date_black_layer, inset), lastInRangeNotFirstInRow),
            RangeDecorator(drawable(R.drawable.bg_calendar_date_black, inset), lastInRangeFirstInRow),
            RangeDecorator(drawable(R.drawable.bg_calendar_date_blue_alpha_left, inset), notBoundaryFirstInRow),
            RangeDecorator(drawable(R.drawable.bg_calendar_date_blue_alpha_right, inset), notBoundaryLastInRow),
            RangeDecorator(drawable(ColorDrawable(blueLight), inset), otherDays),
            RangeSpanDecorator(ForegroundColorSpan(white), firstOrLastInRange)
        )


        calendarView.addDecorator(BackgroundDecorator(drawable(R.drawable.selector_calendar_date, inset)))
        calendarView.addDecorators(decorators)

        calendarView.setOnDateChangedListener { _, day, _ ->
            daySelectionRelay.accept(day.date)
        }
        calendarView.setOnRangeSelectedListener { _, dates ->
            decorators.forEach {
                it.updateDates(dates)
            }
            calendarView.invalidateDecorators()
        }
    }

    override fun onBindPresentationModel(pm: ReportPeriodChooserPm) {
        dialogCloseButtonView.clicks().bindTo { dialog.dismiss() }
        dialogActionButtonView.clicks().bindTo(pm.mainAction)
        pm.actionButtonEnabledCommand.bindTo(dialogActionButtonView::setEnabled)
        pm.closeDialogCommand.bindTo { dialog.dismiss() }
        pm.selectedRangeState.bindTo {
            calendarView.selectRange(CalendarDay.from(it.start), CalendarDay.from(it.end))
        }
        daySelectionRelay.bindTo(pm.selectDateAction)
    }

    private inline fun Fragment.drawable(drawable: Int, inset: Int): Drawable =
        InsetDrawable(ContextCompat.getDrawable(checkNotNull(context), drawable), 0, inset, 0, inset)

    private inline fun drawable(drawable: Drawable, inset: Int): Drawable =
        InsetDrawable(drawable, 0, inset, 0, inset)

    private inline fun CalendarDay.isStartOfWeek() = date.dayOfWeek == calendarView.firstDayOfWeek
    private inline fun CalendarDay.isEndOfWeek() = date.dayOfWeek == calendarView.firstDayOfWeek.plus(6)
    private inline fun CalendarDay.isStartOfMonth() = date == date.with(TemporalAdjusters.firstDayOfMonth())
    private inline fun CalendarDay.isEndOfMonth() = date == date.with(TemporalAdjusters.lastDayOfMonth())

    private inline fun CalendarDay.isLastInRow() = isEndOfWeek() || isEndOfMonth()
    private inline fun CalendarDay.isFirstInRow() = isStartOfWeek() || isStartOfMonth()

    private inline fun CalendarDay.isFirstIn(dates: List<CalendarDay>) = dates.first() == this
    private inline fun CalendarDay.isLastIn(dates: List<CalendarDay>) = dates.last() == this

    private inline fun CalendarDay.isBoundary(dates: List<CalendarDay>) = dates.first() == this || dates.last() == this

    companion object {
        fun newInstance(): ReportPeriodChooserFragment {
            return ReportPeriodChooserFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
