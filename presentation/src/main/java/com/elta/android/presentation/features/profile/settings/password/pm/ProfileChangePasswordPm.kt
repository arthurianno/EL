package com.elta.android.presentation.features.profile.settings.password.pm

import android.content.Context
import android.util.Log
import coil.imageLoader
import com.elta.android.domain.features.auth.interactor.ChangePasswordUseCase
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.messages.SnackBarMessageData
import com.elta.android.presentation.utils.cacheHelper.ImageCacheHelper
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class ProfileChangePasswordPm @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    serviceFacade: ServiceFacade,
    private val getScreenFromCacheUseCase: GetScreenConfigFromCache,
    private val context: Context
) : BasePm(serviceFacade) {

    val oldPasswordInput = inputControl(hideErrorOnUserInput = false)
    val newPasswordInput = inputControl(hideErrorOnUserInput = false)
    val exitDialogControl = dialogControl<DialogData, DialogResult>()
    val changePasswordEnabledState = state(false)
    val continueAction = action<Unit>()
    val backHandleAction = action<Unit>()

    private val exitDialogAction = action<Unit>()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val exitDialogData by lazy { Dialogs.ExitAndLoseData(resources) }

    override fun onCreate() {
        super.onCreate()
        loadConfigScreen()
        bindHandleBack()

        Observables.combineLatest(
            oldPasswordInput.text.observable,
            newPasswordInput.text.observable
        )
            .doOnNext(::getPasswordsError)
            .map { oldAndNewPasswords ->
                oldAndNewPasswords.toList().all { password ->
                    validatePassword(password) && password.isNotBlank()
                } && oldAndNewPasswords.first != oldAndNewPasswords.second
            }
            .subscribe(changePasswordEnabledState.consumer)
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .doOnNext {
                hideKeyBoardCommand.consumer.accept(Unit)
            }
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


    fun loadConfigScreen(){
        coroutineScope.launch {
            when (val result = getScreenFromCacheUseCase("new-password-screen")) {
                is Resource.Success -> {
                    val screenEntity = result.data
                    screenConfigState.consumer.accept(screenEntity)
                    val imageUrl = screenEntity.backgroundImageUrl
                    val imageLoader = context.imageLoader
                    if (imageUrl != null ){
                        val isInCache = ImageCacheHelper.isImageInCache(imageUrl, context,imageLoader)
                        when(isInCache){
                            true -> imagePreloadState.consumer.accept(true)
                            false -> imagePreloadState.consumer.accept(false)
                        }
                    }else {
                        imagePreloadState.consumer.accept(false)
                    }
                }
                is Resource.Error -> {
                    Log.e("GreetingPm", "Error loading screen config: ${result.message}")
                    imagePreloadState.consumer.accept(false)
                }
                is Resource.Loading -> {
                    Log.d("GreetingPm", "Loading screen config...")
                }
            }
        }
    }

    private fun isPasswordsFilled() =
        oldPasswordInput.text.value.isNotBlank() && newPasswordInput.text.value.isNotBlank()

    private fun isPasswordSame() =
        oldPasswordInput.text.valueOrNull == newPasswordInput.text.valueOrNull

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
        showSnackBar(
            SnackBarMessageData.SimpleTextMessage(
                resources.getString(R.string.profile_settings_change_password_changed)
            )
        )
        router.exit()
    }

    private fun createParams(i: Unit): ChangePasswordUseCase.Params =
        ChangePasswordUseCase.Params(oldPasswordInput.text.value, newPasswordInput.text.value)

    private fun validatePassword(password: String): Boolean =
        password.isEmpty() || isPasswordValid(password)

    private fun getPasswordsError(oldAndNewPasswords: Pair<String, String>) {
        oldPasswordInput.error.consumer.accept(
            if (!validatePassword(oldAndNewPasswords.first)) {
                resources.getString(R.string.registration_password_pattern)
            } else {
                ""
            }
        )
        newPasswordInput.error.consumer.accept(
            when {
                !validatePassword(oldAndNewPasswords.second) -> resources.getString(R.string.registration_password_pattern)
                isPasswordSame() && isPasswordsFilled() -> resources.getString(R.string.registration_password_the_same)
                else -> ""
            }
        )
    }
}
