package com.elta.android.presentation.features.auth.login.ui

import com.elta.android.presentation.features.auth.login.model.LoginMode

class LoginFragmentVariantA : BaseLoginComposeFragment() {
    override val loginMode = LoginMode.Legacy

    companion object {
        fun newInstance() = LoginFragmentVariantA()
    }
}
