package com.elta.android.presentation.features.registration.confirmation.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elta.android.presentation.R
import com.elta.android.presentation.States
import com.elta.android.presentation.core.navigation.RouterProvider
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentEmailConfirmationBinding
import com.elta.android.presentation.features.registration.confirmation.model.ConfirmationStatus
import com.elta.android.presentation.features.registration.confirmation.model.EmailConfirmationState
import com.elta.android.presentation.features.registration.confirmation.viewmodel.EmailConfirmationViewModel
import com.elta.android.presentation.utils.PROGRESS_TAG
import com.elta.android.presentation.utils.applyInsetsToContentView
import com.elta.android.presentation.utils.applySystemBarsInsetsPadding
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.visibility
import com.elta.android.presentation.widgets.dialogs.ProgressDialog
import com.nullgr.core.ui.extensions.setStatusBarColor
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EmailConfirmationFragment : Fragment(R.layout.fragment_email_confirmation) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: EmailConfirmationViewModel by viewModels { viewModelFactory }
    private var _binding: FragmentEmailConfirmationBinding? = null
    private val binding get() = checkNotNull(_binding)
    private val viewSubscriptions = CompositeDisposable()
    private var progressJob: Job? = null
    private val progressDialog by lazy { ProgressDialog.newInstance() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        viewModel.setRouter(((parentFragment ?: requireActivity()) as RouterProvider).router)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.backClick()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmailConfirmationBinding.bind(view)
        if (parentFragment !is BaseFlowFragment<*, *>) {
            view.applySystemBarsInsetsPadding(applyStatusBarInset = false)
        }
        binding.toolbar.menuButtonView.text =
            getString(R.string.registration_email_confirmation_login_button)
        binding.toolbar.menuButtonView.setOnClickListener { viewModel.loginWithAnotherAccount() }
        viewSubscriptions.add(
            binding.confirmationSuccessStateView.clicks()
                .subscribe { viewModel.continueToPreviousScreen() }
        )
        viewSubscriptions.add(
            binding.errorStateView.clicks().subscribe { viewModel.retry() }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::render)
            }
        }

        val token = arguments?.getString(EXTRA_TOKEN)
        if (token == null) viewModel.backClick() else viewModel.initialize(token)
    }

    override fun onStart() {
        super.onStart()
        with(LightStatusBarConfigProvider) {
            view?.applyInsetsToContentView(!drawUnderStatusBar)
            requireActivity().window.setStatusBarColor(statusBarColor, lightStatusBar)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setBackgroundDrawableResource(R.color.color_window_background)
    }

    override fun onPause() {
        view?.hideKeyboardFun()
        super.onPause()
    }

    override fun onDestroyView() {
        progressJob?.cancel()
        (childFragmentManager.findFragmentByTag(PROGRESS_TAG) as? ProgressDialog)
            ?.dismissAllowingStateLoss()
        viewSubscriptions.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: EmailConfirmationState) {
        progressJob?.cancel()
        when (val status = state.status) {
            ConfirmationStatus.Loading -> {
                binding.confirmationNextActionView.visibility = View.GONE
                binding.errorStateView.visibility = View.GONE
                progressJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    if (_binding != null && viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        progressDialog.visibility(childFragmentManager).accept(true)
                    }
                }
            }
            ConfirmationStatus.OtherAccount -> {
                progressDialog.visibility(childFragmentManager).accept(false)
                binding.confirmationNextActionView.visibility = View.VISIBLE
                binding.errorStateView.visibility = View.GONE
            }
            is ConfirmationStatus.Failure -> {
                progressDialog.visibility(childFragmentManager).accept(false)
                binding.confirmationNextActionView.visibility = View.GONE
                binding.errorStateView.state().accept(
                    if (status.isServerUnavailable) {
                        States.SimpleError(
                            icon = R.drawable.ic_server_error,
                            title = getString(R.string.error_server_not_responding_title),
                            description = getString(R.string.error_server_not_responding_subtitle),
                            button = getString(R.string.error_server_not_responding_retry_button)
                        )
                    } else {
                        States.SimpleError(
                            icon = R.drawable.ic_server_error,
                            title = getString(R.string.error_general_title),
                            description = status.message,
                            button = getString(R.string.error_general_button)
                        )
                    }
                )
                binding.errorStateView.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val EXTRA_TOKEN = "extra_token"

        fun newInstance(token: String): EmailConfirmationFragment =
            EmailConfirmationFragment().apply { arguments = bundle(EXTRA_TOKEN to token) }
    }
}
