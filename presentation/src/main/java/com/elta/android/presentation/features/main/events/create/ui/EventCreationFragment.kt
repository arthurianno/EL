package com.elta.android.presentation.features.main.events.create.ui

import android.os.Bundle
import android.view.View
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.events.create.pm.EventCreationPm
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig
import com.nullgr.core.ui.toast.showToast
import kotlinx.android.synthetic.main.fragment_event_creation.*

@Suppress("MagicNumber")
class EventCreationFragment : BaseFragment<EventCreationPm>() {

    override val screenLayout: Int = R.layout.fragment_event_creation
    override val classToken: Class<EventCreationPm> = EventCreationPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentLightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarView.title = "Вес" // TODO for test
        toolbarView.setNavigationIcon(R.drawable.ic_dialog_close_white)
        toolbarView.setNavigationOnClickListener { activity?.onBackPressed() }

        // TODO for test
        formPickerView.config = FormMeasurementConfig(
            firstPickerMaxValue = 200,
            firstPickerMinValue = 0,
            secondPickerMaxValue = 9,
            secondPickerMinValue = 0,
            firstMeasureUnit = null,
            secondMeasureUnit = "sec"
        ) { left, right -> (left * 10.0 + right) / 10.0 }
    }

    override fun onBindPresentationModel(pm: EventCreationPm) {
        super.onBindPresentationModel(pm)
        // TODO for test
        formPickerView.valueChanges().bindTo { it.toString().showToast(activity) }
    }

    companion object {
        fun newInstance(eventType: EventType): EventCreationFragment {
            return EventCreationFragment().apply {
                arguments = bundle(EXTRA_EVENT_TYPE to eventType)
            }
        }

        private const val EXTRA_EVENT_TYPE = "extra_event_type"
    }
}
