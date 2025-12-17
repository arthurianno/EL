package com.elta.android.presentation.core.compose.viewmodel

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.utils.cacheHelper.ImageCacheHelper
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
        // Проверяем, реализует ли ViewModel интерфейс
        if (this !is ComposeScreenConfigurable) {
            Log.w("BaseViewModel", "ViewModel doesn't implement ComposeScreenConfigurable")
            // Сразу обновляем state с дефолтными значениями
            reduceState { updateState(null, true) }
            return
        }

        launch {
            when (val result = getScreenConfigUseCase(screenConfigKey)) {
                is Resource.Success -> {
                    val screenEntity = result.data
                    val imageUrl = screenEntity.backgroundImageUrl

                    if (imageUrl != null) {
                        // Проверяем есть ли картинка в кеше
                        val imageLoader = context.imageLoader
                        val isInCache = ImageCacheHelper.isImageInCache(
                            imageUrl,
                            context,
                            imageLoader
                        )
                        // Обновляем state
                        reduceState { updateState(screenEntity, isInCache) }
                    } else {
                        // URL нет - сразу готово с дефолтами
                        reduceState { updateState(screenEntity, true) }
                    }
                }
                is Resource.Error -> {
                    Log.e("BaseViewModel", "Error loading screen config: ${result.message}")
                    // При ошибке - показываем дефолты
                    reduceState { updateState(null, true) }
                }
                is Resource.Loading -> {
                    Log.d("BaseViewModel", "Loading screen config...")
                }
            }
        }
    }

    protected fun loadMultipleScreenConfigs(
        context: Context,
        configs: Map<String, String>, // ключ -> slug
        onUpdate: (results: Map<String, Pair<ScreenEntity?, Boolean>>) -> ST
    ) {
        launch {
            val results = mutableMapOf<String, Pair<ScreenEntity?, Boolean>>()

            configs.forEach { (key, slug) ->
                if (this@BaseViewModel !is ComposeScreenConfigurable) {
                    results[key] = null to true
                    return@forEach
                }

                when (val result = getScreenConfigUseCase(slug)) {
                    is Resource.Success -> {
                        val screenEntity = result.data
                        val imageUrl = screenEntity.backgroundImageUrl

                        val isImageReady = if (imageUrl != null) {
                            ImageCacheHelper.isImageInCache(
                                imageUrl,
                                context,
                                context.imageLoader
                            )
                        } else {
                            true
                        }

                        results[key] = screenEntity to isImageReady
                    }
                    is Resource.Error -> {
                        Log.e("BaseViewModel", "Error loading config '$slug': ${result.message}")
                        results[key] = null to true
                    }
                    is Resource.Loading -> {
                        Log.d("BaseViewModel", "Loading config '$slug'...")
                    }
                }
            }

            // Обновляем state со всеми результатами
            reduceState { onUpdate(results) }
        }
    }
}