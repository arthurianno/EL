package com.elta.android.presentation.features.profile.settings.gender.ui

import android.content.Context
import androidx.core.view.isVisible
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentProfileSetGenderBinding
import com.elta.android.presentation.features.profile.settings.gender.pm.ProfileSetGenderPm
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class ProfileSetGenderFragment : BaseFragment<ProfileSetGenderPm, FragmentProfileSetGenderBinding>(
    FragmentProfileSetGenderBinding::inflate
) {
    companion object {
        fun newInstance() = ProfileSetGenderFragment()
    }

    override val screenLayout: Int = R.layout.fragment_profile_set_gender
    override val classToken: Class<ProfileSetGenderPm> = ProfileSetGenderPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onBindPresentationModel(pm: ProfileSetGenderPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        binding.toolbar.homeButtonView.clicks().bindTo(pm.backHandleAction)
        pm.progressState.bindTo { binding.radioGroup.isVisible = !it }
        pm.checkNotSpecifiedVisibility.bindTo(binding.notSpecifiedButtonView.visibility())
        pm.checkNotSpecified.bindTo(binding.notSpecifiedButtonView)
        pm.checkMale.bindTo(binding.maleButtonView)
        pm.checkFemale.bindTo(binding.femaleButtonView)
        pm.saveChangesEnableState.bindTo { binding.continueButtonView.isEnabled = it }
        binding.continueButtonView.clicks().bindTo(pm.continueAction)
        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            presentationModel.backHandleAction.consumer.accept(Unit)
        }
    }
}
