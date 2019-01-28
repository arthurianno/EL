package com.elta.android.presentation.features.registration.main.ui

import com.elta.android.presentation.features.registration.main.pm.BaseSocialPm
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_auth_base.*

abstract class BaseSocialFragment<PM : BaseSocialPm> : BaseRegistrationFragment<PM>() {

    override fun onBindPresentationModel(pm: PM) {
        super.onBindPresentationModel(pm)
        facebookButtonView.clicks().bindTo(pm.fbAction)
        vkButtonView.clicks().bindTo(pm.vkAction)
        okButtonView.clicks().bindTo(pm.okAction)
    }
}