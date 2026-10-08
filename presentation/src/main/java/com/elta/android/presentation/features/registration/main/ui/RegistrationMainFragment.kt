package com.elta.android.presentation.features.registration.main.ui

import com.elta.android.presentation.features.registration.main.model.RegistrationMode

class RegistrationMainFragment : BaseRegistrationComposeFragment() {
    override val registrationMode = RegistrationMode.Recoverable

    companion object {
        fun newInstance(): RegistrationMainFragment = RegistrationMainFragment()
    }
}
