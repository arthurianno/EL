package com.elta.android.presentation.features.profile.settings.gender.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.profile.settings.gender.pm.ProfileSetGenderPm
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import kotlinx.android.synthetic.main.fragment_profile_set_gender.*

class ProfileSetGenderFragment : BaseFragment<ProfileSetGenderPm>() {

    override val screenLayout: Int = R.layout.fragment_profile_set_gender
    override val classToken: Class<ProfileSetGenderPm> = ProfileSetGenderPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onBindPresentationModel(pm: ProfileSetGenderPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        pm.checkNotSpecifiedVisibility.bindTo(notSpecifiedButtonView.visibility())
        pm.checkNotSpecified.bindTo(notSpecifiedButtonView)
        pm.checkMale.bindTo(maleButtonView)
        pm.checkFemale.bindTo(femaleButtonView)
        pm.saveChangesEnableState.bindTo { continueButtonView.isEnabled = it }
        continueButtonView.clicks().bindTo(pm.continueAction)
        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    override fun handleBack() {
        passTo(presentationModel.backHandleAction)
    }

    companion object {
        fun newInstance() = ProfileSetGenderFragment()
    }
}