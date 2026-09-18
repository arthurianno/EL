package com.elta.android.presentation.features.registration.main.variantA.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.features.registration.main.variantA.pm.RegistrationMainPmVariantA
import com.elta.android.presentation.features.registration.main.ui.BaseRegistrationComposeFragment
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class RegistrationMainFragmentVariantA : BaseRegistrationComposeFragment<RegistrationMainPmVariantA>() {

    override val classToken: Class<RegistrationMainPmVariantA> = RegistrationMainPmVariantA::class.java

    override fun onBindPresentationModel(pm: RegistrationMainPmVariantA) {
        super.onBindPresentationModel(pm)
        bindRegistrationForm(
            emailInput = pm.emailInput,
            passwordInput = pm.passwordInput,
            consentState = pm.privacyPolicyAcceptedState,
            onConsentChanged = { pm.privacyPolicyAcceptAction.consumer.accept(it) },
            onSubmit = { pm.continueAction.consumer.accept(Unit) },
            onBack = { pm.backHandleAction.consumer.accept(Unit) },
            onLogin = { pm.menuAction.consumer.accept(Unit) },
            onPrivacyPolicy = { pm.privacyPolicyClickAction.consumer.accept(Unit) },
            onPersonalData = { pm.personalDataClickAction.consumer.accept(Unit) }
        )
        pm.openPrivacyPolicyCommand.bindTo { showDocument(R.string.registration_privacy_policy) }
        pm.openPersonalDataCommand.bindTo { showDocument(R.string.registration_personal_data) }
        pm.profileIsDeletedDialogControl.bindTo { data, control -> createDialog(this, control, data) }
    }

    companion object {
        fun newInstance(): RegistrationMainFragmentVariantA = RegistrationMainFragmentVariantA()
    }
}
