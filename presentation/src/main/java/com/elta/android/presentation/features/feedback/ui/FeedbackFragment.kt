package com.elta.android.presentation.features.feedback.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentFeedbackBinding
import com.elta.android.presentation.features.feedback.pm.FeedbackPm
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.hideKeyboardFun
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class FeedbackFragment :
    BaseFragment<FeedbackPm, FragmentFeedbackBinding>(FragmentFeedbackBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_feedback
    override val classToken: Class<FeedbackPm> = FeedbackPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbarTitleView.text = getString(R.string.feedback_title)
        binding.toolbar.homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: FeedbackPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)

        pm.emailInput bindTo binding.emailInputView
        pm.emailInput.error.observable
            .distinctUntilChanged()
            .subscribe(binding.emailInputView.error())
        pm.nameInput bindTo binding.nameInputView
        pm.messageInput bindTo binding.messageInputView
        pm.sendFeedbackEnabledState bindTo { binding.sendFeedbackButtonView.isEnabled = it }
        binding.sendFeedbackButtonView.clicks() bindTo pm.continueAction
    }

    override fun handleBack() {
        view?.hideKeyboardFun()
        super.handleBack()
    }

    companion object {
        fun newInstance() = FeedbackFragment()
    }
}
