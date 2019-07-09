package com.elta.android.presentation.features.profile.settings.name.pm

import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.interactor.isNameValid
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.profile.settings.name.model.PersonNameModel
import com.nullgr.core.date.toTimestamp
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import java.util.Date
import javax.inject.Inject

class ProfileSetNamePm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    serviceFacade: ServiceFacade
) : BasePm(serviceFacade) {

    val continueAction = Action<Unit>()
    val backHandleAction = Action<Unit>()
    val saveChangesEnableState = State(false)
    val firstNameInput = inputControl(hideErrorOnUserInput = false)
    val secondNameInput = inputControl(hideErrorOnUserInput = false)
    val exitDialogControl = dialogControl<DialogData, DialogResult>()

    private val exitDialogAction = Action<Unit>()
    private val getProfileAction = Action<Unit>()
    private val isNameNotEmptyState = State(false)
    private val isNameChangedState = State(false)
    private val changedFullNameSate = State(PersonNameModel())
    private val originalFullNameState = State(PersonNameModel())
    private val profileState = State<Profile>()

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
            .map { isNameValid(it.firstName, it.secondName) && isNameChangedState.value }
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
        originalFullNameState.consumer.accept(
            PersonNameModel(
                firstName = profile.firstName,
                secondName = profile.secondName
            )
        )
        firstNameInput.text.consumer.accept(profile.firstName ?: "")
        secondNameInput.text.consumer.accept(profile.secondName ?: "")
    }

    private fun handleSuccess() {
        hideKeyBoardCommand.consumer.accept(Unit)
        bus.event(Events.ProfileDataChanged)
        router.exit()
    }

    private fun checkIsEmpty(name: PersonNameModel) {
        isNameNotEmptyState.consumer.accept(
            !name.firstName.isNullOrEmpty() ||
                !name.secondName.isNullOrEmpty()
        )
    }

    private fun checkIsChanged(name: PersonNameModel) {
        val profileName = originalFullNameState.value
        isNameChangedState.consumer.accept(
            profileName.firstName != name.firstName ||
                profileName.secondName != name.secondName
        )
    }
}