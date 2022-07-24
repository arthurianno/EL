package com.elta.android.presentation.features.profile.settings.gender.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentProfileSetGenderBinding
import com.elta.android.presentation.features.profile.settings.gender.pm.ProfileSetGenderPm
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import me.dmdev.rxpm.widget.bindTo

class ProfileSetGenderFragment : BaseFragment<ProfileSetGenderPm, FragmentProfileSetGenderBinding>(
    FragmentProfileSetGenderBinding::inflate
) {

    override val screenLayout: Int = R.layout.fragment_profile_set_gender
    override val classToken: Class<ProfileSetGenderPm> = ProfileSetGenderPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onBindPresentationModel(pm: ProfileSetGenderPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        pm.checkNotSpecified.bindTo(binding.notSpecifiedButtonView)
        pm.checkMale.bindTo(binding.maleButtonView)
        pm.checkFemale.bindTo(binding.femaleButtonView)
        pm.saveChangesEnableState.bindTo { binding.continueButtonView.isEnabled = it }
        binding.continueButtonView.clicks().bindTo(pm.continueAction)
        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    override fun handleBack() {
        Unit.passTo(presentationModel.backHandleAction)
    }

    companion object {
        fun newInstance() = ProfileSetGenderFragment()
    }
}
