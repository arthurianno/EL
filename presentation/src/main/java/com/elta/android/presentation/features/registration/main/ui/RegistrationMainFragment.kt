package com.elta.android.presentation.features.registration.main.ui;

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.registration.main.pm.RegistrationMainPm
import com.elta.android.presentation.utils.clickableSpan
import com.elta.android.presentation.utils.toggleSecure
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_registration_main.*
import kotlinx.android.synthetic.main.layout_auth_toolbar.*

class RegistrationMainFragment : BaseFragment<RegistrationMainPm>() {

    override val screenLayout: Int = R.layout.fragment_registration_main
    override val classToken: Class<RegistrationMainPm> = RegistrationMainPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    companion object {
        fun newInstance() = RegistrationMainFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuButtonView.setText(R.string.registration_main_toolbar_button_text)
    }

    override fun onBindPresentationModel(pm: RegistrationMainPm) {
        super.onBindPresentationModel(pm)
        policyDescriptionTextView.clickableSpan(
            getString(R.string.registration_main_privacy_policy_clickable_mask)
        ).bindTo { router.navigateTo(Screens.OnBoardingFlow) } // TODO bind to Pm command
        passwordVisibilityButtonView.clicks().bindTo {
            when (passwordInputView.toggleSecure()) {
                true -> passwordVisibilityButtonView.setImageResource(R.drawable.ic_show_password)
                else -> passwordVisibilityButtonView.setImageResource(R.drawable.ic_password_hide)
            }
        }
    }
}
