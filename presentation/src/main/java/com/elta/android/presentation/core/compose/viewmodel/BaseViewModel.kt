package com.elta.android.presentation.core.compose.viewmodel

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.utils.LocaleHelper
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber


interface ComposeScreenConfigurable {
    val screenConfigKey: String
    val getScreenConfigUseCase: GetScreenConfigFromCache
}

@Stable
abstract class BaseViewModel<ST> : ViewModel() {
    private val initState: ST
        get() = createInitState()

    protected abstract fun createInitState(): ST

    private var _router: Router? = null
    val router: Router
        get() = checkNotNull(_router)

    protected open val widgets: List<BaseWidgetModel<*>> = emptyList()

    private val _state = MutableStateFlow(initState)
    val state: StateFlow<ST>
        get() = _state.asStateFlow()

    private val action = MutableSharedFlow<Action>()

    private val _event = MutableSharedFlow<Event?>()
    val event: SharedFlow<Event?>
        get() = _event.asSharedFlow()

    private var reloadScreenConfigAction: (() -> Unit)? = null
    private var loadedScreenConfigLanguage: String? = null

    init {
        this.action
            .onEach {
                handleUserAction(it)
                _state.tryEmit(reduceStateByAction(state.value, it))
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, initState)
    }

    fun setRouter(router: Router) {
        _router = router
    }

    internal fun routerIsNotSet(): Boolean = _router == null

    open fun handleFragmentArguments(arguments: Bundle) {}

    open fun backClick() {
        router.exit()
    }

    val actionReceiver: (Action) -> Unit = { action ->
        sendAction(action)
    }

    infix fun sendAction(action: Action) {
        launch {
            this@BaseViewModel.action.emit(action)
        }
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch {
            block(this)
        }

    protected open fun handleError(error: Throwable, message: String? = null) {
        Timber.e(error, message ?: error.message.orEmpty())
    }

    protected fun sendEvent(event: Event) {
        launch {
            _event.emit(event)
        }
    }

    protected fun reduceState(reduceBlock: () -> ST) {
        _state.tryEmit(reduceBlock())
    }

    protected open fun reduceStateByAction(currentState: ST, action: Action): ST = currentState

    protected open fun handleUserAction(action: Action) {
        when (action) {
            is AppAction.BackPressure -> backClick()
        }
    }

    protected fun List<BaseWidgetModel<*>>.actionObserve() = this.also { widgets ->
        widgets.map { it.action }
            .merge()
            .onEach { action ->
                this@BaseViewModel sendAction action
            }
            .shareIn(viewModelScope, SharingStarted.Eagerly)
    }



    /**
     * Загружает конфигурацию экрана и обновляет state
     * @param context Контекст приложения
     * @param updateState Функция для обновления state с новой конфигурацией
     */
    protected fun loadScreenConfig(
        context: Context,
        updateState: (screenEntity: ScreenEntity?, isImageReady: Boolean) -> ST
    ) {
        reloadScreenConfigAction = {
            loadScreenConfig(
                context = context,
                updateState = updateState
            )
        }
        loadedScreenConfigLanguage = LocaleHelper.getLanguage(context)

        // Проверяем, реализует ли ViewModel интерфейс
        if (this !is ComposeScreenConfigurable) {
            Log.w("BaseViewModel", "⏭️ ViewModel не реализует ComposeScreenConfigurable, используем дефолты")
            // Сразу обновляем state с дефолтными значениями
            reduceState { updateState(null, true) }
            return
        }

        val key = screenConfigKey
        Log.i("BaseViewModel", "🔄 [Compose] Загружаем конфигурацию для ключа: '$key'")

        launch {
            val startTime = System.currentTimeMillis()
            when (val result = getScreenConfigUseCase(key)) {
                is Resource.Success -> {
                    val duration = System.currentTimeMillis() - startTime
                    val screenEntity = result.data
                    Log.i("BaseViewModel", "✅ [Compose] Конфигурация загружена за ${duration}ms для '$key':")
                    Log.i("BaseViewModel", "   - title: '${screenEntity.title}'")
                    Log.i("BaseViewModel", "   - description: '${screenEntity.description}'")
                    Log.i("BaseViewModel", "   - backgroundImageUrl: '${screenEntity.backgroundImageUrl}'")

                    // Обновляем state с новой конфигурацией
                    reduceState { updateState(screenEntity, true) }
                    Log.i("BaseViewModel", "✅ [Compose] State обновлен с конфигурацией для '$key'")
                }
                is Resource.Error -> {
                    val duration = System.currentTimeMillis() - startTime
                    Log.e("BaseViewModel", "❌ [Compose] Ошибка загрузки конфигурации для '$key' за ${duration}ms: ${result.message}")
                    // Даже если ошибка, показываем дефолты
                    reduceState { updateState(null, true) }
                    Log.d("BaseViewModel", "📋 [Compose] State обновлен с дефолтными значениями для '$key'")
                }
                is Resource.Loading -> {
                    Log.d("BaseViewModel", "⏳ [Compose] Загрузка конфигурации для '$key'...")
                }
            }
        }
    }

    protected fun loadMultipleScreenConfigs(
        context: Context,
        configs: Map<String, String>, // ключ -> slug
        onUpdate: (results: Map<String, Pair<ScreenEntity?, Boolean>>) -> ST
    ) {
        reloadScreenConfigAction = {
            loadMultipleScreenConfigs(
                context = context,
                configs = configs,
                onUpdate = onUpdate
            )
        }
        loadedScreenConfigLanguage = LocaleHelper.getLanguage(context)

        Log.i("BaseViewModel", "🔄 [Compose] Загружаем ${configs.size} конфигураций")
        Log.d("BaseViewModel", "📋 [Compose] Карта конфигураций: ${configs.entries.joinToString { "${it.key} -> ${it.value}" }}")

        launch {
            try {
                val startTime = System.currentTimeMillis()
                val results = mutableMapOf<String, Pair<ScreenEntity?, Boolean>>()

                configs.forEach { (key, slug) ->
                    Log.d("BaseViewModel", "🔍 [Compose] Обрабатываем: key='$key', slug='$slug'")

                    if (this@BaseViewModel !is ComposeScreenConfigurable) {
                        Log.e("BaseViewModel", "❌ [Compose] ViewModel не реализует ComposeScreenConfigurable!")
                        results[key] = null to true
                        return@forEach
                    }

                    when (val result = getScreenConfigUseCase(slug)) {
                        is Resource.Success -> {
                            val screenEntity = result.data
                            Log.i("BaseViewModel", "✅ [Compose] Успех для '$slug' (key='$key'): title='${screenEntity.title}'")
                            results[key] = screenEntity to true
                        }
                        is Resource.Error -> {
                            Log.e("BaseViewModel", "❌ [Compose] Ошибка для '$slug' (key='$key'): ${result.message}")
                            results[key] = null to true
                        }
                        is Resource.Loading -> {
                            Log.w("BaseViewModel", "⚠️ [Compose] Loading state для '$slug' - не должно происходить!")
                        }
                    }
                }

                val duration = System.currentTimeMillis() - startTime
                Log.i("BaseViewModel", "✅ [Compose] Все ${results.size} конфигураций обработаны за ${duration}ms")

                // Обновляем state со всеми результатами
                reduceState { onUpdate(results) }
                Log.i("BaseViewModel", "✅ [Compose] State обновлен со всеми конфигурациями")
            } catch (e: Exception) {
                Log.e("BaseViewModel", "❌ [Compose] Исключение в loadMultipleScreenConfigs: ${e.message}", e)
            }
        }
    }

    fun reloadScreenConfigIfLanguageChanged(context: Context) {
        val reloadAction = reloadScreenConfigAction ?: return
        val currentLanguage = LocaleHelper.getLanguage(context)
        val lastLanguage = loadedScreenConfigLanguage ?: run {
            loadedScreenConfigLanguage = currentLanguage
            return
        }

        if (lastLanguage != currentLanguage) {
            Log.i(
                "BaseViewModel",
                "🌐 [Compose] Язык изменился ($lastLanguage -> $currentLanguage), перезагружаем screen config"
            )
            loadedScreenConfigLanguage = currentLanguage
            reloadAction.invoke()
        }
    }
}
