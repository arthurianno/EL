package com.elta.android.presentation.features.calcutator

import android.os.Bundle
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.ButtonPlus
import com.elta.android.presentation.core.compose.widgets.DownButton
import com.elta.android.presentation.core.compose.widgets.SearchField
import com.elta.android.presentation.core.compose.widgets.VerticallyAnimation
import com.elta.android.presentation.features.calcutator.model.CalculatorState
import com.elta.android.presentation.features.calcutator.model.DishUi
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.theme.GetLocalProperties
import ru.marslab.pocketwordtranslator.presentation.widget.VSpacerMedium
import ru.marslab.pocketwordtranslator.presentation.widget.VSpacerSmall
import ru.marslab.pocketwordtranslator.presentation.widget.VSpacerVerySmall

class CalculatorFragment(
    private val dishesConfig: ChooserConfiguration
) : BaseComposeFragment<CalculatorViewModel>() {

    override val viewModel: CalculatorViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        with(viewModel.appTopBarWidgetModel) {
            setTitle(getString(R.string.calculator_appbar_title))
            setStartIconAction(AppAction.BackPressure)
        }
        with(viewModel.searchFieldWidgetModel) {
            setHint(getString(R.string.calculator_search_hint))
        }
        with(viewModel.downButtonWidgetModel) {
            setText(getString(R.string.calculator_save_text))
        }
        viewModel.setHelpText(getString(R.string.calculator_help_text_add_dishes))
    }

    @Composable
    override fun Content(viewModel: CalculatorViewModel) {
        val state = viewModel.state.collectAsState()
        val dishes = state.value.dishes
        val searchInFocus = viewModel.state.collectAsState().value.searchInFocus
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            val systemBarColor = animateColorAsState(
                targetValue = if (searchInFocus) colors.shadeBlack3 else colors.gOrangeB
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = systemBarColor.value)
            ) {
                Scaffold(
                    scaffoldState = rememberScaffoldState(),
                    topBar = { CalculatorTopBar(viewModel.appTopBarWidgetModel, searchInFocus) },
                    backgroundColor = colors.gOrangeB,
                    modifier = Modifier.systemBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .background(
                                color = colors.white,
                                shape = if (searchInFocus) RectangleShape else shapes.sheet
                            )
                            .padding(dimens.contentPadding)
                    ) {
                        SearchField(viewModel.searchFieldWidgetModel, searchInFocus)
                        VSpacerMedium()
                        HelpText(viewModel, searchInFocus)
                        VSpacerSmall()
                        if (searchInFocus) {
                            showSearchView(viewModel, state)
                        } else {
                            showMainContent(viewModel, dishes)
                        }
                    }
                }
                DownButton(widgetModel = viewModel.downButtonWidgetModel)
            }
        }
    }

    @Composable
    private fun showMainContent(
        viewModel: CalculatorViewModel,
        dishes: List<DishUi>
    ) {
        if (dishes.isEmpty()) {
            viewModel.downButtonWidgetModel.disable()
            EmptyContent()
        } else {
            viewModel.downButtonWidgetModel.enable()
            CalculateDishes(dishes)
        }
    }

    @Composable
    private fun showSearchView(
        viewModel: CalculatorViewModel,
        state: State<CalculatorState>
    ) {
        val searchFieldState = viewModel.searchFieldWidgetModel.state.collectAsState()
        if (searchFieldState.value.text.isEmpty()) {
            LastWords(
                lastWords = state.value.lastWords,
                onClick = viewModel::lastWordOnClick
            )
        } else {
            FindingDishes(
                findingDishes = state.value.findingDishes,
                onClick = viewModel::dishOnClick
            )
        }
    }

    @Composable
    private fun FindingDishes(findingDishes: List<DishUi>, onClick: (dish: DishUi) -> Unit) {
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.dishCardVerticalSpace)) {
                items(items = findingDishes) { dish ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shapes.dishCard)
                            .border(dimens.borderWidth, colors.shadeBlack3, shapes.dishCard)
                            .padding(dimens.contentPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardBody(dish)
                        ButtonPlus(onClick = { onClick(dish) })
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.CardBody(dish: DishUi) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .padding(end = dimens.contentPadding)
                    .weight(1f)
            ) {
                Box {
                    Text(
                        text = dish.name,
                        style = types.title3,
                        color = colors.blackBlue,
                        modifier = Modifier.padding(end = dimens.bigDim)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_verify_dish),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
                VSpacerVerySmall()
                Text(
                    text = "${dish.rationCount} ${dish.ration}",
                    color = colors.shadeBlack1
                )
            }
        }
    }

    @Composable
    private fun LastWords(lastWords: List<String>, onClick: (word: String) -> Unit) {
        GetLocalProperties { dimens, _, _, _, _ ->
            LazyColumn() {
                items(items = lastWords) { word ->
                    Text(
                        text = word,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClick(word) }
                            .padding(vertical = dimens.lastWordVertical)
                    )
                }
            }
        }
    }

    @Composable
    private fun CalculateDishes(dishes: List<DishUi>) {
    }

    @Composable
    private fun CalculatorTopBar(
        appTopBarWidgetModel: BaseAppTopBarWidgetModel,
        searchInFocus: Boolean
    ) {
        GetLocalProperties { _, _, colors, _, types ->
            VerticallyAnimation(visualState = !searchInFocus, toUp = false) {
                BaseAppTopBar(
                    widgetModel = appTopBarWidgetModel,
                    backgroundColor = colors.gOrangeB,
                    textStyle = types.h2,
                    textColor = colors.white,
                    startIcon = R.drawable.ic_back
                )
            }
        }
    }

    @Composable
    private fun HelpText(
        viewModel: CalculatorViewModel,
        searchInFocus: Boolean
    ) {
        GetLocalProperties { _, _, colors, _, _ ->
            val state = viewModel.state.collectAsState()
            val searchFieldState = viewModel.searchFieldWidgetModel.state.collectAsState()
            Text(
                text = if (!searchInFocus) {
                    state.value.helpText
                } else {
                    stringResource(
                        id = if (searchFieldState.value.text.isEmpty()) {
                            R.string.calculator_last_search
                        } else {
                            R.string.calculator_search_result
                        }
                    )
                },
                color = colors.shadeBlack2
            )
        }
    }

    @Composable
    private fun EmptyContent() {
        GetLocalProperties { dimens, _, colors, _, types ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = stringResource(id = R.string.calculator_empty_list_text),
                    style = types.body1.copy(color = colors.shadeBlack2),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(dimens.contentPadding)
                )
            }
        }
    }
}
