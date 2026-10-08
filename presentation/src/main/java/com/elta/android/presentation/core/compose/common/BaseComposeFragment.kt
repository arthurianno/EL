package com.elta.android.presentation.core.compose.common

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.navigation.RouterProvider
import com.elta.android.presentation.theme.EltaTheme
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class BaseComposeFragment<VM : BaseViewModel<*>> :
    Fragment(R.layout.fragment_compose_view) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    abstract val viewModel: VM

    protected open val padContentForNavigationBar: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init()
        arguments?.let { viewModel.handleFragmentArguments(it) }
        viewModel.reloadScreenConfigIfLanguageChanged(requireContext())
        addOnBackPressedCallback()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (viewModel as? LifecycleEventObserver)?.let { viewLifecycleOwner.lifecycle.addObserver(it) }
        view.findViewById<ComposeView>(R.id.main_view)
            .apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
            }
            .setContent {
                EltaTheme {
                    Dialogs(viewModel = viewModel)
                    Box(
                        Modifier
                            .fillMaxSize()
                            .then(if (padContentForNavigationBar) Modifier.navigationBarsPadding() else Modifier)
                    ) {
                        Content(viewModel = viewModel)
                    }
                }
            }
    }

    protected open fun VM.init() {}

    @Composable
    open fun Dialogs(viewModel: VM) {
    }

    @Composable
    abstract fun Content(viewModel: VM)

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        // The ViewModel can survive recreation, while its host router may change.
        viewModel.setRouter(((parentFragment ?: requireActivity()) as RouterProvider).router)
        super.onAttach(context)
    }

    private fun addOnBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.sendAction(AppAction.BackPressure)
            }
        })
    }
}
