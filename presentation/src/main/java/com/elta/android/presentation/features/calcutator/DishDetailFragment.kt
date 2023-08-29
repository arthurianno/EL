package com.elta.android.presentation.features.calcutator

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.VSpacerLarge
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonCircle
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.dialogs.InfoDialog
import com.elta.android.presentation.core.compose.widgets.textfields.IconTextField
import com.elta.android.presentation.features.calcutator.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.model.DishDetailAction
import com.elta.android.presentation.features.calcutator.model.DishDetailViewState
import com.elta.android.presentation.features.calcutator.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.model.ServingUiEntity
import com.elta.android.presentation.features.calcutator.ui.MainHeader
import com.elta.android.presentation.features.calcutator.viewmodel.DishDetailViewModel
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle

private const val PORTION_INIT_TEXT = "1"
private const val EXTRA_DISH = "extra_dish"

class DishDetailFragment : BaseComposeFragment<DishDetailViewModel>() {
    companion object {
        fun newInstance(dish: DishUiEntity): DishDetailFragment =
            DishDetailFragment().apply {
                arguments = bundle(EXTRA_DISH to dish)
            }
    }

    override val viewModel: DishDetailViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getParcelable<DishUiEntity>(EXTRA_DISH)?.let { dish ->
            with(viewModel) {
                setDish(dish)
                downButton.setText(
                    getString(
                        if (dish.localId.isEmpty()) {
                            R.string.calculator_add_text
                        } else {
                            R.string.calculator_save_text
                        }
                    )
                )
            }
        }
    }

    override fun DishDetailViewModel.init() {
        portionCountTextField.setIcon(R.drawable.ic_plus_minus)
        if (portionCountTextField.state.value.text.isEmpty()) {
            portionCountTextField.setText(PORTION_INIT_TEXT)
        }
        portionDescriptionTextField.setIcon(R.drawable.ic_list)
        warningMaxBreadUnitsDialog.initDialog(
            title = getString(R.string.calculator_dialog_title_warning),
            message = getString(R.string.calculator_dish_detail_max_bread_units_message),
            positiveButtonText = getString(R.string.ok)
        )

        viewNameDialog.initDialog(
            message = getString(R.string.close),
            buttonText = getString(R.string.close)
        )
    }

    @Composable
    override fun Dialogs(viewModel: DishDetailViewModel) {
        BaseDialog(widgetModel = viewModel.warningMaxBreadUnitsDialog)
        InfoDialog(widgetModel = viewModel.viewNameDialog)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content(viewModel: DishDetailViewModel) {
        val state = viewModel.state.collectAsState().value
        val keyboardController = LocalSoftwareKeyboardController.current
        val descriptionFieldFocusRequester = FocusRequester()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickableWithNoRipple {
                    keyboardController?.hide()
                    descriptionFieldFocusRequester.requestFocus()
                    viewModel sendAction AppAction.FreeScreenTap
                }
        ) {
            MainHeader(
                dish = state.dish,
                backClickListener = { viewModel sendAction AppAction.BackPressure },
                viewNameClickListener = { viewModel sendAction DishDetailAction.ViewName }
            )
            MainContent(
                viewModel = viewModel,
                state = state,
                descriptionFieldFocusRequester = descriptionFieldFocusRequester
            )
        }
    }

    @Composable
    private fun BoxScope.MainContent(
        viewModel: DishDetailViewModel,
        state: DishDetailViewState,
        descriptionFieldFocusRequester: FocusRequester
    ) {
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.dishCardHeight)
                    .clip(shape = shapes.sheet)
                    .align(Alignment.BottomCenter)
                    .background(color = colors.white)
            ) {
                Column {
                    Header(viewModel)
                    VSpacerVerySmall()
                    IconTextField(
                        widgetModel = viewModel.portionCountTextField,
                        paddingValues = PaddingValues(horizontal = dimens.contentPadding)
                    )
                    IconTextField(
                        widgetModel = viewModel.portionDescriptionTextField,
                        paddingValues = PaddingValues(horizontal = dimens.contentPadding),
                        focusRequester = descriptionFieldFocusRequester
                    )
                    Footer(state)
                }
                DownButton(widgetModel = viewModel.downButton)
            }
        }
    }

    @Composable
    private fun Footer(state: DishDetailViewState) {
        GetLocalProperties { dimens, _, colors, _, _ ->
            Box(Modifier.fillMaxWidth()) {
                Column {
                    VSpacerLarge()
                    Text(
                        text = stringResource(id = R.string.calculator_additional_information),
                        color = colors.shadeBlack2,
                        modifier = Modifier.padding(start = dimens.contentPadding)
                    )
                    DishChars(state.dish.servingSelect)
                }
                Box(modifier = Modifier.align(Alignment.TopCenter)) {
                    VerticallyAnimation(visualState = state.isShowCountHelpSnack) {
                        Image(
                            painter = painterResource(id = R.drawable.img_portion_count_help_snack),
                            contentDescription = null,
                            modifier = Modifier.padding(dimens.portionCountHelpPadding)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DishChars(serving: ServingUiEntity) {
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            Row(
                modifier = Modifier
                    .padding(dimens.contentPadding)
                    .fillMaxWidth()
                    .clip(shape = shapes.dishCard)
                    .background(color = colors.paleGray)
                    .padding(dimens.dishChars),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DishChar(R.string.calculator_dish_calories, serving.calories)
                DishChar(R.string.calculator_dish_proteins, serving.protein)
                DishChar(R.string.calculator_dish_fats, serving.fat)
                DishChar(R.string.calculator_dish_carbs, serving.carbohydrate)
            }
        }
    }

    @Composable
    private fun DishChar(@StringRes charName: Int, charCount: Double) {
        GetLocalProperties { _, _, colors, _, types ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = charCount.toString(),
                    style = types.title3
                )
                Text(
                    text = stringResource(id = charName),
                    color = colors.shadeBlack1
                )
            }
        }
    }

    @Composable
    private fun Header(viewModel: DishDetailViewModel) {
        GetLocalProperties { dimens, _, colors, _, _ ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimens.contentPadding, top = dimens.smallDim),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.calculator_product_portion),
                    color = colors.shadeBlack2
                )
                ButtonCircle(
                    icon = R.drawable.btn_query_in_circle,
                    onClick = {
                        viewModel sendAction CalculatorAction.PortionHelpClick
                    },
                    contentDescriptionId = R.string.content_description_question_button
                )
            }
        }
    }
}
