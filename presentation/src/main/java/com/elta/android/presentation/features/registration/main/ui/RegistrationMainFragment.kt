package com.elta.android.presentation.features.registration.main.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.registration.main.pm.RegistrationMainPm
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.utils.clickableSpan
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.fadeVisibility
import com.elta.android.presentation.utils.toggleSecure
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.fragments.showDialog
import kotlinx.android.synthetic.main.fragment_registration_main.*
import kotlinx.android.synthetic.main.layout_auth_toolbar.*

class RegistrationMainFragment : BaseFragment<RegistrationMainPm>() {

    override val screenLayout: Int = R.layout.fragment_registration_main
    override val classToken: Class<RegistrationMainPm> = RegistrationMainPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuButtonView.setText(R.string.registration_main_toolbar_button_text)
    }

    override fun onBindPresentationModel(pm: RegistrationMainPm) {
        super.onBindPresentationModel(pm)
        policyDescriptionTextView
            .clickableSpan(getString(R.string.registration_main_privacy_policy_clickable_mask))
            .bindTo(pm.privacyPolicyClickAction)
        pm.openPrivacyPolicyCommand.bindTo {
            childFragmentManager.showDialog(RegistrationPrivacyPolicyFragment.newInstance())
        }
        passwordVisibilityButtonView.clicks().bindTo {
            passwordVisibilityButtonView.toggleSecureIcon(passwordInputView.toggleSecure())
        }

        pm.emailInput.bindTo(emailInputView)
        pm.emailInput.error.observable.bindTo(emailInputView.error())
        pm.emailInput.error.observable.map(String::isNotEmpty).distinctUntilChanged().bindTo(emailErrorIconView.fadeVisibility())
    }

    private fun ImageView.toggleSecureIcon(isSecure: Boolean) {
        setImageResource(when (isSecure) {
            true -> R.drawable.ic_show_password
            else -> R.drawable.ic_password_hide
        })
    }

    companion object {
        fun newInstance() = RegistrationMainFragment()
    }
}
