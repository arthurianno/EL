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

    override fun onStart() {
        super.onStart()
        viewModel.webimSession.onResume()
    }

    override fun onResume() {
        super.onResume()
        viewModel.webimSession.onResume()
    }

    override fun onDestroy() {
        viewModel.webimSession.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        viewModel.webimSession.onPause()
        super.onPause()
    }
}
