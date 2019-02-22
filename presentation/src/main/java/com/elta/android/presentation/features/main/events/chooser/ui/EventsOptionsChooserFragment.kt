package com.elta.android.presentation.features.main.events.chooser.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.pm.EventsOptionsChooserPm
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.widget.text
import kotlinx.android.synthetic.main.fragment_events_options_chooser.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class EventsOptionsChooserFragment : BaseListFragment<EventsOptionsChooserPm>() {

    override val screenLayout: Int = R.layout.fragment_events_options_chooser
    override val classToken: Class<EventsOptionsChooserPm> = EventsOptionsChooserPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentLightStatusBarConfigProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getParcelable<ChooserConfiguration>(EXTRA_CHOOSER_DATA)?.let {
            presentationModel.setConfiguration(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitleView.setTextColor(Color.WHITE)
        homeButtonView.setColorFilter(Color.WHITE)
    }

    override fun onBindPresentationModel(pm: EventsOptionsChooserPm) {
        super.onBindPresentationModel(pm)
        pm.toolbarTitleCommand.bindTo(toolbarTitleView.text())
        pm.appBarBackgroundCommand.bindTo { appBarLayoutView.setBackgroundResource(it) }
    }

    companion object {
        fun newInstance(config: ChooserConfiguration): EventsOptionsChooserFragment {
            return EventsOptionsChooserFragment().apply {
                arguments = bundle(EXTRA_CHOOSER_DATA to config)
            }
        }

        private const val EXTRA_CHOOSER_DATA = "extra_chooser_config"
    }
}
