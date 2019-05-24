package com.elta.android.presentation.features.profile.settings.password.pm

import com.elta.android.domain.features.auth.interactor.ChangePasswordUseCase
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.widget.InputControl
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class ProfileChangePasswordPm @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    serviceFacade: ServiceFacade
) : BasePm(serviceFacade) {

    val oldPasswordInput = inputControl(hideErrorOnUserInput = false)
    val newPasswordInput = inputControl(hideErrorOnUserInput = false)
    val exitDialogControl = dialogControl<DialogData, DialogResult>()
    val changePasswordEnabledState = State(false)
    val continueAction = Action<Unit>()
    val backHandleAction = Action<Unit>()

    private val isOldPasswordValidState = State(false)
    private val isNewPasswordValidState = State(false)
    private val exitDialogAction = Action<Unit>()
    private val exitDialogData by lazy { Dialogs.ExitAndLoseData(resources) }

    override fun onCreate() {
        super.onCreate()

        bindHandleBack()

        oldPasswordInput bindInputErrorTo isOldPasswordValidState
        newPasswordInput bindInputErrorTo isNewPasswordValidState

        Observables.combineLatest(
            isOldPasswordValidState.observable,
            isNewPasswordValidState.observable
        ) { isOldPasswordValid, isNewPasswordValid ->
            isOldPasswordValid && isNewPasswordValid
        }
            .map { it && isPasswordsFilled() }
            .subscribe(changePasswordEnabledState.consumer)
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .map(::createParams)
            .flatMapCompletable { params ->
                changePasswordUseCase.execute(params)
                    .bindProgress()
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun isPasswordsFilled() =
        oldPasswordInput.text.value.isNotEmpty() && newPasswordInput.text.value.isNotEmpty()

    private fun bindHandleBack() {
        exitDialogAction.observable
            .switchMapMaybe { exitDialogControl.showForResult(exitDialogData) }
            .filter { it == DialogResult.POSITIVE }
            .doOnNext { router.exit() }
            .subscribe()
            .untilDestroy()

        backHandleAction.observable
            .doOnNext(::handleBack)
            .subscribe()
            .untilDestroy()
    }

    private fun handleBack(i: Unit) =
        if (oldPasswordInput.text.value.isEmpty() && newPasswordInput.text.value.isEmpty()) {
            router.exit()
        } else {
            exitDialogAction.consumer.accept(Unit)
        }

    private fun handleSuccess() {
        hideKeyBoardCommand.consumer.accept(Unit)
        router.exit()
    }

    private fun createParams(i: Unit): ChangePasswordUseCase.Params =
        ChangePasswordUseCase.Params(oldPasswordInput.text.value, newPasswordInput.text.value)

    private infix fun InputControl.bindInputErrorTo(state: State<Boolean>) {
        text.observable
            .map(::validatePassword)
            .doOnNext(state.consumer)
            .map(::getPasswordError)
            .subscribe(error.consumer)
            .untilDestroy()
    }

    private fun validatePassword(password: String): Boolean =
        password.isEmpty() || isPasswordValid(password)

    private fun getPasswordError(isPasswordValid: Boolean): String =
        if (isPasswordValid) "" else resources.getString(R.string.registration_password_pattern)
}