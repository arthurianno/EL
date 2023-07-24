package com.elta.android.presentation.features.profile.settings.name.ui

import android.content.Context
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentProfileSetNameBinding
import com.elta.android.presentation.features.profile.settings.name.pm.ProfileSetNamePm
import com.elta.android.presentation.utils.hideKeyboardFun
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import me.dmdev.rxpm.widget.bindTo

class ProfileSetNameFragment :
    BaseFragment<ProfileSetNamePm, FragmentProfileSetNameBinding>(FragmentProfileSetNameBinding::inflate) {
    companion object {
        fun newInstance() = ProfileSetNameFragment()
    }

    override val screenLayout: Int = R.layout.fragment_profile_set_name
    override val classToken: Class<ProfileSetNamePm> = ProfileSetNamePm::class.java

    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onBindPresentationModel(pm: ProfileSetNamePm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)

        binding.toolbar.homeButtonView.clicks().bindTo(pm.backHandleAction)
        binding.continueButtonView.clicks().bindTo(pm.continueAction)

        pm.firstNameInput.bindTo(binding.nameInputView)
        pm.firstNameInput.error.bindTo { binding.nameInputView.error = it.takeUnless { it.isBlank() } }
        pm.secondNameInput.bindTo(binding.surnameInputView)
        pm.secondNameInput.error.bindTo { binding.surnameInputView.error = it.takeUnless { it.isBlank() } }
        pm.saveChangesEnableState.bindTo { binding.continueButtonView.isEnabled = it }
        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            view?.hideKeyboardFun()?.passTo(presentationModel.backHandleAction)
        }
    }
}
