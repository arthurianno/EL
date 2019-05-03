package com.elta.android.presentation.features.profile.settings.name.ui

import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.profile.settings.name.pm.ProfileSetNamePm
import com.elta.android.presentation.utils.hideKeyboardFun
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_profile_set_name.*

class ProfileSetNameFragment : BaseFragment<ProfileSetNamePm>() {

    override val screenLayout: Int = R.layout.fragment_profile_set_name
    override val classToken: Class<ProfileSetNamePm> = ProfileSetNamePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onBindPresentationModel(pm: ProfileSetNamePm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)

        pm.firstNameInput.bindTo(nameInputView)
        pm.secondNameInput.bindTo(surnameInputView)
        pm.saveChangesEnableState.bindTo { continueButtonView.isEnabled = it }
        pm.fullNameSate.bindTo { person ->
            nameInputView.setText(person.firstName)
            surnameInputView.setText(person.secondName)
        }
        continueButtonView.clicks().bindTo(pm.continueAction)
        pm.exitDialogControl.bindTo { data, dc ->
            MaterialDialog.Builder(checkNotNull(activity))
                .cancelable(false)
                .title(data.title)
                .content(data.message)
                .negativeText(data.negative)
                .positiveText(data.positive)
                .onPositive { _, _ -> dc.sendResult(ProfileSetNamePm.DialogResult.POSITIVE) }
                .onNegative { _, _ -> dc.sendResult(ProfileSetNamePm.DialogResult.NEGATIVE) }
                .build()
        }
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
    }

    override fun handleBack() {
        view?.hideKeyboardFun()
        passTo(presentationModel.backHandleAction)
    }

    companion object {
        fun newInstance() = ProfileSetNameFragment()
    }
}