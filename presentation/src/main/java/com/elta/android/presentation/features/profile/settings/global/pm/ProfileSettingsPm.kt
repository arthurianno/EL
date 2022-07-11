package com.elta.android.presentation.features.profile.settings.global.pm

import com.elta.android.domain.features.auth.interactor.DeleteProfileUseCase
import com.elta.android.domain.features.auth.interactor.LinkSocialNetworkUseCase
import com.elta.android.domain.features.auth.interactor.UnLinkSocialNetworkUseCase
import com.elta.android.domain.features.googlefit.interactor.CheckGoogleFitAuthUseCase
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.interactor.googleFitApp
import com.elta.android.domain.features.user.model.HealthAppType
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem.Type
import com.elta.android.presentation.features.profile.settings.global.ui.builder.ProfileSettingsItemsBuilder
import io.reactivex.Observable
import io.reactivex.Single
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class ProfileSettingsPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val linkSocialNetworkUseCase: LinkSocialNetworkUseCase,
    private val unlinkSocialNetworkUseCase: UnLinkSocialNetworkUseCase,
    private val checkGoogleFitAuthUseCase: CheckGoogleFitAuthUseCase,
    private val itemsBuilder: ProfileSettingsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    val unlinkNetworkDialogControl = dialogControl<DialogData, DialogResult>()
    val googleFitActivatedDialogControl = dialogControl<DialogData, DialogResult>()
    val profileDeleteDialogControl = dialogControl<DialogData, DialogResult>()
    val openPrivacyPolicyCommand = command<Unit>(bufferSize = 1)

    private val socialNetworkState = state<SocialNetworkType>()
    private val getProfileSettingsAction = action<Unit>()
    private val linkSocialUserAction = action<Unit>()
    private val unlinkSocialUserAction = action<Unit>()
    private val profileState = state<Profile>()
    private val checkGoogleFitAuthAction = action<Unit>()

    private val unlinkNetworkDialogData: DialogData by lazy { Dialogs.EventUnlinkNetwork(resources) }
    private val googleFitActivatedDialogData: DialogData by lazy {
        Dialogs.GoogleFitActivated(
            resources
        )
    }
    private val profileDeleteDialogData: DialogData by lazy { Dialogs.DeleteProfile(resources) }

    override fun onCreate() {
        super.onCreate()
        observeClicks()
        observeNetworksActions()
        observeGoogleFitAction()

        getProfileSettingsAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                getProfileUseCase.execute()
                    .bindProgress()
                    .handleProfileUseCase()
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observable.merge(
            lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit },
            bus.events<Events.ProfileDataChanged>().map { Unit }
        )
            .subscribe(getProfileSettingsAction.consumer)
            .untilDestroy()
    }

    private fun observeClicks() {
        bus.clicks<Clicks.ProfileSettingsItemClicked>()
            .map { it.type }
            .doOnNext { type ->
                when (type) {
                    Type.NAME -> router.navigateTo(Screens.SetName)
                    Type.GENDER -> router.navigateTo(Screens.SetGender)
                    Type.PASSWORD -> router.navigateTo(Screens.ChangePassword)
                    Type.LEGAL_INFO -> openPrivacyPolicyCommand.consumer.accept(Unit)
                    Type.NOTIFICATION -> router.startFlow(Screens.Reminders)
                    Type.DELETE_PROFILE -> deleteProfile()
                    Type.APP_VERSION -> {} // TODO click by app version
                    else -> throw IllegalArgumentException("This type:$type haven`t implemented yet...")
                }
            }
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.ProfileSettingsSocialItemClicked>()
            .map { it.item }
            .doOnNext { socialNetworkState.consumer.accept(it.type) }
            .subscribe {
                if (it.isLinked) unlinkSocialUserAction.consumer.accept(Unit)
                else linkSocialUserAction.consumer.accept(Unit)
            }
            .untilDestroy()

        bus.clicks<Clicks.ProfileSettingsHealthAppItemClicked>()
            .map { it.type }
            .map(::createSwitchHealthAppParams)
            .flatMapSingle {
                updateProfileUseCase.execute(it)
                    .andThen(getProfileUseCase.execute())
                    .bindProgress()
                    .handleProfileUseCase()
                    .doOnSuccess { checkGoogleFitAuthAction.consumer.accept(Unit) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun deleteProfile() {
        profileDeleteDialogControl.showForResult(profileDeleteDialogData)
            .filter { it == DialogResult.POSITIVE }
            .subscribe {
                deleteProfileUseCase.execute()
                    .bindProgress()
                    .doOnError(::handleError)
                    .subscribe {
                        router.newRootFlow(Screens.AuthFlow)
                    }
            }
            .untilDestroy()
    }

    private fun observeGoogleFitAction() {
        checkGoogleFitAuthAction.observable
            .map { profileState.value }
            .filter { it.googleFitApp()?.isActive ?: false }
            .flatMap {
                checkGoogleFitAuthUseCase.execute()
                    .doOnNext(::showGoogleFitEnabledDialog)
                    .doOnError(::handleError)
            }
            .subscribe()
            .untilDestroy()
    }

    private fun showGoogleFitEnabledDialog(isEnabled: Boolean) {
        if (isEnabled) {
            googleFitActivatedDialogControl.show(googleFitActivatedDialogData)
        }
    }

    private fun observeNetworksActions() {
        linkSocialUserAction.observable
            .map { socialNetworkState.value }
            .map(::createLinkSocialUserParams)
            .flatMapCompletable { params ->
                linkSocialNetworkUseCase.execute(params)
                    .doOnComplete { getProfileSettingsAction.consumer.accept(Unit) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        unlinkSocialUserAction.observable
            .switchMapMaybe {
                unlinkNetworkDialogControl.showForResult(unlinkNetworkDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
            .map { socialNetworkState.value }
            .map(::createUnlinkSocialUserParams)
            .flatMapCompletable { params ->
                unlinkSocialNetworkUseCase.execute(params)
                    .doOnComplete { getProfileSettingsAction.consumer.accept(Unit) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun Single<Profile>.handleProfileUseCase() =
        doOnSuccess(profileState.consumer)
            .map { itemsBuilder.buildItems(it) }
            .doOnSuccess { items.consumer.accept(it) }

    private fun createLinkSocialUserParams(network: SocialNetworkType) =
        LinkSocialNetworkUseCase.Params(network)

    private fun createUnlinkSocialUserParams(network: SocialNetworkType) =
        UnLinkSocialNetworkUseCase.Params(network)

    private fun createSwitchHealthAppParams(type: HealthAppType): UpdateProfileUseCase.Params =
        UpdateProfileUseCase.Params(
            profileState.value.copy().apply {
                val healthApp = healthApps?.find { it.type == type }
                healthApp?.isActive = healthApp?.isActive?.not() ?: false
            }
        )
}
