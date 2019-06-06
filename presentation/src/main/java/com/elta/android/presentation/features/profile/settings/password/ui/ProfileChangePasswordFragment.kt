package com.elta.android.presentation.features.profile.settings.password.ui

import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatImageView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.features.profile.settings.password.pm.ProfileChangePasswordPm
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.toggleSecure
import com.elta.android.presentation.utils.toggleSecureIcon
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_profile_change_password.*
import me.dmdev.rxpm.widget.InputControl

class ProfileChangePasswordFragment : BaseFragment<ProfileChangePasswordPm>() {

    override val screenLayout = R.layout.fragment_profile_change_password
    override val classToken = ProfileChangePasswordPm::class.java
    override val statusBarConfigProvider = LightStatusBarConfigProvider

    override fun onBindPresentationModel(pm: ProfileChangePasswordPm) {
        super.onBindPresentationModel(pm)

        bindProgressDialog(pm)

        oldPasswordVisibilityView bindToggleTo oldPasswordView
        newPasswordVisibilityView bindToggleTo newPasswordView

        with(pm) {
            changePasswordView.clicks() bindTo continueAction
            oldPasswordInput bindInputTo oldPasswordView
            newPasswordInput bindInputTo newPasswordView
            changePasswordEnabledState bindTo { changePasswordView.isEnabled = it }
            hideKeyBoardCommand bindTo { view?.hideKeyboardFun() }
        }
        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    override fun handleBack() {
        view?.hideKeyboardFun()
        passTo(presentationModel.backHandleAction)
    }

    private infix fun AppCompatImageView.bindToggleTo(view: AppCompatEditText) =
        clicks() bindTo { toggleSecureIcon(view.toggleSecure()) }

    private infix fun InputControl.bindInputTo(view: AppCompatEditText) {
        bindTo(view)
        error.observable.distinctUntilChanged() bindTo view.error()
    }

    companion object {
        fun newInstance() = ProfileChangePasswordFragment()
    }
}