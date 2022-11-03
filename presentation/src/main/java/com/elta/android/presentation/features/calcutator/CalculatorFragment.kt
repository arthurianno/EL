package com.elta.android.presentation.features.calcutator

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.DownButton
import com.elta.android.presentation.core.compose.widgets.SearchField
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.theme.GetLocalProperties
import ru.marslab.pocketwordtranslator.presentation.widget.VSpacerMedium
import ru.marslab.pocketwordtranslator.presentation.widget.VSpacerSmall

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
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            val systemBarColor = colors.gOrangeB
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = systemBarColor)
            ) {
                val state = viewModel.state.collectAsState()
                val dishes = state.value.dishes
                Scaffold(
                    scaffoldState = rememberScaffoldState(),
                    topBar = { CalculatorTopBar(viewModel.appTopBarWidgetModel) },
                    backgroundColor = colors.gOrangeB,
                    modifier = Modifier.systemBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .background(color = colors.white, shape = shapes.sheet)
                            .padding(dimens.contentPadding)
                    ) {
                        SearchField(widgetModel = viewModel.searchFieldWidgetModel)
                        VSpacerMedium()
                        HelpText(state.value.helpText)
                        VSpacerSmall()
                        if (dishes.isEmpty()) {
                            viewModel.downButtonWidgetModel.disable()
                            EmptyContent()
                        } else {
                            viewModel.downButtonWidgetModel.enable()
                        }
                    }
                }
                DownButton(widgetModel = viewModel.downButtonWidgetModel)
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
private fun HelpText(text: String) {
    GetLocalProperties { dimens, brash, colors, shapes, types ->
        Text(text = text, color = colors.shadeBlack2)
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
