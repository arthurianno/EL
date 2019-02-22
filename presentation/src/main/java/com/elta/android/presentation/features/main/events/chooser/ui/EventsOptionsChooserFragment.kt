package com.elta.android.presentation.features.main.events.chooser.ui

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.main.events.chooser.pm.EventsOptionsChooserPm

class EventsOptionsChooserFragment : BaseListFragment<EventsOptionsChooserPm>() {

    override val screenLayout: Int = R.layout.fragment_events_options_chooser
    override val classToken: Class<EventsOptionsChooserPm> = EventsOptionsChooserPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    companion object {
        fun newInstance(): EventsOptionsChooserFragment {
            return EventsOptionsChooserFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
