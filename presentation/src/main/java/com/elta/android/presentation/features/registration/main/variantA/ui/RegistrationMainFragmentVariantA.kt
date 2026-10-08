package com.elta.android.presentation.features.registration.main.variantA.ui

import com.elta.android.presentation.features.registration.main.model.RegistrationMode
import com.elta.android.presentation.features.registration.main.ui.BaseRegistrationComposeFragment

class RegistrationMainFragmentVariantA : BaseRegistrationComposeFragment() {
    override val registrationMode = RegistrationMode.Legacy

    companion object {
        fun newInstance(): RegistrationMainFragmentVariantA = RegistrationMainFragmentVariantA()
    }
}
