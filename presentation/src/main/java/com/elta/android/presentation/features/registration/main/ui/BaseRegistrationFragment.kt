package com.elta.android.presentation.features.registration.main.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.features.registration.main.pm.BaseRegistrationPm
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.utils.clickableSpan
import com.jakewharton.rxbinding2.widget.checkedChanges
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import com.nullgr.core.ui.fragments.showDialog
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_auth_base.*

abstract class BaseRegistrationFragment<PM : BaseRegistrationPm> : BaseAuthFragment<PM>() {

    override val menuButtonText: Int = R.string.registration_main_toolbar_button_text
    override val continueButtonText: Int = R.string.registration_main_button_continue
    override val authTitleText: Int = R.string.registration_main_title_new_user
    override val authSubtitleText: Int = R.string.registration_main_subtitle

    protected lateinit var privacyPolicyClicks: Observable<Unit>
    protected lateinit var personalDataClicks: Observable<Unit>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        privacyPolicyView.show()
        authTitleIconView.hide()
        privacyPolicyClicks = policyDescriptionTextView
            .clickableSpan(getString(R.string.registration_main_privacy_policy_clickable_mask))
        personalDataClicks = policyDescriptionTextView
            .clickableSpan(getString(R.string.registration_main_personal_data_clickable_mask))
    }

    override fun onBindPresentationModel(pm: PM) {
        super.onBindPresentationModel(pm)
        privacyPolicyClicks.bindTo(pm.privacyPolicyClickAction)
        personalDataClicks.bindTo(pm.personalDataClickAction)
        pm.openPrivacyPolicyCommand.bindTo {
            childFragmentManager.showDialog(RegistrationPrivacyPolicyFragment.newInstance())
        }
        pm.openPersonalDataCommand.bindTo {

        }

        policyConfirmationCheckBoxView.checkedChanges().bindTo(pm.privacyPolicyAcceptAction)
    }
}