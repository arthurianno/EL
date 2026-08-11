package com.elta.android.presentation.features.statistic.report.ui

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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

    private val rangeDecorators = mutableListOf<com.elta.android.presentation.widgets.simple_date_picker.BaseRangeDecorator>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.calendarView.state().edit()) {
            setMaximumDate(LocalDate.now())
            commit()
        }

        val white = ContextCompat.getColor(view.context, R.color.white)
        val blueLight = ContextCompat.getColor(view.context, R.color.shade_blue3_20)

        val inset = view.resources.getDimensionPixelSize(R.dimen.calendar_day_padding)

        val singleDaySelected = { dates: List<CalendarDay>, day: CalendarDay ->
            dates.size == 1 && day.isBoundary(dates)
        }

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

        rangeDecorators.clear()
        rangeDecorators.addAll(
            listOf(
                RangeDecorator(
                    drawable(R.drawable.bg_calendar_date_black, inset),
                    singleDaySelected
                ),
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
        )

        binding.calendarView.apply {
            setTitleMonths(resources.getStringArray(R.array.month_names))
            addDecorator(
                BackgroundDecorator(
                    drawable(R.drawable.selector_calendar_date, inset)
                )
            )
            addDecorators(rangeDecorators)
            setOnDateChangedListener { _, day, selected ->
                if (selected) {
                    val dates = listOf(day)
                    rangeDecorators.forEach {
                        it.updateDates(dates)
                    }
                    invalidateDecorators()
                    presentationModel.selectRangeAction.consumer.accept(
                        com.elta.android.domain.features.reports.model.Range(day.date, day.date)
                    )
                }
            }
            setOnRangeSelectedListener { _, dates ->
                rangeDecorators.forEach {
                    it.updateDates(dates)
                }
                invalidateDecorators()
                if (dates.isNotEmpty()) {
                    val start = dates.first().date
                    val end = dates.last().date
                    val daysBetween = org.threeten.bp.temporal.ChronoUnit.DAYS.between(start, end)
                    if (daysBetween > 365) {
                        showToast("Максимальный период отчета - 365 дней")
                        val currentRange = presentationModel.selectedRangeState.value
                        binding.calendarView.selectRange(CalendarDay.from(currentRange.start), CalendarDay.from(currentRange.end))
                    } else {
                        presentationModel.selectRangeAction.consumer.accept(
                            com.elta.android.domain.features.reports.model.Range(start, end)
                        )
                    }
                }
            }
        }
    }

    override fun onBindPresentationModel(pm: ReportPeriodChooserPm) {
        bindProgressDialog(pm)
        pm.showSnackBarCommand.bindTo { showToast(getString(R.string.statistic_error)) }
        binding.dialogCloseButtonView.clicks().subscribe { dialog?.dismiss() }
        binding.dialogActionButtonView.clicks().subscribe {
            showReportTypeDropdown(pm)
        }
        pm.closeDialogCommand.bindTo { dialog?.dismiss() }
        pm.selectedRangeState.bindTo { range ->
            val startDay = CalendarDay.from(range.start)
            val endDay = CalendarDay.from(range.end)
            val dates = mutableListOf<CalendarDay>()
            var current = range.start
            while (!current.isAfter(range.end)) {
                dates.add(CalendarDay.from(current))
                current = current.plusDays(1)
            }
            // Update decorators so single-date or range is highlighted on dialog open/bind
            binding.calendarView.post {
                rangeDecorators.forEach { it.updateDates(dates) }
                binding.calendarView.invalidateDecorators()
            }
            binding.calendarView.selectRange(startDay, endDay)
        }
        pm.titleState.bindTo(binding.dateView.text())
        pm.progressState.bindTo(setLoadingState())
    }

    private fun showReportTypeDropdown(pm: ReportPeriodChooserPm) {
        val context = requireContext()
        val popupWindow = androidx.appcompat.widget.ListPopupWindow(context)
        
        val items = listOf(
            DropdownItem(
                title = getString(R.string.report_type_pdf_title),
                subtitle = getString(R.string.report_type_pdf_subtitle),
                iconRes = R.drawable.ic_file,
                iconColor = android.graphics.Color.parseColor("#E53935")
            ),
            DropdownItem(
                title = getString(R.string.report_type_xlsx_title),
                subtitle = getString(R.string.report_type_xlsx_subtitle),
                iconRes = R.drawable.ic_list,
                iconColor = android.graphics.Color.parseColor("#2E7D32")
            )
        )
        
        val adapter = ReportTypeDropdownAdapter(context, items)
        popupWindow.setAdapter(adapter)
        popupWindow.anchorView = binding.dialogActionButtonView
        popupWindow.setWidth(binding.dialogActionButtonView.width)
        popupWindow.isModal = true
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded))
        
        popupWindow.setOnItemClickListener { _, _, position, _ ->
            val type = if (position == 0) com.elta.android.domain.features.reports.model.ReportType.PDF else com.elta.android.domain.features.reports.model.ReportType.XLSX
            pm.mainAction.consumer.accept(type)
            popupWindow.dismiss()
        }
        
        popupWindow.show()
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
