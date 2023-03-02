package com.elta.android.presentation.features.statistic.report.ui

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.elta.android.common.utils.MONTH_NAMES
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.databinding.FragmentStatisticReportPeriodChooserBinding
import com.elta.android.presentation.features.statistic.report.pm.ReportPeriodChooserPm
import com.elta.android.presentation.widgets.simple_date_picker.BackgroundDecorator
import com.elta.android.presentation.widgets.simple_date_picker.RangeDecorator
import com.elta.android.presentation.widgets.simple_date_picker.RangeSpanDecorator
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.prolificinteractive.materialcalendarview.CalendarDay
import io.reactivex.functions.Consumer
import me.dmdev.rxpm.bindTo
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters

private const val DAYS_TO_ADD = 6L

class ReportPeriodChooserFragment :
    BaseBottomSheetFragment<ReportPeriodChooserPm, FragmentStatisticReportPeriodChooserBinding>(
        FragmentStatisticReportPeriodChooserBinding::inflate
    ) {
    companion object {
        fun newInstance() = ReportPeriodChooserFragment()
    }

    override val screenLayout: Int = R.layout.fragment_statistic_report_period_chooser
    override val classToken: Class<ReportPeriodChooserPm> = ReportPeriodChooserPm::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.calendarView.state().edit()) {
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

        val firstOrLastInRange =
            { dates: List<CalendarDay>, day: CalendarDay -> day.isBoundary(dates) }

        val decorators = arrayListOf(
            RangeDecorator(
                drawable(R.drawable.bg_calendar_date_blue_layer, inset),
                firstInRangeNotLastInRow
            ),
            RangeDecorator(
                drawable(R.drawable.bg_calendar_date_blue, inset),
                firstInRangeLastInRow
            ),
            RangeDecorator(
                drawable(R.drawable.bg_calendar_date_black_layer, inset),
                lastInRangeNotFirstInRow
            ),
            RangeDecorator(
                drawable(R.drawable.bg_calendar_date_black, inset),
                lastInRangeFirstInRow
            ),
            RangeDecorator(
                drawable(R.drawable.bg_calendar_date_blue_alpha_left, inset),
                notBoundaryFirstInRow
            ),
            RangeDecorator(
                drawable(R.drawable.bg_calendar_date_blue_alpha_right, inset),
                notBoundaryLastInRow
            ),
            RangeDecorator(drawable(ColorDrawable(blueLight), inset), otherDays),
            RangeSpanDecorator(ForegroundColorSpan(white), firstOrLastInRange)
        )

        binding.calendarView.apply {
            setTitleMonths(MONTH_NAMES)
            addDecorator(
                BackgroundDecorator(
                    drawable(R.drawable.selector_calendar_date, inset)
                )
            )
            addDecorators(decorators)
            setOnDateChangedListener { _, day, _ ->
                presentationModel.selectDateAction.consumer.accept(day.date)
            }
            setOnRangeSelectedListener { _, dates ->
                decorators.forEach {
                    it.updateDates(dates)
                }
                invalidateDecorators()
            }
        }
    }

    override fun onBindPresentationModel(pm: ReportPeriodChooserPm) {
        bindProgressDialog(pm)
        pm.showSnackBarCommand.bindTo { showToast(getString(R.string.statistic_error)) }
        binding.dialogCloseButtonView.clicks().subscribe { dialog?.dismiss() }
        binding.dialogActionButtonView.clicks().bindTo(pm.mainAction)
        pm.closeDialogCommand.bindTo { dialog?.dismiss() }
        pm.selectedRangeState.bindTo {
            binding.calendarView.selectRange(CalendarDay.from(it.start), CalendarDay.from(it.end))
        }
        pm.titleState.bindTo(binding.dateView.text())
        pm.progressState.bindTo(setLoadingState())
    }

    private fun setLoadingState() = Consumer<Boolean> { isLoading ->
        with(binding) {
            root.isClickable = !isLoading
            dialogActionButtonView.isVisible = !isLoading
            dialogActionLoadingIndicator.isVisible = isLoading
        }
    }

    private fun drawable(drawable: Int, inset: Int): Drawable =
        InsetDrawable(
            ContextCompat.getDrawable(requireContext(), drawable),
            0,
            inset,
            0,
            inset
        )

    private fun drawable(drawable: Drawable, inset: Int): Drawable =
        InsetDrawable(drawable, 0, inset, 0, inset)

    private fun CalendarDay.isStartOfWeek() =
        date.dayOfWeek == binding.calendarView.firstDayOfWeek

    private fun CalendarDay.isEndOfWeek() =
        date.dayOfWeek == binding.calendarView.firstDayOfWeek.plus(DAYS_TO_ADD)

    private fun CalendarDay.isStartOfMonth() =
        date == date.with(TemporalAdjusters.firstDayOfMonth())

    private fun CalendarDay.isEndOfMonth() =
        date == date.with(TemporalAdjusters.lastDayOfMonth())

    private fun CalendarDay.isLastInRow() = isEndOfWeek() || isEndOfMonth()
    private fun CalendarDay.isFirstInRow() = isStartOfWeek() || isStartOfMonth()

    private fun CalendarDay.isFirstIn(dates: List<CalendarDay>) = dates.first() == this
    private fun CalendarDay.isLastIn(dates: List<CalendarDay>) = dates.last() == this

    private fun CalendarDay.isBoundary(dates: List<CalendarDay>) =
        dates.first() == this || dates.last() == this
}
