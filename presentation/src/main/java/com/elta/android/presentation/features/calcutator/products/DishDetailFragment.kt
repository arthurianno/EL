package com.elta.android.presentation.features.calcutator.products

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.common.ErrorScreen
import com.elta.android.presentation.core.compose.widgets.common.LoadingScreen
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.dialogs.InfoDialog
import com.elta.android.presentation.features.calcutator.component.PortionProductContent
import com.elta.android.presentation.features.calcutator.component.PortionProductFooter
import com.elta.android.presentation.features.calcutator.component.PortionProductHeader
import com.elta.android.presentation.features.calcutator.products.component.MainHeader
import com.elta.android.presentation.features.calcutator.products.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.products.model.DishDetailAction
import com.elta.android.presentation.features.calcutator.products.model.DishDetailViewState
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.viewmodel.DishDetailViewModel
import com.elta.android.presentation.theme.GetLocalProperties

class DishDetailFragment : BaseComposeFragment<DishDetailViewModel>() {
    companion object {
        fun newInstance(dish: DishUiEntity, calculatorFlow: CalculatorFlow): DishDetailFragment =
            DishDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_DISH, dish)
                    putParcelable(EXTRA_CALCULATOR_FLOW_DATA, calculatorFlow)
                }
            }

        private const val EXTRA_CALCULATOR_FLOW_DATA = "extra_calculator_flow_data"
        private const val EXTRA_DISH = "extra_dish"
    }

    override val viewModel: DishDetailViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getParcelable<DishUiEntity>(EXTRA_DISH)?.let { dish ->
            with(viewModel) {
                setDish(dish)
                downButton.setText(
                    getString(
                        if (dish.localId.isEmpty() || dish.servingSelect.id.isBlank()) {
                            R.string.add_text
                        } else {
                            R.string.calculator_save_text
                        }
                    )
                )
            }
        }
        arguments?.getParcelable<CalculatorFlow>(EXTRA_CALCULATOR_FLOW_DATA)
            ?.let { calculatorFlow ->
                viewModel.setCalculatorFlow(calculatorFlow)
            }
    }

    override fun DishDetailViewModel.init() {
        viewModel.appTopBar.setStartIconAction(AppAction.BackPressure)
        if (portionCountTextField.state.value.textField.text.isEmpty()) {
            portionCountTextField.setText(PORTION_INIT_TEXT)
        }
        warningMaxBreadUnitsDialog.initDialog(
            title = getString(R.string.calculator_max_bread_units_title),
            message = getString(R.string.calculator_dish_detail_max_bread_units_message),
            positiveButtonText = getString(R.string.ok)
        )

        viewNameDialog.initDialog(
            message = getString(R.string.close),
            buttonText = getString(R.string.close)
        )

        warningExitDialog.initDialog(
            title = getString(R.string.exit_dialog_title),
            message = getString(R.string.exit_dialog_message),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )
    }

    @Composable
    override fun Dialogs(viewModel: DishDetailViewModel) {
        BaseDialog(widgetModel = viewModel.warningMaxBreadUnitsDialog)
        BaseDialog(widgetModel = viewModel.warningExitDialog)
        InfoDialog(widgetModel = viewModel.viewNameDialog)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content(viewModel: DishDetailViewModel) {
        val state = viewModel.state.collectAsState().value
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        GetLocalProperties { dimens, brash, colors, shapes, _ ->
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colors.gOrangeA)
                    .statusBarsPadding(),
                topBar = { TopBar(viewModel.appTopBar) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .padding(paddingValues)
                        .focusRequester(focusRequester)
                        .clickableWithNoRipple {
                            keyboardController?.hide()
                            focusRequester.requestFocus()
                            focusManager.clearFocus()
                            viewModel sendAction AppAction.FreeScreenTap
                        }
                ) {
                    when {
                        state.isLoading -> LoadingScreen(color = colors.shadeBlack1)
                        state.isError -> ErrorScreen(
                            titleTextId = R.string.create_custom_product_server_error
                        ) {
                            viewModel.sendAction(DishDetailAction.Retry)
                        }
                        else -> {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                MainHeader(
                                    dish = state.dish,
                                    calculatorFlow = state.calculatorFlow,
                                    viewNameClickListener = { viewModel sendAction DishDetailAction.ViewName }
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(shape = shapes.sheet)
                                        .background(color = colors.white)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(dimens.contentPadding)
                                    ) {
                                        PortionProductHeader { viewModel sendAction CalculatorAction.PortionHelpClick }
                                        VSpacerVerySmall()
                                        PortionProductContent(
                                            portionWidgetModel = viewModel.portionCountTextField,
                                            portionDescriptionWidgetModel = viewModel.portionDescriptionDropdownField,
                                            focusManager = focusManager,
                                            focusRequester = focusRequester,
                                            isError = false,
                                            isFocusRequested = true,
                                        )
                                        PortionProductFooter(state.dish, state.isShowCountHelpSnack)
                                    }
                                }
                                val isEnable = viewModel.downButton.state.collectAsState().value.enable
                                val buttonBackground: Brush = if (isEnable) {
                                    brash.downButton
                                } else {
                                    SolidColor(colors.shadeBlack3)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(brush = buttonBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DownButton(widgetModel = viewModel.downButton)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TopBar(appTopBar: BaseAppTopBarWidgetModel) {
        GetLocalProperties { _, _, colors, _, _ ->
            BaseAppTopBar(
                widgetModel = appTopBar,
                backgroundColor = colors.gOrangeA,
                startIcon = R.drawable.ic_back,
                startIconColor = colors.paleGray
            )
        }
    }

    @Preview
    @Composable
    private fun PreviewDishDetail() {
        Content(viewModel = viewModel())
    }
}

private const val PORTION_INIT_TEXT = "1"
