package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class RegistrationMainPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val emailInput = inputControl(hideErrorOnUserInput = false)
    val passwordInput = inputControl(hideErrorOnUserInput = false)
    val privacyPolicyAceptedState = State<Boolean>()
    val privacyPolicyClickAction = Action<Unit>()
    val openPrivacyPolicyCommand = Command<Unit>()

    override fun onCreate() {
        super.onCreate()

        emailInput.text.observable
            .map(::isEmailValid)
            .subscribe { isEmailValid ->
                val error = when(isEmailValid) {
                    true -> ""
                    else -> resources.getString(R.string.registration_error_input_email)
                }
                emailInput.error.consumer.accept(error)
            }
            .untilDestroy()

        privacyPolicyClickAction.observable
            .subscribe(openPrivacyPolicyCommand.consumer)
            .untilDestroy()
    }
}