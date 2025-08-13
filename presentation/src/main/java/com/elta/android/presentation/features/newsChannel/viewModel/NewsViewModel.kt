package com.elta.android.presentation.features.newsChannel.viewModel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elta.android.common.utils.EltaMessageClient
import com.elta.android.domain.common.repository.MediaRepository
import com.elta.android.domain.common.usecase.FileDeleteUseCase
import com.elta.android.domain.features.consultant.usecase.DownloadFileUseCase
import com.elta.android.domain.features.newsChannel.interactor.LoadMessagesNewsUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.ui.cluster.Dispatchers
import com.elta.android.presentation.features.consultant.model.CachingState
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.RecordGraphState
import com.elta.android.presentation.features.consultant.model.RecordState
import com.elta.android.presentation.features.newsChannel.model.ChatUiEntity
import com.elta.android.presentation.features.newsChannel.model.ContextMenuUiEntityNews
import com.elta.android.presentation.features.newsChannel.model.MessageUiEntity
import com.elta.android.presentation.features.newsChannel.model.NewsAction
import com.elta.android.presentation.features.newsChannel.model.NewsViewState
import com.elta.android.presentation.features.newsChannel.model.OpenDownloadedFile
import com.elta.android.presentation.features.newsChannel.model.ScrollToDown
import com.elta.android.presentation.features.newsChannel.model.ScrollToTop
import com.elta.android.presentation.features.newsChannel.model.ShareImage
import com.elta.android.presentation.features.newsChannel.model.reduceChatState
import com.elta.android.presentation.features.newsChannel.model.toUi
import com.elta.android.presentation.features.newsChannel.ui.components.analyzer.NewsChatAnalyzer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalTime
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider
import com.elta.android.presentation.features.newsChannel.model.PreviewStateNews
import com.nullgr.core.hardware.NetworkChecker

@OptIn(ExperimentalPermissionsApi::class)
class NewsViewModel @Inject constructor(
    private val loadNewsMessages: LoadMessagesNewsUseCase,
    private val mediaRepository: MediaRepository,
    private val downloadFile: DownloadFileUseCase,
    private val deleteFile: FileDeleteUseCase,
    appMetricTracker: AppMetricTracker,
    private val messageClient: EltaMessageClient,
    private val networkStatus: NetworkChecker,
    private val context: Context
) : BaseViewModel<NewsViewState>(), LifecycleEventObserver {
    private var downloadFileSubscriber: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var currentCursor: Long? = null
    private var hasMoreMessages: Boolean = true
    private var isFirstEntry = true
    private var isLoadingNextPage = false
    private var isSwipeRefreshing = false
    private var swipeRefreshCount = 0
    private var lastSwipeRefreshTime: Long? = null

    private companion object {
        const val LIMIT = 5
        const val SWIPE_REFRESH_LIMIT = 3
        const val SWIPE_REFRESH_COOLDOWN_MINUTES = 5L
        const val PREFS_NAME = "NewsViewModelPrefs"
        const val KEY_SWIPE_COUNT = "swipeRefreshCount"
        const val KEY_LAST_SWIPE_TIME = "lastSwipeRefreshTime"
    }

    override fun createInitState(): NewsViewState =
        NewsViewState(
            inputMessage = "",
            recordGraphState = RecordGraphState(
                recordState = RecordState.Empty,
                recordGraph = emptyList(),
                duration = LocalTime.MIN
            ),
            connectState = ConnectState.Connecting,
            chat = ChatUiEntity(
                messages = emptyList(),
            ),
            hasNewMessages = false,
            isOpenBottomSheet = false,
            contextMenuEntity = ContextMenuUiEntityNews(
                isOpenContextMenu = false,
                selectedMessage = null
            ),
            isLoadingNextMessagesPage = false,
            previewState = PreviewStateNews(
                isPhotoPreview = false,
                imageData = null
            ),
            isSwipeRefreshing = false
        )

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        event.handleSessionState()
    }

    init {
        appMetricTracker.trackEvent(AppMetricEvent.TapOnlineConsultant)
        messageClient.isNewsScreenActive = true
        updateNetworkState()
        registerNetworkCallback()
        loadSwipeRefreshState()
        loadNewsPage()
    }

    override fun backClick() {
        messageClient.isNewsScreenActive = false
        super.backClick()
    }

    private fun Lifecycle.Event.handleSessionState() {
        when (this) {
            Lifecycle.Event.ON_CREATE -> Log.e("NewsViewModel", "ON_CREATE")
            Lifecycle.Event.ON_START -> Log.e("NewsViewModel", "ON_START")
            Lifecycle.Event.ON_RESUME -> Log.e("NewsViewModel", "ON_RESUME")
            Lifecycle.Event.ON_PAUSE -> Log.e("NewsViewModel", "ON_PAUSE")
            Lifecycle.Event.ON_DESTROY -> Log.e("NewsViewModel", "ON_DESTROY")
            else -> Unit
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        networkCallback = null
        messageClient.isNewsScreenActive = false
        isFirstEntry = true
    }

    private fun loadSwipeRefreshState() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        swipeRefreshCount = prefs.getInt(KEY_SWIPE_COUNT, 0)
        lastSwipeRefreshTime = prefs.getLong(KEY_LAST_SWIPE_TIME, 0).takeIf { it != 0L }
        Log.d(
            "NewsViewModel",
            "Loaded swipe state: count=$swipeRefreshCount, lastTime=$lastSwipeRefreshTime"
        )
    }

    private fun saveSwipeRefreshState() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt(KEY_SWIPE_COUNT, swipeRefreshCount)
            putLong(KEY_LAST_SWIPE_TIME, lastSwipeRefreshTime ?: 0)
            apply()
        }
        Log.d(
            "NewsViewModel",
            "Saved swipe state: count=$swipeRefreshCount, lastTime=$lastSwipeRefreshTime"
        )
    }

    private fun registerNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                viewModelScope.launch {
                    Log.d("NewsViewModel", "Сеть доступна")
                    if (state.value.connectState == ConnectState.Offline) {
                        reduceState { state.value.copy(connectState = ConnectState.Connect) }
                        Log.d("NewsViewModel", "Сеть восстановлена, пытаемся загрузить новости")
                    }
                }
            }

            override fun onLost(network: Network) {
                viewModelScope.launch {
                    Log.d("NewsViewModel", "Сеть потеряна")
                    reduceState { state.value.copy(connectState = ConnectState.Offline) }
                }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback?.let {
            connectivityManager.registerNetworkCallback(networkRequest, it)
        }
    }

    private fun updateNetworkState() {
        viewModelScope.launch {
            reduceState {
                state.value.copy(
                    connectState = if (!networkStatus.isInternetConnectionEnabled()) {
                        ConnectState.Offline
                    } else {
                        ConnectState.Connect
                    }
                )
            }
        }
    }

    private fun loadNewsPage(cursor: Long? = null) {
        if (isLoadingNextPage) return
        isLoadingNextPage = true

        launch {
            try {
                reduceState {
                    state.value.copy(
                        isLoadingNextMessagesPage = true,
                        hasNewMessages = false,
                        isSwipeRefreshing = isSwipeRefreshing
                    )
                }
                delay(600)

                val allMessagesUi = mutableListOf<MessageUiEntity>()
                if (!networkStatus.isInternetConnectionEnabled()) {
                    reduceState { state.value.copy(connectState = ConnectState.Offline) }
                    // При отсутствии сети возвращаем пустой список
                    allMessagesUi.clear()
                } else {
                    reduceState { state.value.copy(connectState = ConnectState.Connect) }
                    val response =
                        loadNewsMessages(cursor = cursor, limit = LIMIT, direction = "DESC")
                    // При swipeRefresh очищаем список, если cursor == null
                    if (cursor == null && isSwipeRefreshing) {
                        reduceState { state.value.copy(chat = ChatUiEntity(messages = emptyList())) }
                    }
                    if (response.news.isNotEmpty()) {
                        allMessagesUi.addAll(response.news.toUi().filter { newMessage ->
                            !state.value.chat.messages.any { it.id == newMessage.id }
                        })
                        hasMoreMessages = response.hasNextPage
                        currentCursor = response.endCursor
                    } else {
                        hasMoreMessages = false
                    }
                }

                if (allMessagesUi.isNotEmpty()) {
                    Log.d(
                        "NewsViewModel",
                        "Updating chat with titles: ${allMessagesUi.map { it.title }}"
                    )
                    updateChatState(allMessagesUi)
                }
                reduceState {
                    state.value.copy(
                        isLoadingNextMessagesPage = false,
                        isSwipeRefreshing = false
                    )
                }
                isLoadingNextPage = false
                isSwipeRefreshing = false
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error loading news: ${e.message}", e)
                handleError(e)
                reduceState {
                    state.value.copy(
                        isLoadingNextMessagesPage = false,
                        isSwipeRefreshing = false,
                        connectState = if (!networkStatus.isInternetConnectionEnabled()) ConnectState.Offline else ConnectState.Connect
                    )
                }
                isLoadingNextPage = false
                isSwipeRefreshing = false
            }
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            AppAction.BackPressure -> {
                when {
                    state.value.contextMenuEntity.isOpenContextMenu -> reduceState {
                        state.value.copy(
                            contextMenuEntity = state.value.contextMenuEntity.copy(isOpenContextMenu = false)
                        )
                    }

                    state.value.previewState.isPhotoPreview -> reduceState {
                        state.value.copy(
                            previewState = state.value.previewState.copy(
                                isPhotoPreview = false,
                                imageData = null
                            )
                        )
                    }

                    else -> backClick()
                }
            }

            is NewsAction.ChatMessageClick -> {
                if (action.message.image != null) {
                    reduceState {
                        state.value.copy(
                            previewState = state.value.previewState.copy(
                                imageData = action.message.image.base64Data,
                                isPhotoPreview = true,
                            )
                        )
                    }
                } else if (action.message.document != null) {
                    reduceState {
                        state.value.copy(
                            previewState = state.value.previewState.copy(
                                imageData = action.message.document.base64Data,
                                isPhotoPreview = true,
                            )
                        )
                    }
                }
            }

            is NewsAction.OnUpIconClick -> {
                Log.d("NewsViewModel", "OnUpIconClick action received")
                sendEvent(ScrollToTop)
            }

            is NewsAction.OnDownIconClick -> {
                Log.d("NewsViewModel", "OnDownIconClick action received")
                sendEvent(ScrollToDown)
            }

            is NewsAction.OnSwipeRefresh -> {
                val currentTime = System.currentTimeMillis()
                val cooldownMillis = SWIPE_REFRESH_COOLDOWN_MINUTES * 60 * 1000

                if (lastSwipeRefreshTime != null && (currentTime - (lastSwipeRefreshTime
                        ?: 0)) < cooldownMillis && swipeRefreshCount >= SWIPE_REFRESH_LIMIT
                ) {
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            val remainingTime =
                                cooldownMillis - (currentTime - (lastSwipeRefreshTime ?: 0))
                            val remainingMinutes = remainingTime / (60 * 1000) + 1
                            reduceState { state.value.copy(isSwipeRefreshing = true) }
                            delay(600)
                            reduceState { state.value.copy(isSwipeRefreshing = false) }
                            Toast.makeText(
                                context,
                                "Лимит обновлений исчерпан. Подождите $remainingMinutes минут.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    return
                }

                if (lastSwipeRefreshTime == null || (currentTime - (lastSwipeRefreshTime
                        ?: 0)) >= cooldownMillis
                ) {
                    swipeRefreshCount = 0
                }

                swipeRefreshCount++
                lastSwipeRefreshTime = currentTime
                saveSwipeRefreshState()

                isSwipeRefreshing = true
                viewModelScope.launch {
                    reduceState { state.value.copy(isSwipeRefreshing = true) }
                    delay(600)
                }
                currentCursor = null
                loadNewsPage()
            }

            is NewsAction.VerifyFile -> handleFileState(action.message)
            is NewsAction.DownloadImage -> handleDownloadImage(action.imageData)
            is NewsAction.ShareImage -> handleShareImage(action.imageData)
            is NewsAction.LoadNextPage -> {
                if (hasMoreMessages && !isLoadingNextPage) {
                    Log.d("NewsViewModel", "Loading next page with cursor=$currentCursor")
                    loadNewsPage(cursor = currentCursor)
                }
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun handleDownloadImage(imageData: String?) {
        launch {
            try {
                Log.d("NewsViewModel", "Начало скачивания: imageData=$imageData")
                if (imageData == null) {
                    Log.e("NewsViewModel", "No valid image data for download")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Ошибка: нет данных изображения",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // Декодируем Base64
                val imageBytes = Base64.decode(imageData)
                Log.d("NewsViewModel", "Decoded bytes length: ${imageBytes.size}")

                // Сохраняем в кэш
                val fileName = "downloaded_image_${System.currentTimeMillis()}.jpg"
                val downloadedUri = mediaRepository.saveImageToCache(imageBytes, fileName)

                if (downloadedUri != null) {
                    Log.d("NewsViewModel", "Изображение успешно сохранено: $downloadedUri")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Изображение успешно скачано", Toast.LENGTH_SHORT)
                            .show()
                    }
                    val mediaStoreUri = saveToMediaStore(downloadedUri, fileName)
                    sendEvent(OpenDownloadedFile(mediaStoreUri ?: downloadedUri))
                    reduceState {
                        state.value.copy(
                            previewState = state.value.previewState.copy(
                                isPhotoPreview = false,
                                imageData = null
                            )
                        )
                    }
                } else {
                    Log.e("NewsViewModel", "Failed to save image to cache")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Ошибка при скачивании изображения",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error downloading image: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ошибка при скачивании: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun handleShareImage(imageData: String?) {
        launch {
            try {
                Log.d("NewsViewModel", "Начало шаринга: imageData=$imageData")
                if (imageData == null) {
                    Log.e("NewsViewModel", "No valid image data for share")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Ошибка: нет изображения для шаринга",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // Декодируем Base64
                val imageBytes = Base64.decode(imageData)
                Log.d("NewsViewModel", "Decoded bytes length: ${imageBytes.size}")

                // Сохраняем в кэш
                val fileName = "shared_image_${System.currentTimeMillis()}.jpg"
                val file = File(context.cacheDir, fileName)
                FileOutputStream(file).use { it.write(imageBytes) }

                // Проверяем существование файла
                Log.d("NewsViewModel", "File path: ${file.absolutePath}, exists: ${file.exists()}")
                if (!file.exists()) {
                    Log.e("NewsViewModel", "File does not exist")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Файл не существует", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                Log.d("NewsViewModel", "Authorit being used: ${context.packageName}.fileprovider") // Или то, что вы используете
                Log.d("NewsViewModel", "File path: ${file.absolutePath}, exists: ${file.exists()}, length: ${file.length()}")
                // Получаем Uri через FileProvider
                val shareUri = FileProvider.getUriForFile(
                    context,
                    "com.elta.android.fileprovider", // Исправьте на это
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                Log.d("NewsViewModel", "Share intent prepared for $shareUri")
                sendEvent(ShareImage(shareIntent))

                reduceState {
                    state.value.copy(
                        previewState = state.value.previewState.copy(
                            isPhotoPreview = false,
                            imageData = null
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error sharing image: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка при шаринге: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private suspend fun saveToMediaStore(downloadedUri: Uri, fileName: String): Uri? {
        return withContext(Dispatchers.Main) {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EltaApp")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val collection =
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val newUri = resolver.insert(collection, contentValues)

                newUri?.let { uri ->
                    resolver.openInputStream(downloadedUri)?.use { input ->
                        resolver.openOutputStream(uri)?.use { output ->
                            input.copyTo(output)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    Log.d("NewsViewModel", "File saved to MediaStore: $uri")
                    uri
                } ?: run {
                    Log.e("NewsViewModel", "Failed to create new URI in MediaStore")
                    null
                }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error saving to MediaStore: ${e.message}", e)
                null
            }
        }
    }

    override fun reduceStateByAction(currentState: NewsViewState, action: Action): NewsViewState =
        when (action) {
            is NewsAction.InputChanged -> currentState.copy(inputMessage = action.text)
            NewsAction.FileClick -> currentState.copy(isOpenBottomSheet = true)
            is NewsAction.OnDismissContextMenu -> currentState.copy(
                contextMenuEntity = currentState.contextMenuEntity.copy(isOpenContextMenu = false)
            )

            is NewsAction.ChatMessageLongClick -> currentState.copy(
                contextMenuEntity = currentState.contextMenuEntity.copy(
                    isOpenContextMenu = true,
                    selectedMessage = action.message
                )
            )

            else -> currentState
        }

    private fun handleFileState(message: MessageUiEntity) {
        val document = message.document ?: message.image ?: return
        val currentState = state.value

        val isDocumentType = document.fileType == MessageType.Document

        when (document.cachingState) {
            CachingState.Cached -> {
                val screen = if (isDocumentType) {
                    Screens.ViewPdfScreen(document.uri ?: Uri.EMPTY)
                } else {
                    Screens.ViewVideoScreen(document.uri ?: Uri.EMPTY)
                }
                router.navigateTo(screen)
            }

            CachingState.Downloading -> {
                if (downloadFileSubscriber?.isActive == true) {
                    downloadFileSubscriber?.cancel()
                    downloadFileSubscriber = null
                }
                document.uri?.let { deleteFile(it) }
                reduceState {
                    currentState.copy(
                        chat = currentState.chat.reduceChatState(
                            messageId = message.id.toString(),
                            cachingState = CachingState.NotCached,
                            uri = null,
                            size = document.size
                        )
                    )
                }
            }

            else -> downloadFileSubscriber = launch { startDownloadFile(message) }
        }
    }

    private fun updateChatState(messagesUi: List<MessageUiEntity>) {
        val updatedMessages = messagesUi.map { it.copy(isNewMessage = true) }
        val messagesWithCorner = NewsChatAnalyzer.defineMessagesInSequence(updatedMessages)
        val messagesWithDate = NewsChatAnalyzer.defineChangingDateMessages(messagesWithCorner)

        val currentMessages = if (isSwipeRefreshing) {
            emptyList() // Очищаем текущие сообщения при swipeRefresh
        } else {
            state.value.chat.messages
        }
        val allMessages = (currentMessages + messagesWithDate)
            .distinctBy { it.id } // Убедимся, что нет дубликатов по ID
            .sortedByDescending { it.dateSending.timestamp }

        reduceState {
            state.value.copy(
                chat = ChatUiEntity(messages = allMessages),
                hasNewMessages = messagesWithDate.isNotEmpty(),
                isLoadingNextMessagesPage = false
            )
        }

    }

    private suspend fun startDownloadFile(message: MessageUiEntity) {
        val document = message.document ?: message.image ?: return
        val url = document.url.orEmpty()

        if (url.isBlank() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            Log.e("NewsViewModel", "Некорректный URL: $url")
            reduceState {
                state.value.copy(
                    chat = state.value.chat.reduceChatState(
                        messageId = message.id.toString(),
                        cachingState = CachingState.NotCached,
                        size = document.size
                    )
                )
            }
            return
        }

        reduceState {
            Log.d("NewsViewModel", "Установка состояния Downloading для сообщения ${message.id}")
            state.value.copy(
                chat = state.value.chat.reduceChatState(
                    messageId = message.id.toString(),
                    cachingState = CachingState.Downloading,
                    size = document.size
                )
            )
        }

        try {
            val downloadedFileUri = downloadFile(
                url = url,
                fileName = document.fileName
            )
            val fileSize = mediaRepository.getFileSize(downloadedFileUri) ?: document.size
            Log.d(
                "NewsViewModel",
                "Файл загружен: url=$url, fileName=${document.fileName}, downloadedFileUri=$downloadedFileUri, size=$fileSize"
            )

            val updatedChat = state.value.chat.reduceChatState(
                messageId = message.id.toString(),
                cachingState = CachingState.Cached,
                uri = downloadedFileUri,
                size = fileSize?.toDouble()
            )

            reduceState {
                Log.d(
                    "NewsViewModel",
                    "Обновление состояния чата: messageId=${message.id}, cachingState=Cached"
                )
                state.value.copy(chat = updatedChat)
            }

            val screen = if (document.fileType == MessageType.Document) {
                Screens.ViewPdfScreen(downloadedFileUri)
            } else {
                Screens.ViewVideoScreen(downloadedFileUri)
            }
            router.navigateTo(screen)
        } catch (e: Exception) {
            Log.e("NewsViewModel", "Не удалось скачать файл: ${e.message}")
            val errorChat = state.value.chat.reduceChatState(
                messageId = message.id.toString(),
                cachingState = CachingState.NotCached,
                size = document.size
            )
            reduceState {
                state.value.copy(chat = errorChat)
            }
        }
    }
}
