package com.elta.android.presentation.features.registration.confirmation.ui;

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.registration.confirmation.pm.EmailConfirmationPm
import com.elta.android.presentation.utils.bundle

class EmailConfirmationFragment : BaseFragment<EmailConfirmationPm>() {

    override val screenLayout: Int = R.layout.fragment_email_confirmation
    override val classToken: Class<EmailConfirmationPm> = EmailConfirmationPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.get(EXTRA_TOKEN)?.let {
            presentationModel.passToken(it as String)
        }
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
