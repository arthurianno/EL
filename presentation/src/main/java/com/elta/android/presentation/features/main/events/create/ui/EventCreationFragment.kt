package com.elta.android.presentation.features.main.events.create.ui

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.events.create.pm.EventCreationPm
import com.elta.android.presentation.utils.bundle

class EventCreationFragment : BaseFragment<EventCreationPm>() {

    override val screenLayout: Int = R.layout.fragment_event_creation
    override val classToken: Class<EventCreationPm> = EventCreationPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentLightStatusBarConfigProvider

    companion object {
        fun newInstance(eventType: EventType): EventCreationFragment {
            return EventCreationFragment().apply {
                arguments = bundle(EXTRA_EVENT_TYPE to eventType)
            }
        }

        private const val EXTRA_EVENT_TYPE = "extra_event_type"
    }
}
