package com.elta.android.presentation.features.calcutator.products

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.textfields.SearchField
import com.elta.android.presentation.features.calcutator.products.component.CalculatorTopBar
import com.elta.android.presentation.features.calcutator.products.component.MainBlock
import com.elta.android.presentation.features.calcutator.products.viewmodel.CalculatorViewModel
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.selector.ui.EventSelectorFragment
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalNetworkState
import com.elta.android.presentation.utils.bundle
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalComposeUiApi::class)
class CalculatorFragment : BaseComposeFragment<CalculatorViewModel>() {
    companion object {
        fun newInstance(calculatorFlow: CalculatorFlow): Fragment {
            return CalculatorFragment().apply {
                arguments = bundle(EXTRA_CALCULATOR_FLOW_DATA to calculatorFlow)
            }
        }
        
        private const val EXTRA_CALCULATOR_FLOW_DATA = "extra_calculator_flow_data"
    }

    override val viewModel: CalculatorViewModel by viewModels { viewModelFactory }

    override fun CalculatorViewModel.init() {

        arguments?.getParcelable<CalculatorFlow>(EXTRA_CALCULATOR_FLOW_DATA)?.let {
            viewModel.setCalculatorFlow(it)
        }

        appTopBar.setTitle(getString(R.string.calculator_appbar_title))
        appTopBar.setStartIconAction(AppAction.BackPressure)
        searchField.setHint(getString(R.string.calculator_search_hint_product))
        downButton.setText(getString(R.string.calculator_save_list))
        dishDeleteConfirmDialog.initDialog(
            message = getString(R.string.calculator_dish_delete_request),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )
        clearProductsConfirmDialog.initDialog(
            title = getString(R.string.calculator_clear_list_title),
            message = getString(R.string.calculator_clear_list_message),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )
        warningMaxBreadUnitsDialog.initDialog(
            title = getString(R.string.calculator_max_bread_units_title),
            message = getString(R.string.calculator_max_bread_units_message),
            positiveButtonText = getString(R.string.ok)
        )
        exitDialog.initDialog(
            title = getString(R.string.event_form_dialog_title),
            message = getString(R.string.event_form_exit_dialog_body),
            positiveButtonText = getString(R.string.event_form_exit_dialog_confirm_button),
            negativeButtonText = getString(R.string.event_form_dialog_cancel_button)
        )
    }

    @Composable
    override fun Dialogs(viewModel: CalculatorViewModel) {
        BaseDialog(widgetModel = viewModel.dishDeleteConfirmDialog)
        BaseDialog(widgetModel = viewModel.clearProductsConfirmDialog)
        BaseDialog(widgetModel = viewModel.warningMaxBreadUnitsDialog)
        BaseDialog(widgetModel = viewModel.exitDialog)
    }

    @Composable
    override fun Content(viewModel: CalculatorViewModel) {
        val state = viewModel.state.collectAsState().value

        val networkAvailable = LocalNetworkState.current == NetworkState.Available

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        if (!state.searchInFocus) {
            focusManager.clearFocus()
        }
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            val systemBarColor = animateColorAsState(
                targetValue = if (state.searchInFocus) colors.shadeBlack3 else colors.gOrangeA,
                label = ""
            )
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = systemBarColor.value)
                .clickableWithNoRipple {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }) {
                Scaffold(
                    scaffoldState = rememberScaffoldState(),
                    topBar = { CalculatorTopBar(viewModel.appTopBar, state.searchInFocus) },
                    bottomBar = { DownButton(widgetModel = viewModel.downButton) },
                    backgroundColor = colors.gOrangeA,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .background(
                                color = colors.white,
                                shape = if (state.searchInFocus) RectangleShape else shapes.sheet
                            )
                            .padding(
                                start = dimens.contentPadding,
                                top = dimens.contentPadding,
                                end = dimens.contentPadding
                            )
                    ) {
                        if (networkAvailable) {
                            SearchField(
                                widgetModel = viewModel.searchField,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                searchInFocus = state.searchInFocus
                            )
                        }
                        MainBlock(viewModel, focusManager)
                    }
                }
            }
        }
    }
}
