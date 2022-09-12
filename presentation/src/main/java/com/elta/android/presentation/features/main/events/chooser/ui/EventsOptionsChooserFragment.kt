package com.elta.android.presentation.features.main.events.chooser.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentEventsOptionsChooserBinding
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.pm.EventsOptionsChooserPm
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.EventOptionsChooseAdapter
import com.elta.android.presentation.utils.applyWindowInsetsForChildrenView
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.adapter.items.ListItem
import me.dmdev.rxpm.bindTo
import javax.inject.Inject

class EventsOptionsChooserFragment :
    BaseRecyclerViewFragment<EventsOptionsChooserPm, FragmentEventsOptionsChooserBinding>(
        FragmentEventsOptionsChooserBinding::inflate
    ) {

    @Inject
    lateinit var eventOptionsChooseAdapter: EventOptionsChooseAdapter

    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { eventOptionsChooseAdapter }
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
        with(binding.toolbar) {
            toolbarView.applyWindowInsetsForChildrenView()
            toolbarTitleView.setTextColor(Color.WHITE)
            homeButtonView.setColorFilter(Color.WHITE)
        }
    }

    override fun onBindPresentationModel(pm: EventsOptionsChooserPm) {
        super.onBindPresentationModel(pm)
        pm.toolbarTitleCommand.bindTo(binding.toolbar.toolbarTitleView.text())
        pm.appBarBackgroundCommand.bindTo { binding.appBarLayoutView.setBackgroundResource(it) }
        pm.confirmButtonVisibilityCommand.bindTo(binding.confirmButtonView.visibility())
        binding.confirmButtonView.clicks().bindTo(pm.selectionConfirmedAction)
        bindProgressDialog(pm)
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
