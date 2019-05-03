package com.elta.android.presentation.features.profile.settings.gender.pm

import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.features.profile.settings.gender.model.GenderModel
import com.nullgr.core.date.toTimestamp
import io.reactivex.Observable
import me.dmdev.rxpm.widget.checkControl
import me.dmdev.rxpm.widget.dialogControl
import java.util.Date
import javax.inject.Inject

class ProfileSetGenderPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    serviceFacade: ServiceFacade
) : BasePm(serviceFacade) {

    val continueAction = Action<Unit>()
    val backHandleAction = Action<Unit>()
    val saveChangesEnableState = State(false)
    val checkNotSpecified = checkControl()
    val checkMale = checkControl()
    val checkFemale = checkControl()
    val exitDialogControl = dialogControl<DialogData, DialogResult>()

    private val profileGenderState = State<GenderModel>()
    private val changedProfileGenderState = State<GenderModel>()
    private val isGenderChangedState = State(false)
    private val getProfileAction = Action<Unit>()
    private val exitDialogAction = Action<Unit>()

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
                    .doOnSuccess(::handleProfile)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
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

        Observable.merge(
            checkNotSpecified.checkedChanges.observable.filter { it }.map { GenderModel.NOT_SPECIFIED },
            checkMale.checkedChanges.observable.filter { it }.map { GenderModel.MALE },
            checkFemale.checkedChanges.observable.filter { it }.map { GenderModel.FEMALE }
        )
            .doOnNext(changedProfileGenderState.consumer)
            .doOnNext(::updateGenderChangedState)
            .map { isGenderChangedState.value }
            .subscribe(saveChangesEnableState.consumer)
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(getProfileAction.consumer)
            .untilDestroy()
    }

    private fun updateGenderChangedState(genderModel: GenderModel) {
        isGenderChangedState.consumer.accept(
            profileGenderState.value != genderModel &&
                genderModel != GenderModel.NOT_SPECIFIED
        )
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

    private fun handleProfile(profile: Profile) {
        when (profile.gender) {
            Gender.MALE -> checkMale.checked.consumer.accept(true)
            Gender.FEMALE -> checkFemale.checked.consumer.accept(true)
            else -> checkNotSpecified.checked.consumer.accept(true)
        }
        profileGenderState.consumer.accept(
            profile.gender?.let { GenderModel.valueOf(it.name) } ?: GenderModel.NOT_SPECIFIED
        )
    }

    private fun createUpdateProfileUseCase(i: Unit): UpdateProfileUseCase.Params {
        val profileGender = when (changedProfileGenderState.value) {
            GenderModel.MALE -> Gender.MALE
            GenderModel.FEMALE -> Gender.FEMALE
            else -> null
        }
        return UpdateProfileUseCase.Params(
            Profile(
                gender = profileGender,
                timeStamp = Date().toTimestamp()
            )
        )
    }

    private fun handleBack(i: Unit) {
        when (isGenderChangedState.value) {
            true -> exitDialogAction.consumer.accept(Unit)
            else -> router.exit()
        }
    }

    private fun handleSuccess() {
        bus.event(Events.ProfileDataChanged)
        router.exit()
    }

    enum class DialogResult {
        NEGATIVE, POSITIVE
    }
}