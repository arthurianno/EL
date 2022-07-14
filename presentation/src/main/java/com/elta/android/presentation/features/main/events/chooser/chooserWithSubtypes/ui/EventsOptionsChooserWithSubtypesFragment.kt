package com.elta.android.presentation.features.main.events.chooser.chooserWithSubtypes.ui

import android.os.Bundle
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentEventsOptionsChooserBinding
import com.elta.android.presentation.features.main.events.chooser.adapter.EventOptionsChooseAdapter
import com.elta.android.presentation.features.main.events.chooser.chooserWithSubtypes.pm.EventsOptionsChooserWithSubtypesPm
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.adapter.items.ListItem
import me.dmdev.rxpm.bindTo
import javax.inject.Inject

class EventsOptionsChooserWithSubtypesFragment :
    BaseRecyclerViewFragment<EventsOptionsChooserWithSubtypesPm, FragmentEventsOptionsChooserBinding>(
        FragmentEventsOptionsChooserBinding::inflate
    ) {

    @Inject
    lateinit var eventOptionsChooseAdapter: EventOptionsChooseAdapter

    override val screenLayout: Int = R.layout.fragment_events_options_chooser
    override val classToken: Class<EventsOptionsChooserWithSubtypesPm> =
        EventsOptionsChooserWithSubtypesPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentLightStatusBarConfigProvider
    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy {
        eventOptionsChooseAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getParcelable<ChooserConfiguration>(EXTRA_CHOOSER_DATA)?.let {
            presentationModel.setConfiguration(it)
        }
    }

    override fun onBindPresentationModel(pm: EventsOptionsChooserWithSubtypesPm) {
        super.onBindPresentationModel(pm)
        pm.toolbarTitleCommand.bindTo(binding.toolbar.toolbarTitleView.text())
        pm.appBarBackgroundCommand.bindTo { binding.appBarLayoutView.setBackgroundResource(it) }
        bindProgressDialog(pm)
    }

    companion object {
        fun newInstance(config: ChooserConfiguration): EventsOptionsChooserWithSubtypesFragment {
            return EventsOptionsChooserWithSubtypesFragment().apply {
                arguments = bundle(EXTRA_CHOOSER_DATA to config)
            }
        }

        private const val EXTRA_CHOOSER_DATA = "extra_chooser_config"
    }
}