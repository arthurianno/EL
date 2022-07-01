package com.elta.android.presentation.features.registration.policy.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.databinding.FragmentRegistrationPrivacyPolicyBinding
import com.elta.android.presentation.features.registration.policy.pm.RegistrationPrivacyPolicyPm
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show

class RegistrationPrivacyPolicyFragment :
    BaseBottomSheetFragment<RegistrationPrivacyPolicyPm, FragmentRegistrationPrivacyPolicyBinding>(
        FragmentRegistrationPrivacyPolicyBinding::inflate
    ) {

    override val screenLayout: Int = R.layout.fragment_registration_privacy_policy
    override val classToken: Class<RegistrationPrivacyPolicyPm> =
        RegistrationPrivacyPolicyPm::class.java

    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(EXTRA_URL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.privacyContentScrollView.viewTreeObserver.addOnScrollChangedListener {
            binding.toolbar.toolbarView.z = when (binding.privacyContentScrollView.scrollY) {
                0 -> ZERO_Z_INDEX
                else -> DEFAULT_Z_INDEX
            }
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressView.show()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressView.hide()
            }
        }

        with(binding.webView.settings) {
            builtInZoomControls = false
            displayZoomControls = false
        }

        url?.let { binding.webView.loadUrl(it) }
    }

    override fun onBindPresentationModel(pm: RegistrationPrivacyPolicyPm) {
        binding.toolbar.homeButtonView.clicks().subscribe { dialog?.dismiss() }
    }

    companion object {
        private const val EXTRA_URL = "extra_url"
        fun newInstance(url: String): RegistrationPrivacyPolicyFragment =
            RegistrationPrivacyPolicyFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_URL, url)
                }
            }

        private const val ZERO_Z_INDEX = 0f
        private const val DEFAULT_Z_INDEX = 60f
    }
}
