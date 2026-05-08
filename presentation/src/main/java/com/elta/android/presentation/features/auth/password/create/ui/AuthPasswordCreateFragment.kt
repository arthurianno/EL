package com.elta.android.presentation.features.auth.password.create.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentAuthPasswordCreateBinding
import com.elta.android.presentation.features.auth.password.create.pm.AuthPasswordCreatePm
import com.elta.android.presentation.utils.applyStatusBarInsetsPadding
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.toggleSecure
import com.elta.android.presentation.utils.toggleSecureIcon
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class AuthPasswordCreateFragment :
    BaseFragment<AuthPasswordCreatePm, FragmentAuthPasswordCreateBinding>(
        FragmentAuthPasswordCreateBinding::inflate
    ) {

    override val screenLayout: Int = R.layout.fragment_auth_password_create
    override val classToken: Class<AuthPasswordCreatePm> = AuthPasswordCreatePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.get(EXTRA_TOKEN)?.let {
            presentationModel.passToken(it as String)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applyStatusBarInsetsPadding(applyNavigationBarInset = true)
        binding.toolbar.homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: AuthPasswordCreatePm) {
        super.onBindPresentationModel(pm)
        bindScreenConfig(pm){
            withBackgroundImage(binding.headerImageView, R.drawable.ic_welcome)  // Другая картинка!
            withTitle(binding.authPasswordNewTitle, R.string.auth_password_recovery_title)
            withDescription(binding.authPasswordNewSubTitle, R.string.auth_password_recovery_subtitle)
            //withRootView(binding.root)
        }
        binding.passwordVisibilityButtonView.clicks()
            .subscribe { binding.passwordVisibilityButtonView.toggleSecureIcon(binding.passwordInputView.toggleSecure()) }
        pm.passwordInput.bindTo(binding.passwordInputView)
        pm.passwordInput.error.observable
            .distinctUntilChanged()
            .subscribe(binding.passwordInputView.error())
        pm.continueEnabledState.bindTo { binding.saveButtonView.isEnabled = it }
        binding.saveButtonView.clicks().bindTo(pm.continueAction)
        bindProgressDialog(pm)
    }

    companion object {
        fun newInstance(token: String): AuthPasswordCreateFragment =
            AuthPasswordCreateFragment().apply {
                arguments = bundle(EXTRA_TOKEN to token)
            }

        private const val EXTRA_TOKEN = "extra_token"
    }
}
