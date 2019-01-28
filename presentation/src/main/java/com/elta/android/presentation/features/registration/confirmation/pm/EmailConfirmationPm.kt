package com.elta.android.presentation.features.registration.confirmation.pm

import com.elta.android.domain.features.auth.interactor.CheckTokenOwnerUseCase
import com.elta.android.domain.features.auth.interactor.ConfirmEmailUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import io.reactivex.Completable
import javax.inject.Inject

class EmailConfirmationPm @Inject constructor(
    private val confirmEmailUseCase: ConfirmEmailUseCase,
    private val checkTokenOwnerUseCase: CheckTokenOwnerUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val loginWithAnotherAccountAction = Action<Unit>()
    val continueAction = Action<Unit>()
    val contentVisibilityCommand = Command<Boolean>(bufferSize = 1)
    val confirmEmailAction = Action<Unit>()

    private val token = State<String>()

    override fun onCreate() {
        super.onCreate()

        confirmEmailAction.observable
            .skipWhileInProgress()
            .map(::createConfirmRequestParams)
            .flatMapSingle {
                confirmEmailUseCase.execute(it)
                    .hideErrorContainer()
                    .hideContent()
                    .andThen(
                        checkTokenOwnerUseCase.execute(CheckTokenOwnerUseCase.Params(token.value))
                    )
                    .bindProgress()
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.BINDED }
            .map { Unit }
            .subscribe(confirmEmailAction.consumer)
            .untilDestroy()

        continueAction.observable
            .doOnNext { router.exit() }
            .subscribe()
            .untilDestroy()

        loginWithAnotherAccountAction.observable
            .doOnNext { router.backTo(Screens.GreetingFlow) }
            .subscribe()
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        // TODO its not a real logic, should be implemented in errorHandler
        passToErrorViewVisibility(true)
    }

    fun passToken(token: String) {
        this.token.consumer.accept(token)
    }

    private fun handleSuccess(isOwner: Boolean) {
        when (isOwner) {
            false -> contentVisibilityCommand.consumer.accept(true)
            else -> router.newRootScreen(Screens.OnBoardingFlow)
        }
    }

    private fun createConfirmRequestParams(i: Unit): ConfirmEmailUseCase.Params =
        ConfirmEmailUseCase.Params(token.value)

    private inline fun Completable.hideContent(): Completable =
        this.doOnSubscribe { contentVisibilityCommand.consumer.accept(false) }
}