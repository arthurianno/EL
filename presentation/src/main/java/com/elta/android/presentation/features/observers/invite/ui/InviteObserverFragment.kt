package com.elta.android.presentation.features.observers.invite.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.observers.invite.pm.InviteObserverPm
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.fadeVisibility
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fregment_invite_observer.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class InviteObserverFragment : BaseFragment<InviteObserverPm>() {

    override val screenLayout: Int = R.layout.fregment_invite_observer
    override val classToken: Class<InviteObserverPm> = InviteObserverPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: InviteObserverPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)

        pm.emailInput.bindTo(emailInputView)
        pm.emailInput.error.observable
            .distinctUntilChanged()
            .bindTo(emailInputView.error())
        pm.emailInput.error.observable
            .map(String::isNotEmpty)
            .distinctUntilChanged()
            .bindTo(emailErrorIconView.fadeVisibility())

        pm.continueEnabledState.bindTo { continueButtonView.isEnabled = it }
        continueButtonView.clicks().bindTo(pm.continueAction)
    }

    companion object {
        fun newInstance() = InviteObserverFragment()
    }
}