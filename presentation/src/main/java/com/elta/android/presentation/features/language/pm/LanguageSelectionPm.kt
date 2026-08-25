package com.elta.android.presentation.features.language.pm

import android.content.Context
import android.util.Log
import com.elta.android.domain.features.user.interactor.UpdateLanguageTagUseCase
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.language.model.AppLanguage
import com.elta.android.presentation.features.language.model.AppRegion
import com.elta.android.presentation.utils.LocaleHelper
import com.elta.android.presentation.utils.OneSignalTags
import io.reactivex.Completable
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import javax.inject.Inject

private const val TAG = "LangFlow"

class LanguageSelectionPm @Inject constructor(
    private val context: Context,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val updateLanguageTagUseCase: UpdateLanguageTagUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val selectLanguageAction = action<AppLanguage>()
    val selectRegionAction = action<AppRegion>()
    val continueAction = action<Unit>()
    /** Нажатие кнопки «X» — закрыть экран без применения языка. */
    val closeAction = action<Unit>()

    val selectedLanguageState = state<AppLanguage>()
    val selectedRegionState = state<AppRegion>()
    val recreateActivityCommand = command<Unit>()

    private val isFirstLaunchState = state(true)

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "LanguageSelectionPm.onCreate()")

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map {
                val language = AppLanguage.fromCode(LocaleHelper.getLanguage(context))
                Log.i(TAG, "Initial selected language from LocaleHelper: ${language.code}")
                language
            }
            .subscribe(selectedLanguageState.consumer)
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map {
                val regionCode = LocaleHelper.getRegion(context)
                Log.i(TAG, "INIT: region from prefs=$regionCode")

                val region = AppRegion.fromCode(regionCode)
                Log.i(TAG, "INIT: mapped region=${region.code}")
                region
            }
            .subscribe(selectedRegionState.consumer)
            .untilDestroy()

        // X-кнопка: закрываем экран без применения языка.
        closeAction.observable
            .doOnNext { Log.i(TAG, "closeAction: navigating back without language change") }
            .doOnNext { router.exit() }
            .doOnError(::handleError)
            .retry()
            .subscribe()
            .untilDestroy()

        selectLanguageAction.observable
            .skipWhileInProgress()
            .doOnNext { language ->
                Log.i(TAG, "selectLanguageAction: clicked=${language.code}, current=${selectedLanguageState.valueOrNull?.code}")
            }
            .filter { language -> selectedLanguageState.valueOrNull != language }
            .doOnNext(selectedLanguageState.consumer)
            .doOnError(::handleError)
            .retry()
            .subscribe()
            .untilDestroy()

        selectRegionAction.observable
            .skipWhileInProgress()
            .doOnNext { region ->
                Log.i(TAG, "selectRegionAction: clicked=${region.code}, current=${selectedRegionState.valueOrNull?.code}")
            }
            .filter { region -> selectedRegionState.valueOrNull != region }
            .doOnNext(selectedRegionState.consumer)
            .doOnError(::handleError)
            .retry()
            .subscribe()
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .doOnNext { Log.i(TAG, "continueAction clicked") }
            .map {
                val language = selectedLanguageState.valueOrNull ?: AppLanguage.fromCode(LocaleHelper.getLanguage(context))
                val region = selectedRegionState.valueOrNull ?: AppRegion.fromCode(LocaleHelper.getRegion(context))
                language to region
            }
            .flatMapCompletable { (language, region) ->
                val currentLanguage = AppLanguage.fromCode(LocaleHelper.getLanguage(context))

                // Всегда сохраняем регион
                LocaleHelper.saveRegion(context, region.code)
                Log.i(TAG, "continueAction: region saved=${region.code}")
                // сразу читаем обратно
                val savedNow = LocaleHelper.getRegion(context)
                Log.i(TAG, "SAVE: region read right after save=$savedNow")

                if (isFirstLaunchState.value) {
                    Log.i(TAG, "continueAction: first launch, applying language=${language.code}")
                    applyLanguage(language)
                        .andThen(
                            Completable.fromAction {
                                LocaleHelper.markPendingGreetingAfterLanguageSelection(context)
                                Log.i(TAG, "continueAction: pending Greeting flag saved (committed)")
                                // Fix 9: on API 33+ LocaleManager triggers recreation automatically.
                                if (LocaleHelper.needsManualRecreate()) {
                                    Log.i(TAG, "continueAction: recreateActivityCommand sent (< API 33)")
                                    recreateActivityCommand.consumer.accept(Unit)
                                } else {
                                    Log.i(TAG, "continueAction: skip manual recreate — LocaleManager handles it (API 33+)")
                                }
                            }
                        )
                } else {
                    if (language == currentLanguage) {
                        Log.i(TAG, "continueAction: settings mode, language unchanged (${language.code}), syncing region/country and navigating back")
                        OneSignalTags.apply(context)
                        return@flatMapCompletable updateLanguageOnBackend(language.code)
                            .andThen(Completable.fromAction { router.exit() })
                    }

                    Log.i(TAG, "continueAction: settings mode, applying language=${language.code}")
                    applyLanguage(language)
                        .andThen(
                            Completable.fromAction {
                                syncLanguageInBackground(language.code)
                                // После смены языка из настроек — переходим на главный экран (HomeFlow),
                                // а не оставляем пользователя на экране выбора языка.
                                LocaleHelper.markPendingHomeAfterLanguageChange(context)
                                Log.i(TAG, "continueAction: pendingHome flag saved")
                                // Fix 9: same guard as above.
                                if (LocaleHelper.needsManualRecreate()) {
                                    Log.i(TAG, "continueAction: recreateActivityCommand sent (< API 33)")
                                    recreateActivityCommand.consumer.accept(Unit)
                                } else {
                                    Log.i(TAG, "continueAction: skip manual recreate — LocaleManager handles it (API 33+)")
                                }
                            }
                        )
                }
            }
            .doOnError(::handleError)
            .retry()
            .subscribe()
            .untilDestroy()
    }

    fun setFirstLaunch(isFirstLaunch: Boolean) {
        Log.i(TAG, "setFirstLaunch($isFirstLaunch)")
        isFirstLaunchState.consumer.accept(isFirstLaunch)
    }

    private fun applyLanguage(language: AppLanguage): Completable {
        return Completable.fromAction {
            Log.i(TAG, "applyLanguage: start code=${language.code}")
            LocaleHelper.setLocale(context, language.code)
            OneSignalTags.apply(context)
            Log.i(TAG, "applyLanguage: complete code=${language.code}")
        }
    }

    private fun syncLanguageInBackground(languageTag: String) {
        Log.i(TAG, "syncLanguageInBackground: start tag=$languageTag")
        // Fix 5: .untilDestroy() removed — the subscription must survive activity.recreate()
        // which destroys this PM. onErrorComplete() guarantees the chain always terminates.
        @Suppress("CheckResult")
        updateLanguageOnBackend(languageTag)
            .doOnComplete { Log.i(TAG, "syncLanguageInBackground: complete tag=$languageTag") }
            .doOnError { error ->
                Log.e(TAG, "syncLanguageInBackground: error tag=$languageTag, message=${error.message}", error)
            }
            .subscribe()
    }

    private fun updateLanguageOnBackend(languageTag: String): Completable {
        return getUserInfoUseCase.execute()
            .flatMapCompletable { userInfo ->
                Log.i(TAG, "updateLanguageOnBackend: isLoggedIn=${userInfo.isUserLoggedIn}")
                if (userInfo.isUserLoggedIn == true) {
                    updateLanguageTagUseCase.execute(
                        UpdateLanguageTagUseCase.Params(languageTag = languageTag)
                    )
                        .doOnComplete { Log.i(TAG, "updateLanguageOnBackend: backend updated with $languageTag") }
                        .doOnError { error ->
                            Log.e(TAG, "updateLanguageOnBackend: backend update failed, message=${error.message}", error)
                        }
                        .onErrorComplete()
                } else {
                    Log.i(TAG, "updateLanguageOnBackend: skip, user is guest")
                    Completable.complete()
                }
            }
            .doOnError { error -> Log.e(TAG, "updateLanguageOnBackend: getUserInfo failed, message=${error.message}", error) }
            .onErrorComplete()
    }
}
