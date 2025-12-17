package com.elta.android.presentation.features.auth.login.pm

import android.content.Context
import android.util.Log
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.elta.android.common.errors.ProfileIsDeletedError
import com.elta.android.common.logger.FirebaseStorage
import com.elta.android.domain.features.auth.interactor.LoginUseCase
import com.elta.android.domain.features.auth.interactor.RestoreProfileUseCase
import com.elta.android.domain.features.multiLangsConfig.interactor.GetAllScreensUseCase
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.interactor.GetUserIdUseCase
import com.elta.android.domain.features.userinfo.interactor.GetProfileSettingsUseCase
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Screens
import com.elta.android.presentation.Screens.ActivateProfile
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.getMetricAttributes
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.updateStableParam
import com.elta.android.presentation.core.pm.ScreenConfigurable
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.profile.settings.reminders.utils.RemindersManager
import com.elta.android.presentation.features.registration.main.pm.BaseRegistrationPm
import com.elta.android.presentation.utils.cacheHelper.ImageCacheHelper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxSingle
import kotlinx.coroutines.withContext
import me.dmdev.rxpm.action
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class LoginPm @Inject constructor(
    private val remindersManager: RemindersManager,
    private val loginUseCase: LoginUseCase,
    private val restoreProfileUseCase: RestoreProfileUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getProfileSettings: GetProfileSettingsUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getUserId: GetUserIdUseCase,
    private val getFeatureConfigUseCase:GetFeatureConfigUseCase,
    private val firebaseStorage: FirebaseStorage,
    private val appMetric: AppMetricTracker,
    private val getScreenConfigFromCache: GetScreenConfigFromCache,
    private val context : Context,
    services: ServiceFacade
) : BaseRegistrationPm(services,getScreenConfigFromCache,context), ScreenConfigurable {

    private val restoreAction = action<Unit>()

    override val screenConfigKey = "login-screen"
    override val getScreenConfigUseCase = getScreenConfigFromCache
    val profileRestoredDialogControl = dialogControl<DialogData, DialogResult>()
    private val profileRestoredDialogData: DialogData by lazy { Dialogs.ProfileRestored(resources) }
    private val profileIsDeletedDialogData: DialogData by lazy { Dialogs.ProfileIsDeleted(resources) }

    @Suppress("LongMethod")
    override fun onCreate() {
        super.onCreate()
        loadScreenConfig(context)
        Observables.combineLatest(
            isEmailValidState.observable,
            isPasswordValidState.observable
        )
            .map { it.first && it.second }
            .subscribe(continueEnabledState.consumer)
            .untilDestroy()

        menuAction.observable
            .trackEvent(AnalyticsEventType.PASSWORD_RECOVERY)
            .subscribe { router.navigateTo(Screens.PasswordRecovery) }
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .doOnNext {
                appMetric.trackEvent(AppMetricEvent.LoginClick)
            }
            .map(::createLoginParams)
            .flatMapCompletable { params ->
                loginUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .updateAnalyticStableParam()
                    .trackEvent(AnalyticsEventType.LOG_IN)
                    .setAppMetricAttribute()
                    .flatMapCompletable(::checkEmailAndOnBoarding)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        restoreAction.observable
            .skipWhileInProgress()
            .map(::createRestoreParams)
            .flatMapSingle {
                restoreProfileUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnError(::handleError)
            }
            .flatMapCompletable { isOnboarding ->
                profileRestoredDialogControl.showForResult(profileRestoredDialogData)
                    .filter { it == DialogResult.POSITIVE }
                    .flatMapCompletable { checkEmailAndOnBoarding(isOnboarding) }
            }
            .doOnError(::handleError)
            .retry()
            .subscribe()
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        when(error){
            is ProfileIsDeletedError -> profileIsDeleted()
            else -> super.handleError(error)
        }
    }

    private fun checkEmailAndOnBoarding(isEmailConfirmed: Boolean): Completable =
        if (isEmailConfirmed) {
            handleEmailConfirmed()
        } else {
            Completable.fromAction { router.navigateTo(ActivateProfile) }
        }

    private fun handleEmailConfirmed(): Completable {
        return getUserInfoUseCase.execute()
            .flatMap { userInfo ->
                getProfileSettings.execute()
                    .map { profileSettings ->
                        (userInfo.isEmailConfirmed ?: false) to profileSettings.isOnboarded
                    }
            }
            .flatMapCompletable { (isEmailConfirmed, isOnboarded) ->
                updateUserEmail()
                    .andThen(navigateToScreen(isEmailConfirmed, isOnboarded))
            }
    }

    private fun updateUserEmail(): Completable =
        getUserId.execute()
            .doOnSuccess { firebaseStorage.userLogin = it }
            .ignoreElement()

    private fun navigateToScreen(
        isEmailConfirmed: Boolean,
        isOnboarded: Boolean
    ) = Completable.fromAction {
        when {
            !isEmailConfirmed -> router.navigateTo(ActivateProfile)
            isOnboarded -> {
                // fixme Variant A : improved_enabling_location
                val improvedEnablingLocation = getFeatureConfigUseCase.invoke().improvedEnablingLocation
                val screen = if (improvedEnablingLocation) Screens.HomeFlow
                else Screens.HomeFlowVariantA

                remindersManager.scheduleReminders()
                router.newRootFlow(screen)
            }

            else -> router.newRootFlow(Screens.OnBoardingFlow)
        }
    }

    private fun profileIsDeleted() {
        profileIsDeletedDialogControl.showForResult(profileIsDeletedDialogData)
            .filter { it == DialogResult.POSITIVE }
            .doOnSuccess {
                restoreAction.consumer.accept(Unit)
            }
            .subscribe()
            .untilDestroy()
    }

    private fun createLoginParams(i: Unit): LoginUseCase.Params =
        LoginUseCase.Params(emailInput.text.value, passwordInput.text.value)

    private fun createRestoreParams(i: Unit): RestoreProfileUseCase.Params =
        RestoreProfileUseCase.Params(emailInput.text.value, passwordInput.text.value)

    private fun Single<Boolean>.updateAnalyticStableParam(): Single<Boolean> =
        this.flatMap { isEmailActivated ->
            if (isEmailActivated) {
                getUserIdUseCase.execute()
                    .doOnSuccess { updateStableParam(id = it) }
                    .map { isEmailActivated }
            } else {
                Single.just(isEmailActivated)
            }
        }

    private fun Single<Boolean>.setAppMetricAttribute(): Single<Boolean> =
        this.flatMap { isEmailConfirmed ->
            if (isEmailConfirmed) {
                rxSingle { getProfileUseCase.invoke() }
                    .flatMapObservable { it.asObservable() }
                    .doOnNext {
                        appMetric.setProfileAttributes(it.getMetricAttributes())
                    }
            }
            Single.just(isEmailConfirmed)
        }
}
