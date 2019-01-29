package com.elta.android.presentation.features.registration.confirmation.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.registration.confirmation.pm.EmailConfirmationPm
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.visibility
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import kotlinx.android.synthetic.main.fragment_email_confirmation.*
import kotlinx.android.synthetic.main.layout_auth_toolbar.*

class EmailConfirmationFragment : BaseFragment<EmailConfirmationPm>() {

    override val screenLayout: Int = R.layout.fragment_email_confirmation
    override val classToken: Class<EmailConfirmationPm> = EmailConfirmationPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.get(EXTRA_TOKEN)?.let {
            presentationModel.passToken(it as String)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuButtonView.text = getString(R.string.registration_email_confirmation_login_button)
    }

    override fun onBindPresentationModel(pm: EmailConfirmationPm) {
        super.onBindPresentationModel(pm)
        menuButtonView.clicks().bindTo(pm.loginWithAnotherAccountAction)
        confirmationSuccessStateView.clicks().bindTo(pm.continueAction)
        pm.contentVisibilityCommand.bindTo(confirmationNextActionView.visibility())
        errorStateView.clicks().bindTo(pm.confirmEmailAction)
        pm.progressState.bindTo(progressDialog.visibility(childFragmentManager))
    }

    companion object {
        fun newInstance(token: String): EmailConfirmationFragment {
            return EmailConfirmationFragment().apply {
                arguments = bundle(EXTRA_TOKEN to token)
            }
        }

        private const val EXTRA_TOKEN = "extra_token"
    }
}
