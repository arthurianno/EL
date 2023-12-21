package com.elta.android.presentation.features.main.events.selector.ui

import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.selector.component.EventSelectorContentScreen
import com.elta.android.presentation.features.main.events.selector.viewmodel.EventSelectorViewModel
import com.elta.android.presentation.utils.bundle

class EventSelectorFragment: BaseComposeFragment<EventSelectorViewModel>() {

    companion object {
        fun newInstance(config: ChooserConfiguration): Fragment {
            return EventSelectorFragment().apply {
                arguments = bundle(EXTRA_SELECTOR_DATA to config)
            }
        }

        private const val EXTRA_SELECTOR_DATA = "extra_selector_data"
    }

    override val viewModel: EventSelectorViewModel by viewModels { viewModelFactory }

    override fun EventSelectorViewModel.init() {
        arguments?.getParcelable<ChooserConfiguration>(EXTRA_SELECTOR_DATA)?.let {
            viewModel.setConfiguration(it)
        }

        appTopBar.setTitle(getString(R.string.medicament_type_toolbar))
        appTopBar.setStartIconAction(AppAction.BackPressure)
        searchField.setHint(getString(R.string.medicament_type_search_hint))
        downButton.setText(getString(R.string.events_options_chooser_button_confirm))

        exitDialog.initDialog(
            title = getString(R.string.exit_dialog_title),
            message = getString(R.string.exit_dialog_message_changes),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )
    }

    @Composable
    override fun Content(viewModel: EventSelectorViewModel) {
        EventSelectorContentScreen(viewModel)
    }

    @Composable
    override fun Dialogs(viewModel: EventSelectorViewModel) {
        BaseDialog(widgetModel = viewModel.exitDialog)
    }
}