package com.elta.android.presentation.features.registration.main.ui

import com.elta.android.presentation.features.registration.main.pm.RegistrationMainPm

class RegistrationMainFragment : BaseSocialFragment<RegistrationMainPm>() {

    override val classToken: Class<RegistrationMainPm> = RegistrationMainPm::class.java

    companion object {
        fun newInstance(): RegistrationMainFragment = RegistrationMainFragment()
    }
}
