package com.elta.android.presentation.features.calcutator

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.viewModels
import com.elta.android.presentation.core.compose.BaseComposeFragment
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration

class CalculatorFragment(
    private val dishesConfig: ChooserConfiguration
) : BaseComposeFragment<CalculatorViewModel>() {

    override val viewModel: CalculatorViewModel by viewModels()

    @Composable
    override fun Content(viewModel: CalculatorViewModel) {
        val state = viewModel.state.collectAsState()
        Text(
            text = state.value.profile.firstName.orEmpty(),
            modifier = Modifier.fillMaxSize(),
            textAlign = TextAlign.Center
        )
    }
}
