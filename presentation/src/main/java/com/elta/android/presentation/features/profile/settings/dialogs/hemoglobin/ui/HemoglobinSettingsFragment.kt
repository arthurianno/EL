package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.pm.HemoglobinSettingsPm
import com.elta.android.presentation.utils.sequenceClicks
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.ui.extensions.toggleVisibilityState
import com.prolificinteractive.materialcalendarview.CalendarDay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_base_settings_dialog.*
import kotlinx.android.synthetic.main.layout_settings_dialog_hemoglobin.*
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class HemoglobinSettingsFragment : BaseSettingsDialogFragment<HemoglobinSettingsPm>() {

    override val contentLayout = R.layout.layout_settings_dialog_hemoglobin
    override val dialogType = DialogType.HbA1C
    override val classToken: Class<HemoglobinSettingsPm> = HemoglobinSettingsPm::class.java

    @Inject
    lateinit var adapter: DynamicAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsView.layoutManager = LinearLayoutManager(activity)
        itemsView.adapter = adapter

        arrowView.setOnClickListener {
            arrowView.isSelected = !calendarContainerView.isExpanded
            calendarContainerView.setExpanded(!calendarContainerView.isExpanded, true)
        }
        with(calendarView.state().edit()) {
            setMaximumDate(LocalDate.now())
            commit()
        }
    }

    override fun onBindPresentationModel(pm: HemoglobinSettingsPm) {
        super.onBindPresentationModel(pm)
        pm.progressState.bindTo {
            progressView.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE)
            hemoglobinContentView.toggleVisibilityState(!it, defaultFalseState = View.INVISIBLE)
        }
        pm.dateSelectedState.bindTo { calendarView.selectedDate = it.toCalendarDay() }
        pm.dateState.bindTo(dateView.text())
        pm.hemoglobinValueState.bindTo(hemoglobinValueView.text())
        Observable.merge(minusView.clicks(), minusView.sequenceClicks()).bindTo {
            minusView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            pm.minusAction.consumer.accept(Unit)
        }
        Observable.merge(plusView.clicks(), plusView.sequenceClicks()).bindTo {
            plusView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            pm.plusAction.consumer.accept(Unit)
        }
        calendarView.setOnDateChangedListener { _, day, selected ->
            if (selected) {
                pm.dateSelectedAction.consumer.accept(day.date.toDate())
            }
        }
        pm.hemoglobinItemsState.bindTo { items -> adapter.updateData(items) }
    }

    private fun LocalDate.toDate(): Date {
        val instant = this.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant()
        return DateTimeUtils.toDate(instant)
    }

    private fun Date.toCalendarDay(): CalendarDay {
        val c = Calendar.getInstance()
        c.time = this
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH] + 1
        val dayOfMonth = c[Calendar.DAY_OF_MONTH]
        return CalendarDay.from(year, month, dayOfMonth)
    }

    companion object {
        fun newInstance(): HemoglobinSettingsFragment = HemoglobinSettingsFragment()
    }
}
