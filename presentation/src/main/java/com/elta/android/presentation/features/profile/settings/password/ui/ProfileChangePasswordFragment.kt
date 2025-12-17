package com.elta.android.presentation.features.profile.settings.password.ui

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentProfileChangePasswordBinding
import com.elta.android.presentation.features.profile.settings.password.pm.ProfileChangePasswordPm
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.toggleSecure
import com.elta.android.presentation.utils.toggleSecureIcon
import com.elta.android.presentation.widgets.status.Visibility
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import me.dmdev.rxpm.widget.InputControl
import me.dmdev.rxpm.widget.bindTo

class ProfileChangePasswordFragment :
    BaseFragment<ProfileChangePasswordPm, FragmentProfileChangePasswordBinding>(
        FragmentProfileChangePasswordBinding::inflate
    ) {
    companion object {
        fun newInstance() = ProfileChangePasswordFragment()
    }

    override val screenLayout = R.layout.fragment_profile_change_password
    override val classToken = ProfileChangePasswordPm::class.java
    override val statusBarConfigProvider = LightStatusBarConfigProvider

    override fun onBindPresentationModel(pm: ProfileChangePasswordPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        binding.root.visibility = View.INVISIBLE
        pm.screenConfigState.bindTo { screenEntity ->
            binding.titleView.text = screenEntity?.title ?: getString(R.string.profile_settings_change_password_title)
            binding.descriptionView.text = screenEntity?.description ?: getString(R.string.profile_settings_change_password_description)
            binding.root.visibility = View.VISIBLE
        }
        binding.oldPasswordVisibilityView.bindToggleTo(binding.oldPasswordView)
        binding.newPasswordVisibilityView.bindToggleTo(binding.newPasswordView)
        binding.toolbar.homeButtonView.clicks().bindTo(pm.backHandleAction)
        binding.changePasswordView.clicks().bindTo(pm.continueAction)

        pm.oldPasswordInput.bindInputTo(binding.oldPasswordView)
        pm.newPasswordInput.bindInputTo(binding.newPasswordView)
        pm.changePasswordEnabledState.bindTo { binding.changePasswordView.isEnabled = it }
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            view?.hideKeyboardFun()?.passTo(presentationModel.backHandleAction)
        }
    }

    private infix fun AppCompatImageView.bindToggleTo(view: AppCompatEditText) =
        clicks().subscribe { toggleSecureIcon(view.toggleSecure()) }

    private infix fun InputControl.bindInputTo(view: AppCompatEditText) {
        bindTo(view)
        error.observable.distinctUntilChanged().subscribe(view.error())
    }
}
