package com.elta.android.presentation.features.profile.settings.global.pm

import android.util.Log
import com.elta.android.domain.features.auth.interactor.DeleteProfileUseCase
import com.elta.android.domain.features.auth.interactor.LinkSocialNetworkUseCase
import com.elta.android.domain.features.auth.interactor.UnLinkSocialNetworkUseCase
import com.elta.android.domain.features.firebase.interactor.TokenUseCase
import com.elta.android.domain.features.googlefit.interactor.CheckGoogleFitAuthUseCase
import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.model.HealthApp
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
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.SingletonRxBusProvider
import io.reactivex.Observable
import io.reactivex.Single
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class ProfileSettingsPm @Inject constructor(
    private val tokenUseCase: TokenUseCase,
    private val getProfileUseCase: GetUpdatedProfileUseCase,
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
    val copyTokenCommand = command<String>()
    val downloadGoogleFitCommand = command<Unit>()
    val openGoogleFitCommand = command<Unit>()

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
                    Type.GLUCOSE_FORMAT -> router.navigateTo(Screens.GlucoseFormat)
                    Type.DELETE_PROFILE -> deleteProfile()
                    Type.TOKEN -> copyToken()
                    Type.APP_VERSION, Type.EMAIL -> {}
                    else -> Log.e(javaClass.simpleName, "This type:$type haven`t implemented yet...")
                }
            }
            .doOnError(::handleError)
            .subscribe()
            .untilDestroy()

        Observable.merge(
            SingletonRxBusProvider.BUS.observable(RxBus.Keys.SINGLE)
                .filter { it is GoogleFitAuthResult.Access }
                .map { HealthAppType.GOOGLE_FIT },
            bus.clicks<Clicks.ProfileSettingsHealthAppItemClicked>()
                .map { it.type }
        )
            .flatMapSingle { type ->
                checkGoogleFitAuthUseCase.execute()
                    .doOnError(::handleError)
                    .map { googleFitAuthResult ->
                        createSwitchHealthAppParams(type, googleFitAuthResult) to googleFitAuthResult
                    }
                    .doOnSuccess { (params, googleFitAuthResult) ->
                        val isActive = params.profile.healthApps?.first { it.type == type }?.isActive ?: false
                        showGoogleFitEnabledDialog(googleFitAuthResult, isActive)
                    }
            }
            .map { (params, _) -> params }
            .flatMapSingle {
                updateProfileUseCase.execute(it)
                    .andThen(getProfileUseCase.execute())
                    .bindProgress()
                    .handleProfileUseCase()
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun copyToken() {
        tokenUseCase()
            .subscribe(copyTokenCommand.consumer)
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

    private fun showGoogleFitEnabledDialog(googleFitAuthResult: GoogleFitAuthResult, isActive: Boolean) {
        when(googleFitAuthResult) {
            GoogleFitAuthResult.Access -> if (isActive) googleFitActivatedDialogControl.show(googleFitActivatedDialogData)
            GoogleFitAuthResult.ApplicationNotInstalled -> downloadGoogleFitCommand.consumer.accept(Unit)
            GoogleFitAuthResult.NotAccess -> openGoogleFitCommand.consumer.accept(Unit)
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

    private fun createSwitchHealthAppParams(type: HealthAppType, googleFitAuthResult: GoogleFitAuthResult): UpdateProfileUseCase.Params {
        val list = profileState.value.healthApps?.map {
            if (it.type == type) {
                it.copy(isActive = it.getActive(googleFitAuthResult))
            } else {
                it
            }
        }
        return UpdateProfileUseCase.Params(
            profileState.value.copy(
                healthApps = list
            )
        )

    }

    private fun HealthApp.getActive(googleFitAuthResult: GoogleFitAuthResult) =
        googleFitAuthResult is GoogleFitAuthResult.Access && !isActive

}
