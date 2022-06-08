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
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.adapter.bindTo
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.pm.HemoglobinSettingsPm
import com.elta.android.presentation.utils.sequenceClicks
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.elta.android.presentation.widgets.simple_date_picker.BackgroundDecorator
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.adapter.DynamicAdapter
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

    override val contentLayout = R.layout.layout_settings_dialog_hemoglobin
    override val dialogType = DialogType.HbA1C
    override val classToken: Class<HemoglobinSettingsPm> = HemoglobinSettingsPm::class.java

    private val calendarContainerView =
        binding.dialogContentContainerView.findViewById<ExpandableLayout>(R.id.calendarContainerView)
    private val itemsView =
        binding.dialogContentContainerView.findViewById<RecyclerView>(R.id.itemsView)
    private val arrowView =
        binding.dialogContentContainerView.findViewById<AppCompatImageView>(R.id.arrowView)
    private val minusView =
        binding.dialogContentContainerView.findViewById<AppCompatImageView>(R.id.minusView)
    private val plusView =
        binding.dialogContentContainerView.findViewById<AppCompatImageView>(R.id.plusView)
    private val dateView =
        binding.dialogContentContainerView.findViewById<AppCompatTextView>(R.id.dateView)
    private val hemoglobinValueView =
        binding.dialogContentContainerView.findViewById<AppCompatTextView>(R.id.hemoglobinValueView)
    private val calendarView =
        binding.dialogContentContainerView.findViewById<MaterialCalendarView>(R.id.calendarView)
    private val hemoglobinContentView =
        binding.dialogContentContainerView.findViewById<LinearLayout>(R.id.hemoglobinContentView)

    @Inject
    lateinit var adapter: DynamicAdapter

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
        pm.hemoglobinItemsState.observable.bindTo(adapter, compositeUnbind)
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
