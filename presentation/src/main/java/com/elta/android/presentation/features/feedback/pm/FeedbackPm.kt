package com.elta.android.presentation.features.feedback.pm

import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.feedback.interactor.SendFeedbackUseCase
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class FeedbackPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val sendFeedbackUseCase: SendFeedbackUseCase,
    serviceFacade: ServiceFacade
) : BasePm(serviceFacade) {

    val sendFeedbackEnabledState = State(false)
    val nameInput = inputControl()
    val emailInput = inputControl(hideErrorOnUserInput = false)
    val messageInput = inputControl()
    val continueAction = Action<Unit>()

    private val getProfileAction = Action<Unit>()
    private val isEmailValidState = State(false)

    override fun onCreate() {
        super.onCreate()
        bindProfileAction()
        bindContinueAction()
        bindInputs()
    }

    private fun bindProfileAction() {
        getProfileAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                getProfileUseCase.execute(Unit)
                    .bindProgress()
                    .map { it.email ?: "" }
                    .doOnSuccess(emailInput.text.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(getProfileAction.consumer)
            .untilDestroy()
    }

    private fun bindContinueAction() =
        continueAction.observable
            .skipWhileInProgress()
            .map(::createParams)
            .flatMapCompletable { params ->
                sendFeedbackUseCase.execute(params)
                    .bindProgress()
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

    private fun bindInputs() {
        emailInput.text.observable
            .map(::validateEmail)
            .map(::getEmailError)
            .subscribe(emailInput.error.consumer)
            .untilDestroy()

        Observables.combineLatest(
            nameInput.text.observable,
            isEmailValidState.observable,
            messageInput.text.observable
        ) { name, isEmailValid, message ->
            name.isNotEmpty() && isEmailValid && message.isNotEmpty()
        }
            .subscribe(sendFeedbackEnabledState.consumer)
            .untilDestroy()
    }

    private fun validateEmail(email: String): Boolean =
        email.isEmpty() || isEmailValid(email).also { isEmailValidState.consumer.accept(it) }

    private fun getEmailError(isEmailValid: Boolean): String =
        if (isEmailValid) "" else resources.getString(R.string.registration_error_input_email)

    private fun createParams(i: Unit): SendFeedbackUseCase.Params =
        SendFeedbackUseCase.Params(
            nameInput.text.value,
            emailInput.text.value,
            messageInput.text.value
        )

    private fun handleSuccess() {
        hideKeyBoardCommand.consumer.accept(Unit)
        router.exit()
    }
}