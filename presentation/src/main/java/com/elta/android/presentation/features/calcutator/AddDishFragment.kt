package com.elta.android.presentation.features.calcutator

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
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonBack
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonCircle
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.textfields.IconTextField
import com.elta.android.presentation.features.calcutator.model.DishState
import com.elta.android.presentation.features.calcutator.model.DishUi
import com.elta.android.presentation.features.calcutator.model.PortionUi
import com.elta.android.presentation.features.calcutator.viewmodel.AddDishViewModel
import com.elta.android.presentation.theme.GetLocalProperties
import ru.marslab.pocketwordtranslator.presentation.widget.VSpacer
import ru.marslab.pocketwordtranslator.presentation.widget.VSpacerLarge
import ru.marslab.pocketwordtranslator.presentation.widget.VSpacerMedium
import ru.marslab.pocketwordtranslator.presentation.widget.VSpacerVerySmall

private const val PORTION_INIT_TEXT = "1"

class AddDishFragment(
    private val dishId: String
) : BaseComposeFragment<AddDishViewModel>() {
    override val viewModel: AddDishViewModel by viewModels { viewModelFactory }

    override fun initView() {
        with(viewModel.downButtonWidgetModel) {
            setText(getString(R.string.calculator_add_text))
        }
        with(viewModel.portionCountTextField) {
            setIcon(R.drawable.ic_plus_minus)
            setText(PORTION_INIT_TEXT)
        }
        with(viewModel.portionDescriptionTextField) {
            setIcon(R.drawable.ic_list)
        }
        viewModel.setDish(dishId)
    }

    @Composable
    override fun Content(viewModel: AddDishViewModel) {
        val state = viewModel.state.collectAsState()
        Box(modifier = Modifier.fillMaxSize()) {
            Header(
                dish = state.value.dish,
                onBackClick = { viewModel.sendAction(AppAction.BackPressure) }
            )
            MainContent(viewModel, state)
        }
    }

    @Composable
    private fun BoxScope.Header(dish: DishUi, onBackClick: () -> Unit) {
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
    private fun BoxScope.HeaderTitle(dish: DishUi) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(dimens.dishHeaderTitle)
            ) {
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
                    text = stringResource(id = R.string.calculator_xe_count),
                    color = colors.white
                )
            }
        }
    }

    @Composable
    private fun BoxScope.BreadUnitsValue(dish: DishUi) {
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
                        id = R.string.calculator_xe_count_label,
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
    private fun BoxScope.MainContent(viewModel: AddDishViewModel, state: State<DishState>) {
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
                DownButton(widgetModel = viewModel.downButtonWidgetModel)
            }
        }
    }

    @Composable
    private fun Footer(state: State<DishState>) {
        GetLocalProperties { dimens, _, colors, _, _ ->
            val dish = state.value.dish
            val portion = state.value.portion
            Text(
                text = stringResource(id = R.string.calculator_additional_information),
                color = colors.shadeBlack2,
                modifier = Modifier.padding(start = dimens.contentPadding)
            )
            if (dish.isVerification) {
                VSpacerMedium()
                VerifyProduct()
            }
            DishChars(portion)
        }
    }

    @Composable
    private fun DishChars(portion: PortionUi) {
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
                DishChar(R.string.calculator_dish_calories, portion.calories)
                DishChar(R.string.calculator_dish_proteins, portion.proteins)
                DishChar(R.string.calculator_dish_fats, portion.fats)
                DishChar(R.string.calculator_dish_carbs, portion.carbs)
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
