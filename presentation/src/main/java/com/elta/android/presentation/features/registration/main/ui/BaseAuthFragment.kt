package com.elta.android.presentation.features.registration.main.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentAuthBaseBinding
import com.elta.android.presentation.features.registration.main.pm.BaseAuthPm
import com.elta.android.presentation.utils.applyStatusBarInsetsPadding
import com.elta.android.presentation.utils.error
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.isKeyboardOpen
import com.elta.android.presentation.utils.lostFocusOnClickOutside
import com.elta.android.presentation.utils.toggleSecure
import com.elta.android.presentation.utils.toggleSecureIcon
import com.jakewharton.rxbinding2.view.clicks
import com.rengwuxian.materialedittext.MaterialEditText
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

abstract class BaseAuthFragment<PM : BaseAuthPm> :
    BaseFragment<PM, FragmentAuthBaseBinding>(FragmentAuthBaseBinding::inflate) {

    protected abstract val menuButtonText: Int
    protected abstract val continueButtonText: Int
    protected abstract val authTitleText: Int
    protected abstract val authSubtitleText: Int

    override val screenLayout: Int = R.layout.fragment_auth_base
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applyStatusBarInsetsPadding(
            onApplyInsets = ::clearFocusesFromInputs,
            applyNavigationBarInset = true,
            applyImeInset = true
        )
        with(binding) {
            toolbar.menuButtonView.setText(menuButtonText)
            continueButtonView.setText(continueButtonText)
            authTitleView.setText(authTitleText)
            authSubtitleView.setText(authSubtitleText)
        }
    }

    private fun clearFocusesFromInputs(view: View) {
        if (!requireActivity().isKeyboardOpen(view)) {
            // Код для случая, когда клавиатура ЗАКРЫЛАСЬ:
            binding.emailInputView.clearFocus()
            binding.passwordInputView.clearFocus()
        } else {
            // НОВЫЙ КОД для случая, когда клавиатура ОТКРЫЛАСЬ:
            binding.scrollView.postDelayed({
                // Вариант А: Скролл до самого низа (чтобы увидеть пароль и чекбокс)
                binding.scrollView.fullScroll(View.FOCUS_DOWN)

                // Вариант Б (альтернатива): Точный скролл к верхней границе пароля
                // binding.scrollView.smoothScrollTo(0, binding.passwordFrameLayout.top)
            }, 150) // Небольшая задержка, чтобы клавиатура успела выехать
        }
    }

    @Suppress("LongMethod")
    override fun onBindPresentationModel(pm: PM) {
        super.onBindPresentationModel(pm)
        binding.passwordVisibilityButtonView.clicks().subscribe {
            binding.passwordVisibilityButtonView.toggleSecureIcon(binding.passwordInputView.toggleSecure())
        }

        pm.emailInput.bindTo(binding.emailInputView)
        pm.emailInput.error.observable
            .distinctUntilChanged()
            .subscribe(binding.emailInputView.error())

        pm.passwordInput.bindTo(binding.passwordInputView)
        pm.passwordInput.error.observable
            .distinctUntilChanged()
            .subscribe(binding.passwordInputView.error())

        pm.continueEnabledState.bindTo { binding.continueButtonView.isEnabled = it }
        binding.continueButtonView.clicks().bindTo(pm.continueAction)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.menuAction)
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
        pm.profileIsDeletedDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        bindProgressDialog(pm)
    }
}
