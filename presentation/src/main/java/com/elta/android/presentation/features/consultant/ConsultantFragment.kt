package com.elta.android.presentation.features.consultant

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.features.consultant.viewmodel.ConsultantViewModel
import com.elta.android.presentation.features.consultant.widgets.ConsultantBottomAppBar
import com.elta.android.presentation.features.consultant.widgets.ConsultantTopAppBar
import com.elta.android.presentation.theme.GetLocalProperties

class ConsultantFragment : BaseComposeFragment<ConsultantViewModel>() {

    override val viewModel: ConsultantViewModel by viewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)
    }

    @Composable
    override fun Content(viewModel: ConsultantViewModel) {
        val state = viewModel.state.collectAsState()
        GetLocalProperties { dimens, brash, colors, shapes, types ->
            Column(modifier = Modifier.fillMaxSize()) {
                ConsultantTopAppBar(widgetModel = viewModel.consultantTopAppBar)
                ChatContent(viewModel)
                ConsultantBottomAppBar(widgetModel = viewModel.consultantBottomAppBar)
            }
        }
    }

    @Composable
    private fun ColumnScope.ChatContent(viewModel: ConsultantViewModel) {
        GetLocalProperties { dimens, brash, colors, shapes, types ->
            val state = viewModel.state.collectAsState()
            Box(
                modifier = Modifier.Companion
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.value.chat.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.consultant_chat_empty_text),
                        color = colors.shadeBlack2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance(): ConsultantFragment = ConsultantFragment()
    }
}
