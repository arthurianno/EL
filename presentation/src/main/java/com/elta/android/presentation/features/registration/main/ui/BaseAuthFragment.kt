package com.elta.android.presentation.features.registration.main.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.registration.main.pm.BaseAuthPm
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.fadeVisibility
import com.elta.android.presentation.utils.toggleSecure
import com.elta.android.presentation.utils.toggleSecureIcon
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_auth_base.*
import kotlinx.android.synthetic.main.layout_toolbar.*

abstract class BaseAuthFragment<PM : BaseAuthPm> : BaseFragment<PM>() {

    protected abstract val menuButtonText: Int
    protected abstract val continueButtonText: Int
    protected abstract val authTitleText: Int
    protected abstract val authSubtitleText: Int

    override val screenLayout: Int = R.layout.fragment_auth_base
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuButtonView.setText(menuButtonText)
        continueButtonView.setText(continueButtonText)
        authTitleView.setText(authTitleText)
        authSubtitleView.setText(authSubtitleText)
    }

    @Suppress("LongMethod")
    override fun onBindPresentationModel(pm: PM) {
        super.onBindPresentationModel(pm)
        passwordVisibilityButtonView.clicks().bindTo {
            passwordVisibilityButtonView.toggleSecureIcon(passwordInputView.toggleSecure())
        }

        pm.emailInput.bindTo(emailInputView)
        pm.emailInput.error.observable
            .distinctUntilChanged()
            .bindTo(emailInputView.error())
        pm.emailInput.error.observable
            .map(String::isNotEmpty)
            .distinctUntilChanged()
            .bindTo(emailErrorIconView.fadeVisibility())

        pm.passwordInput.bindTo(passwordInputView)
        pm.passwordInput.error.observable
            .distinctUntilChanged()
            .bindTo(passwordInputView.error())

        pm.continueEnabledState.bindTo { continueButtonView.isEnabled = it }
        continueButtonView.clicks().bindTo(pm.continueAction)
        menuButtonView.clicks().bindTo(pm.menuAction)
        bindProgressDialog(pm)
    }
}