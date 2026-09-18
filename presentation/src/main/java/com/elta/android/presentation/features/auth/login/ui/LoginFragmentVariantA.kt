package com.elta.android.presentation.features.auth.login.ui

import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.features.auth.login.pm.LoginPmVariantA
import me.dmdev.rxpm.widget.bindTo

class LoginFragmentVariantA : BaseLoginComposeFragment<LoginPmVariantA>() {

    override val classToken: Class<LoginPmVariantA> = LoginPmVariantA::class.java

    override fun onBindPresentationModel(pm: LoginPmVariantA) {
        super.onBindPresentationModel(pm)
        bindLoginForm(
            emailInput = pm.emailInput,
            passwordInput = pm.passwordInput,
            onSubmit = { pm.continueAction.consumer.accept(Unit) },
            onBack = { pm.backHandleAction.consumer.accept(Unit) },
            onForgotPassword = { pm.menuAction.consumer.accept(Unit) }
        )
        pm.profileIsDeletedDialogControl.bindTo { data, control -> createDialog(this, control, data) }
    }

    companion object {
        fun newInstance(): LoginFragmentVariantA = LoginFragmentVariantA()
    }
}
