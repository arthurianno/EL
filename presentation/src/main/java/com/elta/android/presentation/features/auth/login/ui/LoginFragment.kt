package com.elta.android.presentation.features.auth.login.ui

import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.features.auth.login.pm.LoginPm
import me.dmdev.rxpm.widget.bindTo

class LoginFragment : BaseLoginComposeFragment<LoginPm>() {

    override val classToken: Class<LoginPm> = LoginPm::class.java

    override fun onBindPresentationModel(pm: LoginPm) {
        super.onBindPresentationModel(pm)
        bindLoginForm(
            emailInput = pm.emailInput,
            passwordInput = pm.passwordInput,
            onSubmit = { pm.continueAction.consumer.accept(Unit) },
            onBack = { pm.backHandleAction.consumer.accept(Unit) },
            onForgotPassword = { pm.menuAction.consumer.accept(Unit) }
        )
        pm.profileIsDeletedDialogControl.bindTo { data, control -> createDialog(this, control, data) }
        pm.profileRestoredDialogControl.bindTo { data, control -> createDialog(this, control, data) }
    }

    companion object {
        fun newInstance(): LoginFragment = LoginFragment()
    }
}
