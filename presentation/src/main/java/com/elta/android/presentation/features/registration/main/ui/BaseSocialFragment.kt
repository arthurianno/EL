package com.elta.android.presentation.features.registration.main.ui

import com.elta.android.presentation.features.registration.main.pm.BaseSocialPm
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo

abstract class BaseSocialFragment<PM : BaseSocialPm> : BaseRegistrationFragment<PM>() {

    override fun onBindPresentationModel(pm: PM) {
        super.onBindPresentationModel(pm)
        with(binding) {
            facebookButtonView.clicks().bindTo(pm.fbAction)
            vkButtonView.clicks().bindTo(pm.vkAction)
            okButtonView.clicks().bindTo(pm.okAction)
        }
    }
}
