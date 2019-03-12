package com.elta.android.presentation.features.main.events.edit.ui

import android.os.Bundle
import android.view.View
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.events.base.ui.BaseEventFragment
import com.elta.android.presentation.features.main.events.edit.pm.EditEventPm
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.menuClicks
import com.elta.android.presentation.utils.visibility
import kotlinx.android.synthetic.main.fragment_event_form.*
import java.util.concurrent.TimeUnit

class EditEventFragment : BaseEventFragment<EditEventPm>() {

    override val classToken: Class<EditEventPm> = EditEventPm::class.java

    override fun getEventType() = checkNotNull(arguments)[EXTRA_EVENT_TYPE] as EventType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presentationModel.setEventId(checkNotNull(arguments)[EXTRA_EVENT_ID] as String)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarView.inflateMenu(R.menu.menu_remove)
    }

    override fun onBindPresentationModel(pm: EditEventPm) {
        super.onBindPresentationModel(pm)
        pm.progressState.observable
            .throttleLast(DEBOUNCE, TimeUnit.MILLISECONDS)
            .bindTo(progressDialog.visibility(childFragmentManager))
        toolbarView.menuClicks(R.id.remove).bindTo(pm.deleteEventAction)
    }

    companion object {
        fun newInstance(eventId: String, eventType: EventType): EditEventFragment {
            return EditEventFragment().apply {
                arguments = bundle(
                    EXTRA_EVENT_ID to eventId,
                    EXTRA_EVENT_TYPE to eventType
                )
            }
        }

        private const val EXTRA_EVENT_TYPE = "extra_event_type"
        private const val EXTRA_EVENT_ID = "extra_event_id"
        private const val DEBOUNCE = 300L
    }
}
