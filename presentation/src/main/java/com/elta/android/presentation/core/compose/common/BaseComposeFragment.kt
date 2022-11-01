package com.elta.android.presentation.core.compose.common

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.navigation.RouterProvider
import com.elta.android.presentation.theme.EltaTheme
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class BaseComposeFragment<VM : ViewModel> : Fragment(R.layout.fragment_compose_view) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

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

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        (viewModel as? BaseViewModel<*, *, *>)?.run {
            if (routerIsNotSet()) {
                setRouter(((parentFragment ?: requireActivity()) as RouterProvider).router)
            }
        }
        super.onAttach(context)
    }
}
