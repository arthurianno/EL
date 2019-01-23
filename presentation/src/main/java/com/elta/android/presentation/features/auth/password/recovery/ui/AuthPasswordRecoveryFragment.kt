package com.elta.android.presentation.features.auth.password.recovery.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.auth.password.recovery.pm.AuthPasswordRecoveryPm
import kotlinx.android.synthetic.main.layout_auth_toolbar.*

class AuthPasswordRecoveryFragment : BaseFragment<AuthPasswordRecoveryPm>() {

    override val screenLayout: Int = R.layout.fragment_auth_password_recovery
    override val classToken: Class<AuthPasswordRecoveryPm> = AuthPasswordRecoveryPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    companion object {
        fun newInstance() = AuthPasswordRecoveryFragment()
    }
}
