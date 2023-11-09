package com.elta.android.presentation.features.profile.settings.name.pm

import com.elta.android.domain.features.user.hasWrongChars
import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.interactor.isNameValid
import com.elta.android.domain.features.user.isTooLong
import com.elta.android.domain.features.user.isTooShort
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.profile.settings.name.model.PersonNameModel
import com.nullgr.core.date.toTimestamp
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import java.util.Date
import javax.inject.Inject

class ProfileSetNamePm @Inject constructor(
    private val getProfileUseCase: GetUpdatedProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    serviceFacade: ServiceFacade
) : BasePm(serviceFacade) {

    val continueAction = action<Unit>()
    val backHandleAction = action<Unit>()
    val saveChangesEnableState = state(false)
    val firstNameInput = inputControl(hideErrorOnUserInput = false)
    val secondNameInput = inputControl(hideErrorOnUserInput = false)
    val exitDialogControl = dialogControl<DialogData, DialogResult>()

    private val exitDialogAction = action<Unit>()
    private val getProfileAction = action<Unit>()
    private val isNameNotEmptyState = state(false)
    private val isNameChangedState = state(false)
    private val isFirstNameChangedState = state(false)
    private val isSecondNameChangedState = state(false)
    private val changedFullNameState = state(PersonNameModel(firstName = "", secondName = ""))
    private val originalFullNameState = state(PersonNameModel(firstName = "", secondName = ""))
    private val profileState = state<Profile>()

    private val exitDialogData: DialogData by lazy { Dialogs.ExitAndLoseData(resources) }

    override fun onCreate() {
        super.onCreate()
        bindHandleBack()

        getProfileAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                getProfileUseCase.execute()
                    .bindProgress()
                    .hideErrorContainer()
                    .doOnSuccess(profileState.consumer)
                    .doOnSuccess(::handleProfile)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observables.combineLatest(
            firstNameInput.text.observable,
            secondNameInput.text.observable
        ) { firstName, secondName ->
            PersonNameModel(firstName, secondName)
        }
            .doOnNext(changedFullNameState.consumer)
            .doOnNext(::checkIsEmpty)
            .doOnNext(::checkIsChanged)
            .doOnNext(::checkIsValid)
            .map { personalName ->
                isNameValid(personalName.firstName) &&
                        isNameValid(personalName.secondName) &&
                        isNameChangedState.value
            }
            .subscribe(saveChangesEnableState.consumer)
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .map(::createUpdateProfileUseCase)
            .flatMapCompletable {
                updateProfileUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete(::handleSuccess)
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

    private fun bindHandleBack() {
        exitDialogAction.observable
            .switchMapMaybe {
                exitDialogControl.showForResult(exitDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
            .doOnNext { router.exit() }
            .subscribe()
            .untilDestroy()

        backHandleAction.observable
            .doOnNext(::handleBack)
            .subscribe()
            .untilDestroy()
    }

    private fun handleBack(i: Unit) {
        if (isNameNotEmptyState.value && isNameChangedState.value) {
            exitDialogAction.consumer.accept(Unit)
        } else {
            router.exit()
        }
    }

    private fun createUpdateProfileUseCase(i: Unit) = UpdateProfileUseCase.Params(
        profileState.value.copy(
            firstName = changedFullNameState.value.firstName,
            secondName = changedFullNameState.value.secondName,
            timeStamp = Date().toTimestamp()
        )
    )

    private fun handleProfile(profile: Profile) {
        val firstName = profile.firstName.orEmpty()
        val secondName = profile.secondName.orEmpty()
        originalFullNameState.consumer.accept(
            PersonNameModel(
                firstName = firstName,
                secondName = secondName
            )
        )
        firstNameInput.text.consumer.accept(firstName)
        secondNameInput.text.consumer.accept(secondName)
    }

    private fun handleSuccess() {
        hideKeyBoardCommand.consumer.accept(Unit)
        bus.event(Events.ProfileDataChanged)
        router.exit()
    }

    private fun checkIsEmpty(name: PersonNameModel) {
        isNameNotEmptyState.consumer.accept(
            name.firstName.isNotEmpty() || name.secondName.isNotEmpty()
        )
    }

    private fun checkIsChanged(name: PersonNameModel) {
        val profileName = originalFullNameState.value
        isFirstNameChangedState.consumer.accept(profileName.firstName != name.firstName)
        isSecondNameChangedState.consumer.accept(profileName.secondName != name.secondName)
        isNameChangedState.consumer.accept(isFirstNameChangedState.value || isSecondNameChangedState.value)
    }

    private fun checkIsValid(name: PersonNameModel) {
        if (profileState.hasValue() && isNameChangedState.value) {
            if (isFirstNameChangedState.value) {
                firstNameInput.error.consumer.accept(getFirstNameErrorString(name.firstName))
            }
            if (isSecondNameChangedState.value) {
                secondNameInput.error.consumer.accept(getSecondNameErrorString(name.secondName))
            }
        }
    }

    private fun getFirstNameErrorString(name: String): String = when {
        name.hasWrongChars() -> services.resources.getString(R.string.profile_first_name_invalid_characters_error)
        name.isTooShort() -> services.resources.getString(R.string.profile_first_name_min_length_error)
        name.isTooLong() -> services.resources.getString(R.string.profile_name_max_length_error)
        else -> ""
    }

    private fun getSecondNameErrorString(name: String): String = when {
        name.hasWrongChars() -> services.resources.getString(R.string.profile_second_name_invalid_characters_error)
        name.isTooShort() -> services.resources.getString(R.string.profile_second_name_min_length_error)
        name.isTooLong() -> services.resources.getString(R.string.profile_name_max_length_error)
        else -> ""
    }
}
