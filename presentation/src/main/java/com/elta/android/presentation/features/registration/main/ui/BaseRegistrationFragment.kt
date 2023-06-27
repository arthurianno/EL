package com.elta.android.presentation.features.registration.main.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.features.registration.main.pm.BaseRegistrationPm
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.utils.clickableSpan
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.checkedChanges
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import com.nullgr.core.ui.fragments.showDialog
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo

abstract class BaseRegistrationFragment<PM : BaseRegistrationPm> : BaseAuthFragment<PM>() {

    override val menuButtonText: Int = R.string.registration_main_toolbar_button_text
    override val continueButtonText: Int = R.string.registration_main_button_continue
    override val authTitleText: Int = R.string.registration_main_title_new_user
    override val authSubtitleText: Int = R.string.registration_main_subtitle


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.privacyPolicyView.show()
        binding.authTitleIconView.hide()
    }

    override fun onBindPresentationModel(pm: PM) {
        super.onBindPresentationModel(pm)
        binding.toolbar.homeButtonView.clicks()
            .subscribe(pm.backHandleAction.consumer)
        binding.policyDescriptionTextView.text =
            getString(R.string.registration_main_description_privacy_policy)
        binding.policyDescriptionTextView.clickableSpan(getString(R.string.registration_main_privacy_policy_clickable_mask))
            .bindTo(pm.privacyPolicyClickAction)
        binding.policyDescriptionTextView.clickableSpan(getString(R.string.registration_main_personal_data_clickable_mask))
            .bindTo(pm.personalDataClickAction)
        pm.openPrivacyPolicyCommand.bindTo {
            childFragmentManager.showDialog(
                RegistrationPrivacyPolicyFragment.newInstance(getString(R.string.registration_privacy_policy))
            )
        }
        pm.openPersonalDataCommand.bindTo {
            childFragmentManager.showDialog(
                RegistrationPrivacyPolicyFragment.newInstance(getString(R.string.registration_personal_data))
            )
        }

        binding.policyConfirmationCheckBoxView.checkedChanges().bindTo(pm.privacyPolicyAcceptAction)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            Unit.passTo(presentationModel.backHandleAction)
        }
    }
}
