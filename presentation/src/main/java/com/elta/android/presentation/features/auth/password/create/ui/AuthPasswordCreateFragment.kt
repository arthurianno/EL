package com.elta.android.presentation.features.auth.password.create.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.auth.password.create.pm.AuthPasswordCreatePm
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.toggleSecure
import com.elta.android.presentation.utils.toggleSecureIcon
import com.elta.android.presentation.utils.visibility
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_auth_password_create.*
import kotlinx.android.synthetic.main.layout_auth_toolbar.*

class AuthPasswordCreateFragment : BaseFragment<AuthPasswordCreatePm>() {

    override val screenLayout: Int = R.layout.fragment_auth_password_create
    override val classToken: Class<AuthPasswordCreatePm> = AuthPasswordCreatePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presentationModel.passToken("Sometoken") // TODO here will be token from deep link
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: AuthPasswordCreatePm) {
        super.onBindPresentationModel(pm)
        passwordVisibilityButtonView.clicks().bindTo {
            passwordVisibilityButtonView.toggleSecureIcon(passwordInputView.toggleSecure())
        }
        pm.progressState.bindTo(progressDialog.visibility(childFragmentManager))
        pm.passwordInput.bindTo(passwordInputView)
        pm.passwordInput.error.observable
            .distinctUntilChanged()
            .bindTo(passwordInputView.error())
        pm.continueEnabledState.bindTo { saveButtonView.isEnabled = it }
        saveButtonView.clicks().bindTo(pm.continueAction)
    }

    companion object {
        fun newInstance(): AuthPasswordCreateFragment = AuthPasswordCreateFragment()
    }
}
