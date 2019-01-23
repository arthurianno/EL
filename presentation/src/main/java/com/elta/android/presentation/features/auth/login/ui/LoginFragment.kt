package com.elta.android.presentation.features.auth.login.ui;

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.login.pm.LoginPm
import com.elta.android.presentation.features.registration.main.ui.BaseAuthFragment
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import kotlinx.android.synthetic.main.fragment_auth_base.*

class LoginFragment : BaseAuthFragment<LoginPm>() {

    override val menuButtonText: Int = R.string.auth_toolbar_button_text
    override val continueButtonText: Int = R.string.auth_button_continue
    override val authTitleText: Int = R.string.auth_title
    override val authSubtitleText: Int = R.string.auth_subtitle
    override val classToken: Class<LoginPm> = LoginPm::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        privacyPolicyView.hide()
        authTitleIconView.show()
    }

    companion object {
        fun newInstance(): LoginFragment = LoginFragment()
    }
}
