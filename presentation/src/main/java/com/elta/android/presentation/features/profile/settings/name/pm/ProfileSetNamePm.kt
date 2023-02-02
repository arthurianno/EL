package com.elta.android.presentation.features.profile.settings.name.pm

import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.interactor.MAX_NAME_LENGTH
import com.elta.android.domain.features.user.interactor.MIN_NAME_LENGTH
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.interactor.isNameValid
import com.elta.android.domain.features.user.interactor.isNameWithValidCharacters
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
    private val getProfileUseCase: GetProfileUseCase,
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
    private val changedFullNameSate = state(PersonNameModel(firstName = "", secondName = ""))
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
            .doOnNext(changedFullNameSate.consumer)
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
        when (isNameNotEmptyState.value && isNameChangedState.value) {
            true -> exitDialogAction.consumer.accept(Unit)
            else -> router.exit()
        }
    }

    private fun createUpdateProfileUseCase(i: Unit) = UpdateProfileUseCase.Params(
        profileState.value.copy(
            firstName = changedFullNameSate.value.firstName,
            secondName = changedFullNameSate.value.secondName,
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
            name.firstName.isNotEmpty() ||
                name.secondName.isNotEmpty()
        )
    }

    private fun checkIsChanged(name: PersonNameModel) {
        val profileName = originalFullNameState.value
        isNameChangedState.consumer.accept(
            profileName.firstName != name.firstName ||
                profileName.secondName != name.secondName
        )
    }

    private fun checkIsValid(name: PersonNameModel) {
        firstNameInput.error.consumer.accept(getFirstNameErrorString(name.firstName))
        secondNameInput.error.consumer.accept(getSecondNameErrorString(name.secondName))
    }

    private fun getFirstNameErrorString(name: String): String = when {
        !isNameWithValidCharacters(name) -> services.resources.getString(R.string.profile_first_name_invalid_characters_error)
        else -> getNameErrorString(name)
    }

    private fun getSecondNameErrorString(name: String): String = when {
        !isNameWithValidCharacters(name) -> services.resources.getString(R.string.profile_second_name_invalid_characters_error)
        else -> getNameErrorString(name)
    }

    private fun getNameErrorString(name: String) = when {
        name.length < MIN_NAME_LENGTH -> services.resources.getString(R.string.profile_name_min_length_error)
        name.length > MAX_NAME_LENGTH -> services.resources.getString(R.string.profile_name_max_length_error)
        else -> ""
    }
}
