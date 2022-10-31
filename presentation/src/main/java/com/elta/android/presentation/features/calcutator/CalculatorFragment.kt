package com.elta.android.presentation.features.calcutator

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.BaseAppTopBarWidgetModel
import com.elta.android.presentation.features.calcutator.model.DishUi
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.theme.GetLocalProperties

class CalculatorFragment(
    private val dishesConfig: ChooserConfiguration
) : BaseComposeFragment<CalculatorViewModel>() {

    override val viewModel: CalculatorViewModel by viewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.appTopBarWidgetModel.setTitle(getString(R.string.calculator_appbar_title))
    }

    @Composable
    override fun Content(viewModel: CalculatorViewModel) {
        val state = viewModel.state.collectAsState()
        val dishes = state.value.dishes
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            Scaffold(
                scaffoldState = rememberScaffoldState(),
                topBar = { CalculatorTopBar(viewModel.appTopBarWidgetModel) },
                backgroundColor = colors.gOrangeB
            ) {
                Box(
                    Modifier
                        .padding(it)
                        .fillMaxSize()
                ) {
                    MainContent(dishes = dishes)
                }
            }
        }
    }
}

@Composable
private fun CalculatorTopBar(appTopBarWidgetModel: BaseAppTopBarWidgetModel) {
    GetLocalProperties { _, _, colors, _, types ->
        BaseAppTopBar(
            widgetModel = appTopBarWidgetModel,
            backgroundColor = colors.gOrangeB,
            textStyle = types.h2,
            textColor = colors.white,
            startIcon = R.drawable.ic_back
        )
    }
}

@Composable
private
fun MainContent(dishes: List<DishUi>) {
    GetLocalProperties { dimens, brash, colors, shapes, types ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colors.white, shape = shapes.sheet)
                .padding(dimens.contentPadding),
            contentAlignment = Alignment.Center
        ) {
            if (dishes.isEmpty()) {
                EmptyContent()
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    GetLocalProperties { dimens, _, colors, _, types ->
        Text(
            text = stringResource(id = R.string.calculator_empty_list_text),
            style = types.body1.copy(color = colors.shadeBlack1),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(dimens.contentPadding)
        )
    }
}
