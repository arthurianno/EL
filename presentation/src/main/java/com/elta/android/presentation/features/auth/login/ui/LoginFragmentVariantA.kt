package com.elta.android.presentation.features.auth.login.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.login.pm.LoginPmVariantA
import com.elta.android.presentation.features.registration.main.variantA.ui.BaseRegistrationFragmentVariantA
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show

// fixme Variant A : recovery_account
class LoginFragmentVariantA : BaseRegistrationFragmentVariantA<LoginPmVariantA>() {

    override val menuButtonText: Int = R.string.auth_toolbar_button_text
    override val continueButtonText: Int = R.string.auth_button_continue
    override val authTitleText: Int = R.string.auth_title
    override val authSubtitleText: Int = R.string.auth_subtitle
    override val classToken: Class<LoginPmVariantA> = LoginPmVariantA::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            privacyPolicyView.hide()
            authTitleIconView.show()
        }
    }

    companion object {
        fun newInstance(): LoginFragmentVariantA = LoginFragmentVariantA()
    }
}
