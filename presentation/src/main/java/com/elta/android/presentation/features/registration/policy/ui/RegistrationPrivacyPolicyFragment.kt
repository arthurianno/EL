package com.elta.android.presentation.features.registration.policy.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.features.registration.policy.pm.RegistrationPrivacyPolicyPm
import com.elta.android.presentation.utils.htmlText
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_registration_privacy_policy.*
import kotlinx.android.synthetic.main.layout_bottom_sheet_toolbar.*

class RegistrationPrivacyPolicyFragment : BaseBottomSheetFragment<RegistrationPrivacyPolicyPm>() {

    override val screenLayout: Int = R.layout.fragment_registration_privacy_policy
    override val classToken: Class<RegistrationPrivacyPolicyPm> = RegistrationPrivacyPolicyPm::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        privacyPolicyTextView.htmlText = getString(R.string.registration_privacy_policy_text)
        toolbarTitleView.text = getString(R.string.registration_privacy_policy_toolbar_title)
        homeButtonView.clicks().bindTo { dismissAllowingStateLoss() }
        privacyContentScrollView.viewTreeObserver.addOnScrollChangedListener {
            toolbarView.z = when (privacyContentScrollView.scrollY) {
                0 -> ZERO_Z_INDEX
                else -> DEFAULT_Z_INDEX
            }
        }
    }

    override fun onBindPresentationModel(pm: RegistrationPrivacyPolicyPm) {}

    companion object {
        fun newInstance() = RegistrationPrivacyPolicyFragment()
        private const val ZERO_Z_INDEX = 0f
        private const val DEFAULT_Z_INDEX = 60f
    }
}
