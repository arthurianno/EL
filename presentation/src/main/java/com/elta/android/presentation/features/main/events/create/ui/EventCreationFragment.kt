package com.elta.android.presentation.features.main.events.create.ui

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.features.main.events.base.ui.BaseEventFragment
import com.elta.android.presentation.features.main.events.create.pm.EventCreationPm
import com.elta.android.presentation.utils.bundle

@Suppress("MagicNumber")
class EventCreationFragment : BaseEventFragment<EventCreationPm>() {

    override val classToken: Class<EventCreationPm> = EventCreationPm::class.java

    override fun getEventType() = checkNotNull(arguments)[EXTRA_EVENT_TYPE] as EventType

    companion object {
        fun newInstance(eventType: EventType): EventCreationFragment {
            return EventCreationFragment().apply {
                arguments = bundle(EXTRA_EVENT_TYPE to eventType)
            }
        }

        private const val EXTRA_EVENT_TYPE = "extra_event_type"
    }
}
