package com.elta.android.presentation.features.auth.password.recovery.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentAuthPasswordRecoveryBinding
import com.elta.android.presentation.features.auth.password.recovery.pm.AuthPasswordRecoveryPm
import com.elta.android.presentation.utils.applyStatusBarInsetsPadding
import com.elta.android.presentation.utils.error
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class AuthPasswordRecoveryFragment :
    BaseFragment<AuthPasswordRecoveryPm, FragmentAuthPasswordRecoveryBinding>(
        FragmentAuthPasswordRecoveryBinding::inflate
    ) {

    override val screenLayout: Int = R.layout.fragment_auth_password_recovery
    override val classToken: Class<AuthPasswordRecoveryPm> = AuthPasswordRecoveryPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applyStatusBarInsetsPadding(applyNavigationBarInset = true)
        binding.toolbar.homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: AuthPasswordRecoveryPm) {
        super.onBindPresentationModel(pm)
        bindScreenConfig(pm){
            withBackgroundImage(binding.backgroundImageView, R.drawable.ic_welcome)  // Другая картинка!
            withTitle(binding.authPasswordRecoveryTitle, R.string.auth_password_recovery_title)
            withDescription(binding.authPasswordRecoverySubTitle, R.string.auth_password_recovery_subtitle)
            withRootView(binding.root)
        }
        pm.emailInput.bindTo(binding.emailInputView)
        pm.emailInput.error.observable
            .distinctUntilChanged()
            .subscribe(binding.emailInputView.error())
        pm.continueEnabledState.bindTo { binding.sendLinkButtonView.isEnabled = it }
        binding.sendLinkButtonView.clicks().bindTo(pm.continueAction)
        pm.profileIsDeletedDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        bindProgressDialog(pm)
    }

    companion object {
        fun newInstance() = AuthPasswordRecoveryFragment()
    }
}
