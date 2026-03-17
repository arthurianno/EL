package com.elta.android.presentation.features.language.pm

import android.content.Context
import android.util.Log
import com.elta.android.domain.features.user.interactor.UpdateLanguageTagUseCase
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.language.model.AppLanguage
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
    val continueAction = action<Unit>()

    val selectedLanguageState = state<AppLanguage>()
    val showContinueButtonState = state<Boolean>(true)
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

        isFirstLaunchState.observable
            .doOnNext { isFirstLaunch ->
                Log.i(TAG, "isFirstLaunchState changed: $isFirstLaunch")
            }
            .map { true }
            .subscribe(showContinueButtonState.consumer)
            .untilDestroy()

        selectLanguageAction.observable
            .skipWhileInProgress()
            .doOnNext { language ->
                Log.i(
                    TAG,
                    "selectLanguageAction: clicked=${language.code}, current=${selectedLanguageState.valueOrNull?.code}, firstLaunch=${isFirstLaunchState.value}"
                )
            }
            .filter { language -> selectedLanguageState.valueOrNull != language }
            .doOnNext(selectedLanguageState.consumer)
            .doOnNext {
                Log.i(TAG, "selectLanguageAction: language selected, waiting for button click")
            }
            .doOnError(::handleError)
            .retry()
            .subscribe()
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .doOnNext { Log.i(TAG, "continueAction clicked") }
            .map { selectedLanguageState.valueOrNull ?: AppLanguage.fromCode(LocaleHelper.getLanguage(context)) }
            .flatMapCompletable { language ->
                if (isFirstLaunchState.value) {
                    Log.i(TAG, "continueAction: first launch, applying language=${language.code}")
                    applyLanguage(language)
                        .andThen(
                            Completable.fromAction {
                                LocaleHelper.markPendingGreetingAfterLanguageSelection(context)
                                Log.i(TAG, "continueAction: pending Greeting flag saved")
                                Log.i(TAG, "continueAction: recreateActivityCommand sent")
                                recreateActivityCommand.consumer.accept(Unit)
                            }
                        )
                } else {
                    Log.i(TAG, "continueAction: settings mode, applying language=${language.code}")
                    applyLanguage(language)
                        .andThen(
                            Completable.fromAction {
                                syncLanguageInBackground(language.code)
                                Log.i(TAG, "continueAction: recreateActivityCommand sent")
                                recreateActivityCommand.consumer.accept(Unit)
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
        updateLanguageOnBackend(languageTag)
            .doOnComplete { Log.i(TAG, "syncLanguageInBackground: complete tag=$languageTag") }
            .doOnError { error ->
                Log.e(TAG, "syncLanguageInBackground: error tag=$languageTag, message=${error.message}", error)
            }
            .subscribe()
            .untilDestroy()
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
