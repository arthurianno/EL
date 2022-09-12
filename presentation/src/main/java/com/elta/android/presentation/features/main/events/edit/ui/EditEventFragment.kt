package com.elta.android.presentation.features.main.events.edit.ui

import android.os.Bundle
import android.view.View
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.events.base.ui.BaseEventFragment
import com.elta.android.presentation.features.main.events.edit.pm.EditEventPm
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.show
import me.dmdev.rxpm.bindTo

private const val EXTRA_EVENT_TYPE = "extra_event_type"
private const val EXTRA_EVENT_ID = "extra_event_id"

class EditEventFragment : BaseEventFragment<EditEventPm>() {

    override val classToken: Class<EditEventPm> = EditEventPm::class.java

    override fun getEventType() = arguments?.get(EXTRA_EVENT_TYPE) as EventType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presentationModel.setEventId(arguments?.getString(EXTRA_EVENT_ID).orEmpty())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            menuButtonView.show()
            menuButtonView.setText(R.string.event_menu_remove)
        }
    }

    override fun onBindPresentationModel(pm: EditEventPm) {
        super.onBindPresentationModel(pm)
        binding.menuButtonView.clicks().bindTo(pm.deleteEventAction)
        bindProgressDialog(pm)
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
    }
}
