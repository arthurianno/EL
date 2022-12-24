package com.elta.android.presentation.features.consultant

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.fragment.app.viewModels
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.features.consultant.viewmodel.ConsultantViewModel
import com.elta.android.presentation.features.consultant.widgets.ConsultantBottomAppBar
import com.elta.android.presentation.features.consultant.widgets.ConsultantTopAppBar
import com.elta.android.presentation.theme.GetLocalProperties

class ConsultantFragment : BaseComposeFragment<ConsultantViewModel>() {

    override val viewModel: ConsultantViewModel by viewModels { viewModelFactory }

    @Composable
    override fun Content(viewModel: ConsultantViewModel) {
        val state = viewModel.state.collectAsState()
        GetLocalProperties { dimens, brash, colors, shapes, types ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                ConsultantTopAppBar(widgetModel = viewModel.consultantTopAppBar)
                ConsultantBottomAppBar(widgetModel = viewModel.consultantBottomAppBar)
            }
        }
    }

    companion object {
        fun newInstance(): ConsultantFragment = ConsultantFragment()
    }
}
