package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui

import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.common.utils.MONTH_NAMES
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.pm.HemoglobinSettingsPm
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.HemoglobinEventsAdapter
import com.elta.android.presentation.utils.sequenceClicks
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.elta.android.presentation.widgets.simple_date_picker.BackgroundDecorator
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.ui.extensions.toggleVisibilityState
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import io.reactivex.Observable
import me.dmdev.rxpm.bindTo
import net.cachapa.expandablelayout.ExpandableLayout
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class HemoglobinSettingsFragment : BaseSettingsDialogFragment<HemoglobinSettingsPm>() {

    @Inject
    lateinit var adapter: HemoglobinEventsAdapter

    override val contentLayout = R.layout.layout_settings_dialog_hemoglobin
    override val dialogType = DialogType.HbA1C
    override val classToken: Class<HemoglobinSettingsPm> = HemoglobinSettingsPm::class.java

    private val calendarContainerView by lazy {
        binding.dialogContentContainerView.findViewById<ExpandableLayout>(R.id.calendarContainerView)
    }
    private val itemsView by lazy {
        binding.dialogContentContainerView.findViewById<RecyclerView>(R.id.itemsView)
    }
    private val arrowView by lazy {
        binding.dialogContentContainerView.findViewById<AppCompatImageView>(R.id.arrowView)
    }
    private val minusView by lazy {
        binding.dialogContentContainerView.findViewById<AppCompatImageView>(R.id.minusView)
    }
    private val plusView by lazy {
        binding.dialogContentContainerView.findViewById<AppCompatImageView>(R.id.plusView)
    }
    private val dateView by lazy {
        binding.dialogContentContainerView.findViewById<AppCompatTextView>(R.id.dateView)
    }
    private val hemoglobinValueView by lazy {
        binding.dialogContentContainerView.findViewById<AppCompatTextView>(R.id.hemoglobinValueView)
    }
    private val calendarView by lazy {
        binding.dialogContentContainerView.findViewById<MaterialCalendarView>(R.id.calendarView)
    }

    private val hemoglobinContentView by lazy {
        binding.dialogContentContainerView.findViewById<LinearLayout>(R.id.hemoglobinContentView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsView.layoutManager = FixedLinearLayoutManager(requireActivity())
        itemsView.adapter = adapter

        arrowView.setOnClickListener {
            arrowView.isSelected = !calendarContainerView.isExpanded
            calendarContainerView.setExpanded(!calendarContainerView.isExpanded, true)
        }
        with(calendarView.state().edit()) {
            setMaximumDate(LocalDate.now())
            commit()
        }

        val inset = view.resources.getDimensionPixelSize(R.dimen.calendar_day_padding)
        calendarView.setTitleMonths(MONTH_NAMES)
        calendarView.addDecorator(
            BackgroundDecorator(
                drawable(
                    R.drawable.selector_calendar_date,
                    inset
                )
            )
        )
    }

    override fun onBindPresentationModel(pm: HemoglobinSettingsPm) {
        super.onBindPresentationModel(pm)
        pm.progressState.bindTo {
            binding.progressView.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE)
            hemoglobinContentView.toggleVisibilityState(!it, defaultFalseState = View.INVISIBLE)
        }
        pm.dateSelectedState.bindTo {
            calendarView.selectedDate = CalendarDay.from(it.toLocalDate())
        }
        pm.dateState.bindTo(dateView.text())
        pm.hemoglobinValueState.bindTo(hemoglobinValueView.text())
        Observable.merge(minusView.clicks(), minusView.sequenceClicks()).subscribe {
            minusView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            pm.minusAction.consumer.accept(Unit)
        }
        Observable.merge(plusView.clicks(), plusView.sequenceClicks()).subscribe {
            plusView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            pm.plusAction.consumer.accept(Unit)
        }
        calendarView.setOnDateChangedListener { _, day, selected ->
            if (selected) {
                pm.dateSelectedAction.consumer.accept(ZonedDateTime.now().with(day.date))
            }
            calendarView.invalidateDecorators()
        }
        pm.hemoglobinItemsState.bindTo(adapter::submitList)
    }

    private fun drawable(drawable: Int, inset: Int): Drawable =
        InsetDrawable(
            ContextCompat.getDrawable(requireContext(), drawable),
            0,
            inset,
            0,
            inset
        )

    companion object {
        fun newInstance(): HemoglobinSettingsFragment = HemoglobinSettingsFragment()
    }
}
