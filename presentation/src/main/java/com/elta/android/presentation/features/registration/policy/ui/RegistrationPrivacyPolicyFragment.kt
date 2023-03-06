package com.elta.android.presentation.features.registration.policy.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.databinding.FragmentRegistrationPrivacyPolicyBinding
import com.elta.android.presentation.features.registration.policy.pm.RegistrationPrivacyPolicyPm
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show

private const val ZERO_Z_INDEX = 0f
private const val DEFAULT_Z_INDEX = 60f
private const val EXTRA_URL = "extra_url"

class RegistrationPrivacyPolicyFragment :
    BaseBottomSheetFragment<RegistrationPrivacyPolicyPm, FragmentRegistrationPrivacyPolicyBinding>(
        FragmentRegistrationPrivacyPolicyBinding::inflate
    ) {
    companion object {
        fun newInstance(url: String): RegistrationPrivacyPolicyFragment =
            RegistrationPrivacyPolicyFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_URL, url)
                }
            }
    }

    override val screenLayout: Int = R.layout.fragment_registration_privacy_policy
    override val classToken: Class<RegistrationPrivacyPolicyPm> =
        RegistrationPrivacyPolicyPm::class.java

    private var url: String? = null
    private val webViewClient = object : WebViewClient() {
        private var progressView: FrameLayout? = null

        fun attachView(progress: FrameLayout) {
            progressView = progress
        }

        fun detachView() {
            progressView = null
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progressView?.show()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            progressView?.hide()
        }
    }

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
        binding.webView.webViewClient = webViewClient
        with(binding.webView.settings) {
            builtInZoomControls = false
            displayZoomControls = false
        }
        url?.let { binding.webView.loadUrl(it) }
    }

    override fun onStart() {
        super.onStart()
        webViewClient.attachView(binding.progressView)
    }

    override fun onStop() {
        webViewClient.detachView()
        super.onStop()
    }

    override fun onBindPresentationModel(pm: RegistrationPrivacyPolicyPm) {
        binding.toolbar.homeButtonView.clicks().subscribe { dialog?.dismiss() }
    }
}
