@file:Suppress("NOTHING_TO_INLINE", "TooManyFunctions", "MethodOverloading", "LabeledExpression")

package com.elta.android.presentation.core.pm

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import coil.imageLoader
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.trackEvent
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.date.DateChangedEvent
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.pm.listeners.ConnectionListener
import com.elta.android.presentation.core.pm.listeners.Trackable
import com.elta.android.presentation.core.pm.widgets.ErrorHandler
import com.elta.android.presentation.core.pm.widgets.errorHandler
import com.elta.android.presentation.core.pm.widgets.networkControl
import com.elta.android.presentation.core.pm.widgets.stateControl
import com.elta.android.presentation.core.ui.snackbarview.SnackBarData
import com.elta.android.presentation.core.ui.stateview.StateData
import com.elta.android.presentation.utils.cacheHelper.ImageCacheHelper
import com.nullgr.core.rx.bindProgress
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.skipWhileInProgress
import me.dmdev.rxpm.state
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val ACTION_DEBOUNCE_MILLIS = 500L

@Suppress("SpreadOperator")
abstract class BasePm(
    protected val services: ServiceFacade
) : PresentationModel() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var router: FlowRouter

    val progressState = state(false)
    val progressDialogState = state(false)
    protected open val screenConfigKey: String? = null
    protected open val getScreenConfigUseCase: GetScreenConfigFromCache? = null

    val screenConfigState = state<ScreenEntity?>()
    val imagePreloadState = state<Boolean>()

    val hideKeyBoardCommand = command<Unit>()
    val hideKeyboardAction = action<Unit>()

    val showKeyBoardCommand = command<Unit>()
    val showSnackBarCommand = command<SnackBarData>(bufferSize = 1)
    val showToastCommand = command<Int>()
    val clearFocusCommand = command<Unit>()

    val retryAction = action<Unit>()

    val networkStateAction = action<Boolean>()
    val networkStateCommand = command<Boolean>(bufferSize = 1)

    val errorControl = stateControl()
    val emptyControl = stateControl()

    internal val resources = services.resources
    internal val network = services.network
    internal val analytics = services.analytics
    internal val bus = services.bus
    internal val errorParser = services.errorParser

    protected val errorHandler: ErrorHandler = errorHandler()

    private val networkControl by lazy { networkControl(network) }

    open val isEmptyScreen: Boolean = false

    override fun onCreate() {
        super.onCreate()

        if (this is Trackable) {
            analytics.trackEvent(analyticsEvent)
        }

        if (this is ConnectionListener) {
            networkStateAction.observable
                .doOnNext { networkStateCommand.consumer.accept(it) }
                .subscribe()
                .untilDestroy()

            networkControl.observable
                .subscribe()
                .untilDestroy()
        }

        hideKeyboardAction.observable
            .doOnNext(hideKeyBoardCommand.consumer::accept)
            .doOnNext(clearFocusCommand.consumer::accept)
            .subscribe()
            .untilDestroy()
    }

    protected fun loadScreenConfig(context: Context) {
        val key = screenConfigKey
        val useCase = getScreenConfigUseCase

        // Если нет ключа или useCase - сразу говорим что готовы (покажутся дефолты)
        if (key == null || useCase == null) {
            Log.d("BasePm", "⏭️ Пропускаем загрузку конфигурации: key=$key, useCase=$useCase")
            imagePreloadState.consumer.accept(true)
            return
        }

        Log.i("BasePm", "🔄 Загружаем конфигурацию для ключа: '$key'")
        launch {
            val startTime = System.currentTimeMillis()
            when (val result = useCase(key)) {
                is Resource.Success -> {
                    val duration = System.currentTimeMillis() - startTime
                    val screenEntity = result.data
                    Log.i("BasePm", "✅ Конфигурация загружена за ${duration}ms для '$key':")
                    Log.i("BasePm", "   - title: '${screenEntity.title}'")
                    Log.i("BasePm", "   - description: '${screenEntity.description}'")
                    Log.i("BasePm", "   - backgroundImageUrl: '${screenEntity.backgroundImageUrl}'")

                    screenConfigState.consumer.accept(screenEntity)

                    val imageUrl = screenEntity.backgroundImageUrl
                    if (imageUrl != null) {
                        Log.d("BasePm", "🖼️ Изображение будет загружено из: $imageUrl")
                        // Всегда разрешаем загрузку картинки (из кеша или сети)
                        imagePreloadState.consumer.accept(true)
                    } else {
                        Log.d("BasePm", "📋 URL изображения отсутствует, используем дефолтное")
                        // URL нет - сразу true (покажется дефолт)
                        imagePreloadState.consumer.accept(true)
                    }
                }
                is Resource.Error -> {
                    val duration = System.currentTimeMillis() - startTime
                    Log.e("BasePm", "❌ Ошибка загрузки конфигурации для '$key' за ${duration}ms: ${result.message}")
                    // При ошибке - сразу true (покажется дефолт)
                    imagePreloadState.consumer.accept(true)
                    // Можно отправить null, чтобы фрагмент знал что конфига нет
                    //screenConfigState.consumer.accept(null)
                }
                is Resource.Loading -> {
                    Log.d("BasePm", "⏳ Загрузка конфигурации для '$key'...")
                }
            }
        }
    }

    private suspend fun handleConfigSuccess(screenEntity: ScreenEntity, context: Context) {
        screenConfigState.consumer.accept(screenEntity)

        screenEntity.backgroundImageUrl?.let { imageUrl ->
            val isInCache = ImageCacheHelper.isImageInCache(
                imageUrl,
                context,
                context.imageLoader
            )
            imagePreloadState.consumer.accept(isInCache)
        } ?: imagePreloadState.consumer.accept(true)
    }

    protected fun loadMultipleScreenConfigs(
        context: Context,
        configs: Map<String, String>, // ключ -> slug
        onUpdate: (results: Map<String, Pair<ScreenEntity?, Boolean>>) -> Unit
    ) {
        val useCase = getScreenConfigUseCase

        if (useCase == null) {
            Log.w("BasePm", "getScreenConfigUseCase is null, skipping loadMultipleScreenConfigs")
            // Вызываем callback с пустыми результатами
            val emptyResults = configs.keys.associateWith { null to true }
            onUpdate(emptyResults)
            return
        }

        launch {
            try {
                Log.d("BasePm", "loadMultipleScreenConfigs started with ${configs.size} configs")
                val results = mutableMapOf<String, Pair<ScreenEntity?, Boolean>>()

                configs.forEach { (key, slug) ->
                    Log.d("BasePm", "Processing config: key=$key, slug=$slug")

                    when (val result = useCase(slug)) {
                        is Resource.Success -> {
                            Log.d("BasePm", "SUCCESS for '$slug': ${result.data.title}")
                            val screenEntity = result.data
                            // Всегда разрешаем загрузку картинок (из кеша или сети)
                            results[key] = screenEntity to true
                        }
                        is Resource.Error -> {
                            Log.e("BasePm", "ERROR loading config '$slug': ${result.message}")
                            results[key] = null to true
                        }
                        is Resource.Loading -> {
                            Log.d("BasePm", "LOADING state for config '$slug'")
                        }
                    }
                }

                Log.d("BasePm", "All configs processed, results size: ${results.size}")
                // Обновляем через callback
                onUpdate(results)
            } catch (e: Exception) {
                Log.e("BasePm", "EXCEPTION in loadMultipleScreenConfigs: ${e.message}", e)
                // При ошибке возвращаем пустые результаты
                val emptyResults = configs.keys.associateWith { null to true }
                onUpdate(emptyResults)
            }
        }
    }


    internal fun sendDateChangedEvent() {
        bus.event(DateChangedEvent)
    }

    internal fun showSnackBar(data: SnackBarData) {
        showSnackBarCommand.consumer.accept(data)
    }

    internal fun hideKeyboard() {
        hideKeyBoardCommand.consumer.accept(Unit)
    }

    internal fun setErrorStateData(data: StateData) {
        errorControl.dataState.consumer.accept(data)
    }

    internal fun setErrorViewVisibility(visible: Boolean) {
        errorControl.visibilityState.consumer.accept(visible)
    }

    protected fun showToast(@StringRes messageId: Int) {
        showToastCommand.consumer.accept(messageId)
    }

    protected open fun handleError(error: Throwable) {
        Timber.tag(this::class.java.simpleName).e(error, error.message)
        errorHandler.handleError(error)
    }

    protected inline fun <T> Observable<List<T>>.mapFilter(crossinline predicate: (T) -> Boolean): Observable<List<T>> =
        map { it.filter { item -> predicate(item) } }

    protected fun <T> Observable<T>.debounceAction(): Observable<T> =
        this.throttleFirst(ACTION_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS)

    protected fun <T> Observable<T>.hideErrorContainer(): Observable<T> =
        this.doOnSubscribe { errorControl.visibilityState.consumer.accept(false) }

    protected fun <T> Single<T>.hideErrorContainer(): Single<T> =
        this.doOnSubscribe { errorControl.visibilityState.consumer.accept(false) }

    protected fun Completable.hideErrorContainer(): Completable =
        this.doOnSubscribe { errorControl.visibilityState.consumer.accept(false) }

    protected fun <T> Observable<T>.skipWhileInProgress(): Observable<T> =
        this.skipWhileInProgress(progressState.observable)

    protected fun <T> Observable<T>.bindProgress(): Observable<T> =
        this.bindProgress(progressState.consumer)

    protected fun <T> Single<T>.bindProgress(): Single<T> =
        this.bindProgress(progressState.consumer)

    protected fun Completable.bindProgress(): Completable =
        this.bindProgress(progressState.consumer)

    protected inline fun <T> Single<T>.trackEvent(
        @AnalyticsEventType name: String,
        vararg pairs: Pair<String, String>
    ): Single<T> =
        this.doOnSuccess { this@BasePm.trackEvent(name, *pairs) }

    protected inline fun <T> Observable<T>.trackEvent(@AnalyticsEventType name: String): Observable<T> =
        this.doOnNext { this@BasePm.trackEvent(name) }

    protected inline fun <T> Observable<T>.trackEvent(
        crossinline event: (T) -> AnalyticsEvent?
    ): Observable<T> =
        this.doOnNext { this@BasePm.trackEvent(event(it)) }

    protected inline fun <T> Observable<T>.trackEvent(
        @AnalyticsEventType name: String,
        vararg pairs: Pair<String, String>
    ): Observable<T> =
        this.doOnNext { this@BasePm.trackEvent(name, *pairs) }

    protected inline fun Completable.trackEvent(@AnalyticsEventType name: String): Completable =
        this.andThen(Completable.fromAction { this@BasePm.trackEvent(name) })

    protected inline fun Completable.trackEvent(
        crossinline event: () -> AnalyticsEvent?
    ): Completable =
        this.doOnComplete { this@BasePm.trackEvent(event()) }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch {
            block(this)
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }
}

interface ScreenConfigurable {
    val screenConfigKey: String
}
