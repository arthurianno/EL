package com.elta.android.presentation.core.compose

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.elta.android.presentation.R
import com.elta.android.presentation.theme.EltaTheme

abstract class BaseComposeFragment<VM : ViewModel> : Fragment(R.layout.fragment_compose_view) {

    abstract val viewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ComposeView>(R.id.main_view).setContent {
            EltaTheme {
                Content(viewModel = viewModel)
            }
        }
    }

    @Composable
    abstract fun Content(viewModel: VM)
}
