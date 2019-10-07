package com.elta.android.presentation.features.registration.policy.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.features.registration.policy.pm.RegistrationPrivacyPolicyPm
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_registration_privacy_policy.*
import kotlinx.android.synthetic.main.layout_bottom_sheet_toolbar.*

class RegistrationPrivacyPolicyFragment : BaseBottomSheetFragment<RegistrationPrivacyPolicyPm>() {

    override val screenLayout: Int = R.layout.fragment_registration_privacy_policy
    override val classToken: Class<RegistrationPrivacyPolicyPm> = RegistrationPrivacyPolicyPm::class.java

    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(EXTRA_URL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitleView.text = getString(R.string.registration_privacy_policy_toolbar_title)
        privacyContentScrollView.viewTreeObserver.addOnScrollChangedListener {
            toolbarView.z = when (privacyContentScrollView.scrollY) {
                0 -> ZERO_Z_INDEX
                else -> DEFAULT_Z_INDEX
            }
        }

        with(webView.settings) {
            builtInZoomControls = false
            displayZoomControls = false
        }

        url?.let { webView.loadUrl(it) }
    }

    override fun onBindPresentationModel(pm: RegistrationPrivacyPolicyPm) {
        homeButtonView.clicks().bindTo { dialog.dismiss() }
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
