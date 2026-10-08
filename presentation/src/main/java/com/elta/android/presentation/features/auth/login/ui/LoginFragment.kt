package com.elta.android.presentation.features.auth.login.ui

import com.elta.android.presentation.features.auth.login.model.LoginMode

class LoginFragment : BaseLoginComposeFragment() {
    override val loginMode = LoginMode.Recoverable

    companion object {
        fun newInstance() = LoginFragment()
    }
}
