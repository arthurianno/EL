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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerLarge
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonBack
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonCircle
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.textfields.IconTextField
import com.elta.android.presentation.features.calcutator.model.DishDetailViewState
import com.elta.android.presentation.features.calcutator.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.model.ServingUiEntity
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
            message = getString(R.string.calculator_max_bread_units_message),
            positiveButtonText = getString(R.string.ok)
        )
    }

    @Composable
    override fun Dialogs() {
        BaseDialog(widgetModel = viewModel.warningMaxBreadUnitsDialog)
    }

    @Composable
    override fun Content(viewModel: DishDetailViewModel) {
        val state = viewModel.state.collectAsState()
        viewModel.downButton.setEnableState(state.value.dish.breadUnits > 0.0)
        Box(modifier = Modifier.fillMaxSize()) {
            Header(
                dish = state.value.dish,
                onBackClick = { viewModel.sendAction(AppAction.BackPressure) }
            )
            MainContent(viewModel, state)
        }
    }

    @Composable
    private fun BoxScope.Header(dish: DishUiEntity, onBackClick: () -> Unit) {
        GetLocalProperties { dimens, brash, colors, _, _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = dimens.dishCardHeight - dimens.headerBottomDim)
                    .align(Alignment.TopCenter)
                    .background(brush = brash.dishHeader)
            ) {
                ButtonBack(
                    color = colors.paleGray,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(dimens.contentPadding)
                        .align(Alignment.TopStart),
                    onClick = onBackClick
                )
                HeaderTitle(dish)
                BreadUnitsValue(dish)
            }
        }
    }

    @Composable
    private fun BoxScope.HeaderTitle(dish: DishUiEntity) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(dimens.dishHeaderTitle)
            ) {
                Text(text = dish.brandName, style = types.body1, color = colors.white)
                Row {
                    if (dish.isVerification) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_verify_dish),
                            contentDescription = null,
                            modifier = Modifier.padding(top = dimens.smallDim)
                        )
                    }
                    Text(
                        text = dish.name,
                        style = types.h1,
                        color = colors.white,
                        modifier = Modifier.padding(start = dimens.verySmallDim)
                    )
                }
                VSpacer(height = dimens.halfMediumDim)
                Text(
                    text = stringResource(id = R.string.calculator_bread_units_count),
                    color = colors.white
                )
            }
        }
    }

    @Composable
    private fun BoxScope.BreadUnitsValue(dish: DishUiEntity) {
        GetLocalProperties { dimens, _, colors, shapes, types ->
            Box(
                modifier = Modifier
                    .padding(dimens.xeValueCard)
                    .clip(shapes.dishCard)
                    .background(color = colors.white)
                    .align(Alignment.BottomEnd)
                    .padding(dimens.xeValue)
            ) {
                Text(
                    text = stringResource(
                        id = R.string.calculator_bread_units_count_label,
                        dish.breadUnits.toString()
                    ),
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.gOrangeB,
                    style = types.title3
                )
            }
        }
    }

    @Composable
    private fun BoxScope.MainContent(
        viewModel: DishDetailViewModel,
        state: State<DishDetailViewState>
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
                    Header()
                    VSpacerVerySmall()
                    IconTextField(
                        widgetModel = viewModel.portionCountTextField,
                        paddingValues = PaddingValues(horizontal = dimens.contentPadding)
                    )
                    IconTextField(
                        widgetModel = viewModel.portionDescriptionTextField,
                        paddingValues = PaddingValues(horizontal = dimens.contentPadding)
                    )
                    VSpacerLarge()
                    Footer(state)
                }
                DownButton(widgetModel = viewModel.downButton)
            }
        }
    }

    @Composable
    private fun Footer(state: State<DishDetailViewState>) {
        GetLocalProperties { dimens, _, colors, _, _ ->
            val dish = state.value.dish
            val serving = state.value.dish.servingSelect
            Text(
                text = stringResource(id = R.string.calculator_additional_information),
                color = colors.shadeBlack2,
                modifier = Modifier.padding(start = dimens.contentPadding)
            )
            if (dish.isVerification) {
                VSpacerMedium()
                VerifyProduct()
            }
            DishChars(serving)
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
                DishChar(R.string.calculator_dish_proteins, serving.proteins)
                DishChar(R.string.calculator_dish_fats, serving.fats)
                DishChar(R.string.calculator_dish_carbs, serving.carbs)
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
    private fun VerifyProduct() {
        GetLocalProperties { dimens, _, colors, _, _ ->
            Row(modifier = Modifier.padding(horizontal = dimens.contentPadding)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_verify_dish),
                    contentDescription = null,
                    modifier = Modifier.padding(end = dimens.halfMediumDim)
                )
                Text(
                    text = stringResource(id = R.string.calculator_verify_product),
                    color = colors.blackBlue
                )
            }
        }
    }

    @Composable
    private fun Header() {
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
                    onClick = { /*TODO обработка клика по знаку Вопрос*/ }
                )
            }
        }
    }
}
