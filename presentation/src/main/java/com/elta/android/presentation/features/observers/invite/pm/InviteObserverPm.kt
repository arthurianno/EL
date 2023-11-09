package com.elta.android.presentation.features.observers.invite.pm

import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.observers.interactor.GetObserverInvitesUseCase
import com.elta.android.domain.features.observers.interactor.SendObserverInviteUseCase
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.domain.features.observers.model.ObserverStatus
import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.messages.SnackBarMessageData
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

private const val EMPTY_STRING = ""

class InviteObserverPm @Inject constructor(
    private val getObserverInvitesUseCase: GetObserverInvitesUseCase,
    private val sendObserverInviteUseCase: SendObserverInviteUseCase,
    private val getProfileUseCase: GetUpdatedProfileUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val emailInput = inputControl(hideErrorOnUserInput = false)
    val continueEnabledState = state(false)
    val continueAction = action<Unit>()

    private val loadObserversAction = action<Unit>()
    private val observersState = state<List<Observer>>()
    private val userEmailState = state<String>()

    override fun onCreate() {
        super.onCreate()
        getProfileUseCase.execute()
            .map { it.email.orEmpty() }
            .subscribe(userEmailState.consumer)
            .untilDestroy()

        emailInput.text.observable
            .map {
                validateEmail(it) &&
                        !haveSameObserver() &&
                        !haveAwaitingObserver() &&
                        !isUserEmail()
            }
            .doOnNext(continueEnabledState.consumer)
            .map(::getEmailError)
            .subscribe(emailInput.error.consumer)
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .map(::createObserverInviteUseCaseParams)
            .flatMapCompletable {
                sendObserverInviteUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        loadObserversAction.observable
            .skipWhileInProgress()
            .flatMap {
                getObserverInvitesUseCase.execute()
                    .hideErrorContainer()
                    .bindProgress()
                    .mapFilter { it.status != ObserverStatus.EXPIRED }
                    .doOnNext(observersState.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(loadObserversAction.consumer)
            .untilDestroy()
    }

    private fun validateEmail(email: String): Boolean =
        email.isNotBlank() && isEmailValid(email)

    private fun haveSameObserver(): Boolean =
        observersState.valueOrNull
            ?.filter { it.status == ObserverStatus.CONFIRMED }
            ?.any { it.email.equals(emailInput.text.valueOrNull, true) }
            ?: false

    private fun haveAwaitingObserver(): Boolean =
        observersState.valueOrNull
            ?.filter { it.status == ObserverStatus.PENDING }
            ?.any { it.email == emailInput.text.valueOrNull }
            ?: false

    private fun isUserEmail(): Boolean =
        userEmailState.valueOrNull
            ?.equals(emailInput.text.valueOrNull)
            ?: false

    private fun getEmailError(isEmailValid: Boolean): String =
        when {
            emailInput.text.valueOrNull.isNullOrBlank() -> EMPTY_STRING
            haveSameObserver() -> resources.getString(R.string.registration_error_same_email)
            haveAwaitingObserver() -> resources.getString(R.string.registration_error_same_email_awaiting)
            isUserEmail() -> resources.getString(R.string.registration_error_current_user_email)
            !isEmailValid -> resources.getString(R.string.registration_error_input_email)
            else -> EMPTY_STRING
        }

    private fun handleSuccess() {
        hideKeyBoardCommand.consumer.accept(Unit)
        bus.event(Events.ObserverInvited)
        showSnackBar(
            SnackBarMessageData.SimpleTextMessage(
                resources.getString(R.string.profile_observer_invite_with_success)
            )
        )
        router.exit()
    }

    private fun createObserverInviteUseCaseParams(i: Unit) =
        SendObserverInviteUseCase.Params(emailInput.text.value)
}
