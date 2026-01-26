package com.elta.android.presentation.features.registration.confirmation.pm

import com.elta.android.common.errors.EmailLinkInvalid
import com.elta.android.domain.features.auth.interactor.CheckTokenOwnerUseCase
import com.elta.android.domain.features.auth.interactor.ConfirmEmailUseCase
import com.elta.android.presentation.Events // Не забудь этот импорт!
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import io.reactivex.Completable
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import javax.inject.Inject

class EmailConfirmationPm @Inject constructor(
    private val confirmEmailUseCase: ConfirmEmailUseCase,
    private val checkTokenOwnerUseCase: CheckTokenOwnerUseCase,
    services: ServiceFacade
) : BasePm(services) {
    // GetFeatureConfigUseCase здесь больше не нужен, так как мы идем на Onboarding

    override val isEmptyScreen: Boolean = true

    val loginWithAnotherAccountAction = action<Unit>()
    val continueAction = action<Unit>()
    val contentVisibilityCommand = command<Boolean>(bufferSize = 1)
    val confirmEmailAction = action<Unit>()

    private val token = state<String>()

    @Suppress("LongMethod")
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
            .retry() // Обрати внимание: retry() может скрывать ошибки, если они не обработаны выше
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

    fun setToken(token: String) {
        this.token.consumer.accept(token)
    }

    override fun handleError(error: Throwable) {
        if (error is EmailLinkInvalid)
            router.newRootChain(Screens.GreetingFlow, Screens.AuthFlow)
        else
            super.handleError(error)
    }

    // ИСПРАВЛЕННЫЙ МЕТОД
    private fun handleSuccess(isOwner: Boolean) {
        if (isOwner) {
            // 1. ВАЖНО: Сообщаем AppPm, что почта подтверждена.
            // Это уберет красные плашки, когда юзер в итоге попадет на главный экран.
            bus.post(Events.ProfileDataChanged)

            // 2. ОТПРАВЛЯЕМ НА ОНБОРДИНГ
            // Так как профиль пустой, юзер должен его заполнить.
            // Это предотвратит краш HomeFlowPm (NoSuchElementException).
            router.newRootFlow(Screens.OnBoardingFlow)
        } else {
            contentVisibilityCommand.consumer.accept(true)
        }
    }

    private fun createConfirmRequestParams(i: Unit): ConfirmEmailUseCase.Params =
        ConfirmEmailUseCase.Params(token.value)

    private inline fun Completable.hideContent(): Completable =
        this.doOnSubscribe { contentVisibilityCommand.consumer.accept(false) }
}