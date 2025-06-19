package com.elta.android.presentation.features.registration.main.variantA.ui

import com.elta.android.presentation.features.registration.main.variantA.pm.RegistrationMainPmVariantA

// fixme Variant A : recovery_account
class RegistrationMainFragmentVariantA : BaseRegistrationFragmentVariantA<RegistrationMainPmVariantA>() {

    override val classToken: Class<RegistrationMainPmVariantA> = RegistrationMainPmVariantA::class.java

    companion object {
        fun newInstance(): RegistrationMainFragmentVariantA = RegistrationMainFragmentVariantA()
    }
}
