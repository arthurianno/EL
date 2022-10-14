package com.elta.android.presentation.features.profile.settings.name.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentProfileSetNameBinding
import com.elta.android.presentation.features.profile.settings.name.pm.ProfileSetNamePm
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.hideKeyboardFun
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import me.dmdev.rxpm.widget.bindTo

class ProfileSetNameFragment :
    BaseFragment<ProfileSetNamePm, FragmentProfileSetNameBinding>(FragmentProfileSetNameBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_profile_set_name
    override val classToken: Class<ProfileSetNamePm> = ProfileSetNamePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onBindPresentationModel(pm: ProfileSetNamePm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)

        with(pm.firstNameInput) {
            bindTo(binding.nameInputView)
            error.observable
                .distinctUntilChanged()
                .subscribe(binding.nameInputView.error())
        }
        with(pm.secondNameInput) {
            error.observable
                .distinctUntilChanged()
                .subscribe(binding.surnameInputView.error())
            bindTo(binding.surnameInputView)
        }
        pm.saveChangesEnableState.bindTo { binding.continueButtonView.isEnabled = it }
        binding.continueButtonView.clicks().bindTo(pm.continueAction)
        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
    }

    override fun handleBack() {
        view?.hideKeyboardFun()?.passTo(presentationModel.backHandleAction)
    }

    companion object {
        fun newInstance() = ProfileSetNameFragment()
    }
}
