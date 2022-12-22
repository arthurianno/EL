package com.elta.android.presentation.features.consultant

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.features.consultant.viewmodel.MainChartViewModel

class MainChartFragment : BaseComposeFragment<MainChartViewModel>() {
    override val viewModel: MainChartViewModel by viewModels { viewModelFactory }

    @Composable
    override fun Content(viewModel: MainChartViewModel) {
    }
}
