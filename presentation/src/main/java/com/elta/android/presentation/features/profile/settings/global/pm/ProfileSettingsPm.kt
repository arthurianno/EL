package com.elta.android.presentation.features.profile.settings.global.pm

import android.util.Log
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.domain.features.appsettings.interactor.ChangeBackendVariantUseCase
import com.elta.android.domain.features.appsettings.interactor.GetBackendVariantUseCase
import com.elta.android.domain.features.auth.interactor.DeleteProfileUseCase
import com.elta.android.domain.features.auth.interactor.LinkSocialNetworkUseCase
import com.elta.android.domain.features.auth.interactor.LogOutUseCase
import com.elta.android.domain.features.auth.interactor.UnLinkSocialNetworkUseCase
import com.elta.android.domain.features.emias.interactor.GetEmiasStatusUseCase
import com.elta.android.domain.features.firebase.interactor.TokenUseCase
import com.elta.android.domain.features.googlefit.interactor.CheckGoogleFitAuthUseCase
import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.model.HealthApp
import com.elta.android.domain.features.user.model.HealthAppType
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.getMetricAttributes
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.profile.settings.emias.viewmodel.EMIAS_TIMEOUT
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem.Type
import com.elta.android.presentation.features.profile.settings.global.ui.builder.ProfileSettingsItemsBuilder
import com.elta.android.presentation.utils.OneSignalTags
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.SingletonRxBusProvider
import io.reactivex.Observable
import io.reactivex.Single
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import org.threeten.bp.LocalDate
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject

private const val NAV_TRACE_TAG = "NavTrace"

class ProfileSettingsPm @Inject constructor(
    private val tokenUseCase: TokenUseCase,
    private val getProfileUseCase: GetUpdatedProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val linkSocialNetworkUseCase: LinkSocialNetworkUseCase,
    private val unlinkSocialNetworkUseCase: UnLinkSocialNetworkUseCase,
    private val checkGoogleFitAuthUseCase: CheckGoogleFitAuthUseCase,
    private val getBackendVariantUseCase: GetBackendVariantUseCase,
    private val changeBackendVariantUseCase: ChangeBackendVariantUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val itemsBuilder: ProfileSettingsItemsBuilder,
    private val getEmiasStatus: GetEmiasStatusUseCase,
    private val appMetric: AppMetricTracker,
    services: ServiceFacade
) : BaseListPm(services) {

    val unlinkNetworkDialogControl = dialogControl<DialogData, DialogResult>()
    val googleFitActivatedDialogControl = dialogControl<DialogData, DialogResult>()
    val profileDeleteDialogControl = dialogControl<DialogData, DialogResult>()
    val emiasErrorDialogControl = dialogControl<DialogData, DialogResult>()
    val datePickerExitDialogControl = dialogControl<DialogData, DialogResult>()
    val openPrivacyPolicyCommand = command<Unit>(bufferSize = 1)
    val copyTokenCommand = command<String>()
    val downloadGoogleFitCommand = command<Unit>()
    val openGoogleFitCommand = command<Unit>()

    private val socialNetworkState = state<SocialNetworkType>()
    private val getProfileSettingsAction = action<Unit>()
    private val linkSocialUserAction = action<Unit>()
    private val unlinkSocialUserAction = action<Unit>()
    private val profileState = state<Profile>()
    private val logoutAction = action<Unit>()
    val datePickerCloseAction = action<LocalDate>()

    private val unlinkNetworkDialogData: DialogData by lazy { Dialogs.EventUnlinkNetwork(resources) }

    val showDatePickerDialog = command<LocalDate>(bufferSize = 1)
    val dateTimeSelectedAction = action<LocalDate>()

    private val googleFitActivatedDialogData: DialogData by lazy {
        Dialogs.GoogleFitActivated(
            resources
        )
    }
    private val datePickerExitDialogData: DialogData by lazy {
        Dialogs.DataPickerExit(
            resources
        )
    }
    private val profileDeleteDialogData: DialogData by lazy { Dialogs.DeleteProfile(resources) }
    private val emiasErrorDialogData: DialogData by lazy {
        Dialogs.Emias.ReceivingDataError(
            resources
        )
    }
    private val networkConnectionErrorDialogData: DialogData by lazy {
        Dialogs.Emias.NoInternetConnection(
            resources
        )
    }
    private val previousDay = LocalDate.now().minusDays(1)

    override fun onCreate() {
        super.onCreate()
        appMetric.trackEvent(AppMetricEvent.SettingsScreen)
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

        logoutAction.observable
            .flatMapCompletable {
                logOutUseCase.execute()
                    .doOnComplete { OneSignalTags.logout() }
            }
            .doOnError(::handleError)
            .subscribe()
            .untilDestroy()

        Observable.merge(
            lifecycleObservable.filter { it == Lifecycle.CREATED }.map { },
            bus.events<Events.ProfileDataChanged>().map { }
        )
            .subscribe(getProfileSettingsAction.consumer)
            .untilDestroy()
        dateTimeSelectedAction.observable
            .map { UpdateProfileUseCase.Params(profileState.value.copy(birthDate = it)) }
            .flatMapCompletable { params ->
                updateProfileUseCase.execute(params)
                    .doOnComplete { bus.event(Events.ProfileDataChanged) }
                    .doOnComplete { appMetric.setProfileAttributes(params.profile.getMetricAttributes()) }
                    .doOnError(::handleError)
            }
            .subscribe()
            .untilDestroy()
        datePickerCloseAction.observable
            .doOnNext(::handleClosePicker)
            .subscribe()
            .untilDestroy()
    }

    private fun handleClosePicker(selectedDate: LocalDate) {
        if (selectedDate != previousDay ||
            (profileState.value.birthDate != null && selectedDate != profileState.value.birthDate)
        ) {
            datePickerExitDialogControl.showForResult(datePickerExitDialogData)
                .filter { it == DialogResult.POSITIVE }
                .subscribe { dateTimeSelectedAction.consumer.accept(selectedDate) }
                .untilDestroy()
        }
    }

    private fun observeClicks() {
        bus.clicks<Clicks.ProfileSettingsItemClicked>()
            .map { it.type }
            .doOnNext { type -> Log.i(NAV_TRACE_TAG, "ProfileSettingsPm.ProfileSettingsItemClicked(type=$type)") }
            .doOnNext { type ->
                when (type) {
                    Type.NAME -> router.navigateTo(Screens.SetName)
                    Type.GENDER -> router.navigateTo(Screens.SetGender)
                    Type.PASSWORD -> router.navigateTo(Screens.ChangePassword)
                    Type.LEGAL_INFO -> openPrivacyPolicyCommand.consumer.accept(Unit)
                    Type.EMIAS_ACCOUNT -> handleEmiasStatus()
                    Type.NOTIFICATION -> router.startFlow(Screens.Reminders)

                    Type.LANGUAGE -> {
                        if (BuildConfig.SHOW_LANGUAGE_SELECTION) {
                            router.navigateTo(Screens.LanguageSelection(isFirstLaunch = false))
                        }
                    }
                    Type.GLUCOSE_FORMAT -> router.navigateTo(Screens.GlucoseFormat)
                    Type.DELETE_PROFILE -> deleteProfile()
                    Type.TOKEN -> copyToken()
                    Type.BIRTH_DATE, Type.BIRTH_DATE_PLACEHOLDER -> showDataPicker()
                    Type.APP_VERSION, Type.EMAIL -> {}
                }
            }
            .doOnError { error ->
                Log.e(NAV_TRACE_TAG, "ProfileSettingsPm.ProfileSettingsItemClicked stream error", error)
                handleError(error)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.ChangeBackendVariant>()
            .map(::createBackendVariantParams)
            .flatMapCompletable {
                changeBackendVariantUseCase.execute(it)
                    .doOnComplete { logoutAction.consumer.accept(Unit) }
            }
            .doOnError { error ->
                Log.e(NAV_TRACE_TAG, "ProfileSettingsPm.ChangeBackendVariant stream error", error)
                handleError(error)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observable.merge(
            SingletonRxBusProvider.BUS.observable(RxBus.Keys.SINGLE)
                .filter { it is GoogleFitAuthResult.Access }
                .map { HealthAppType.GOOGLE_FIT },
            bus.clicks<Clicks.ProfileSettingsHealthAppItemClicked>()
                .map { it.type }
        )
            .doOnNext { type -> Log.i(NAV_TRACE_TAG, "ProfileSettingsPm.HealthAppClicked(type=$type)") }
            .flatMapSingle { type ->
                checkGoogleFitAuthUseCase.execute()
                    .doOnError(::handleError)
                    .map { googleFitAuthResult ->
                        createSwitchHealthAppParams(
                            type,
                            googleFitAuthResult
                        ) to googleFitAuthResult
                    }
                    .doOnSuccess { (params, googleFitAuthResult) ->
                        val isActive =
                            params.profile.healthApps?.first { it.type == type }?.isActive ?: false
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
            .doOnError { error ->
                Log.e(NAV_TRACE_TAG, "ProfileSettingsPm.HealthApp stream error", error)
                handleError(error)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun handleEmiasStatus() {
        getEmiasStatus.execute()
            .timeout(EMIAS_TIMEOUT, TimeUnit.MILLISECONDS)
            .onErrorResumeNext { error ->
                val emiasDialog =
                    if (error is NetworkConnectionError) networkConnectionErrorDialogData
                    else emiasErrorDialogData

                emiasErrorDialogControl.showForResult(emiasDialog)
                    .flatMapSingle { Single.error(error) }

            }
            .doOnError { Timber.e(it) }
            .doOnSuccess {
                appMetric.trackEvent(AppMetricEvent.EmiasClick)
                router.navigateTo(
                    Screens.EmiasProfile(
                        linkedStatus = it.first,
                        emias = it.second,
                        birthDateFromProfile = profileState.value.birthDate
                    )
                )
            }
            .subscribe()
            .untilDestroy()
    }

    private fun createBackendVariantParams(backendVariant: Clicks.ChangeBackendVariant) =
        ChangeBackendVariantUseCase.Params(backendVariant.type)

    private fun copyToken() {
        tokenUseCase()
            .subscribe(copyTokenCommand.consumer)
            .untilDestroy()
    }

    private fun showDataPicker() {
        val date = profileState.value.birthDate ?: previousDay
        showDatePickerDialog.consumer.accept(date)
    }

    private fun deleteProfile() {
        profileDeleteDialogControl.showForResult(profileDeleteDialogData)
            .filter { it == DialogResult.POSITIVE }
            .doOnSubscribe { appMetric.trackEvent(AppMetricEvent.SettingDeleteProfileClick) }
            .flatMapCompletable {
                deleteProfileUseCase.execute()
                    .bindProgress()
                    .timeout(EMIAS_TIMEOUT, TimeUnit.MILLISECONDS)
                    .doOnError {
                        when (it) {
                            is NetworkConnectionError, is TimeoutException -> bus.event(Events.NetworkProblemTryLater)
                            else -> handleError(it)
                        }
                    }
                    .doOnComplete {
                        appMetric.trackEvent(AppMetricEvent.DeleteProfileAlertClick)
                    }
                    .doOnComplete {
                        router.newRootFlow(Screens.AuthFlow)
                    }
            }
            .subscribe()
            .untilDestroy()
    }

    private fun showGoogleFitEnabledDialog(
        googleFitAuthResult: GoogleFitAuthResult,
        isActive: Boolean
    ) {
        when (googleFitAuthResult) {
            GoogleFitAuthResult.Access -> if (isActive) googleFitActivatedDialogControl.show(
                googleFitActivatedDialogData
            )

            GoogleFitAuthResult.ApplicationNotInstalled -> downloadGoogleFitCommand.consumer.accept(
                Unit
            )

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
            .flatMap {
                getBackendVariantUseCase.execute()
            }
            .map { backendVariant ->
                itemsBuilder.buildItems(profileState.value, backendVariant)
            }
            .doOnSuccess { items.consumer.accept(it) }

    private fun createLinkSocialUserParams(network: SocialNetworkType) =
        LinkSocialNetworkUseCase.Params(network)

    private fun createUnlinkSocialUserParams(network: SocialNetworkType) =
        UnLinkSocialNetworkUseCase.Params(network)

    private fun createSwitchHealthAppParams(
        type: HealthAppType,
        googleFitAuthResult: GoogleFitAuthResult
    ): UpdateProfileUseCase.Params {
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
