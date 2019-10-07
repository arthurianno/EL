package com.elta.android.presentation.features.feedback.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.feedback.pm.FeedbackPm
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.hideKeyboardFun
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_feedback.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class FeedbackFragment : BaseFragment<FeedbackPm>() {

    override val screenLayout: Int = R.layout.fragment_feedback
    override val classToken: Class<FeedbackPm> = FeedbackPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitleView.text = getString(R.string.feedback_title)
        homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: FeedbackPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)

        pm.emailInput bindTo emailInputView
        pm.emailInput.error.observable
            .distinctUntilChanged()
            .bindTo(emailInputView.error())
        pm.nameInput bindTo nameInputView
        pm.messageInput bindTo messageInputView
        pm.sendFeedbackEnabledState bindTo { sendFeedbackButtonView.isEnabled = it }
        sendFeedbackButtonView.clicks() bindTo pm.continueAction
    }

    override fun handleBack() {
        view?.hideKeyboardFun()
        super.handleBack()
    }

    companion object {
        fun newInstance() = FeedbackFragment()
    }
}