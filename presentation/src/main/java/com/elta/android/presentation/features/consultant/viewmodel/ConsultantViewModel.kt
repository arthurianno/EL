package com.elta.android.presentation.features.consultant.viewmodel

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elta.android.common.utils.EltaMessageClient
import com.elta.android.domain.common.usecase.CopyTextUseCase
import com.elta.android.domain.common.usecase.FileDeleteUseCase
import com.elta.android.domain.common.usecase.PhotoCreateUseCase
import com.elta.android.domain.features.consultant.model.ChatState
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.usecase.DeleteMessageUseCase
import com.elta.android.domain.features.consultant.usecase.EditMessageUseCase
import com.elta.android.domain.features.consultant.usecase.GetMessagesUseCase
import com.elta.android.domain.features.consultant.usecase.LoadLastMessagesUseCase
import com.elta.android.domain.features.consultant.usecase.LoadNextCachedMessagesUseCase
import com.elta.android.domain.features.consultant.usecase.SendMessageUseCase
import com.elta.android.domain.features.consultant.usecase.SendRateUseCase
import com.elta.android.domain.features.consultant.usecase.WebimChatStateUseCase
import com.elta.android.domain.features.consultant.usecase.WebimNetworkStateUseCase
import com.elta.android.domain.features.consultant.usecase.WebimSessionUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.features.consultant.model.CachingState
import com.elta.android.presentation.features.consultant.model.ChatUiEntity
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.ConsultantViewState
import com.elta.android.presentation.features.consultant.model.ContextMenuUiEntity
import com.elta.android.presentation.features.consultant.model.FileSelect
import com.elta.android.presentation.features.consultant.model.MakeVibration
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import com.elta.android.presentation.features.consultant.model.OpenCamera
import com.elta.android.presentation.features.consultant.model.OpenSettings
import com.elta.android.presentation.features.consultant.model.PhotoSelect
import com.elta.android.presentation.features.consultant.model.PreviewState
import com.elta.android.presentation.features.consultant.model.RatingUiEntity
import com.elta.android.presentation.features.consultant.model.RecordGraphState
import com.elta.android.presentation.features.consultant.model.RecordState
import com.elta.android.presentation.features.consultant.model.ScrollToDown
import com.elta.android.presentation.features.consultant.model.SendAutoMessage
import com.elta.android.presentation.features.consultant.model.reduceAudioState
import com.elta.android.presentation.features.consultant.model.reduceChatState
import com.elta.android.presentation.features.consultant.model.toUi
import com.elta.android.presentation.features.consultant.ui.ConsultantFragment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalPermissionsApi::class)
class ConsultantViewModel @Inject constructor(
    private val webimSession: WebimSessionUseCase,
    private val webimNetworkState: WebimNetworkStateUseCase,
    private val chatState: WebimChatStateUseCase,
    private val sendMessage: SendMessageUseCase,
    private val getMessages: GetMessagesUseCase,
    private val loadLastMessages: LoadLastMessagesUseCase,
    private val loadNextCachedMessages: LoadNextCachedMessagesUseCase,
    private val editMessage: EditMessageUseCase,
    private val deleteMessage: DeleteMessageUseCase,
    private val messageTimeline: ConsultantMessageTimeline,
    private val fileController: ConsultantFileController,
    private val photoCreate: PhotoCreateUseCase,
    private val voiceRecorder: ConsultantVoiceRecorder,
    private val playback: ConsultantPlaybackController,
    private val deleteFile: FileDeleteUseCase,
    private val sendRate: SendRateUseCase,
    private val copyText: CopyTextUseCase,
    private val appMetricTracker: AppMetricTracker,
    private val messageClient: EltaMessageClient
) : BaseViewModel<ConsultantViewState>(), LifecycleEventObserver {
    override fun createInitState(): ConsultantViewState =
        ConsultantViewState(
            inputMessage = "",
            recordGraphState = RecordGraphState(
                recordState = RecordState.Empty,
                recordGraph = emptyList(),
                duration = LocalTime.MIN
            ),
            connectState = ConnectState.Connecting,
            chat = ChatUiEntity(
                messages = emptyList(),
                ratingEntity = RatingUiEntity(
                    isRatingMessageShowing = false,
                    starsCount = null
                )
            ),
            user = WebimUser(
                id = "",
                name = ""
            ),
            previewState = PreviewState(
                isPhotoPreview = false,
                isFromCamera = false,
                uriPhoto = null,
                urlPhoto = null
            ),
            hasNewMessages = false,
            isOpenBottomSheet = false,
            audioFileUri = Uri.EMPTY,
            contextMenuEntity = ContextMenuUiEntity(
                isOpenContextMenu = false,
                selectedMessage = null
            ),
            isEditMessage = false,
            messageForEdit = null,
            isLoadingNextMessagesPage = false
        )

    private var messageFromCache: List<MessageUiEntity> = emptyList()

    private val ratingScheduler by lazy { ConsultantRatingScheduler { sendRate(it) } }

    val cameraDialog = BaseDialogWidgetModel<Unit>(
        positiveOnCLick = { sendEvent(OpenSettings) }, negativeOnCLick = {}
    )
    val storageDialog = BaseDialogWidgetModel<Unit>(
        positiveOnCLick = { sendEvent(OpenSettings) }, negativeOnCLick = {}
    )
    val audioDialog = BaseDialogWidgetModel<Unit>(
        positiveOnCLick = { sendEvent(OpenSettings) }, negativeOnCLick = {}
    )
    val abortAudioRecordDialog = BaseDialogWidgetModel<Unit>(
        positiveOnCLick = { deleteRecordVoice() }, negativeOnCLick = {}
    )
    val resolutionFeedbackDialog = BaseDialogWidgetModel<Unit>(
        positiveOnCLick = {
            val currentState = state.value
            reduceState {
                currentState.copy(
                    chat = currentState.chat.updateRatingEntity(
                        currentState.chat.ratingEntity.copy(isRatingMessageShowing = true)
                    )
                )
            }
        },
        negativeOnCLick = { sendEvent(SendAutoMessage) }
    )

    init {
        appMetricTracker.trackEvent(AppMetricEvent.TapOnlineConsultant)
        messageClient.isConsultantScreenActive = true
        launch {
            webimNetworkState()
                .catch { handleError(it) }
                .collectLatest {
                    reduceState { state.value.copy(connectState = it.toUi()) }
                }
        }
        launch {
            getMessages()
                .catch { handleError(it) }
                .map { it.messages.toUi() to it.hasNewMessage }
                .collectLatest { (messages, hasNewMessages) ->
                    val preparedMessages = messageTimeline.prepare(messageFromCache + messages)
                    reduceState {
                        state.value.copy(
                            chat = ChatUiEntity(
                                messages = preparedMessages,
                                ratingEntity = RatingUiEntity(
                                    isRatingMessageShowing = false,
                                    starsCount = null
                                )
                            ),
                            hasNewMessages = hasNewMessages
                        )
                    }
                }
        }
        launch {
            chatState.invoke()
                .filter { it == ChatState.Close }
                .drop(1)
                .collect {
                    resolutionFeedbackDialog.dialogOpen()
                }
        }
        launch {
            state
                .filter { it.connectState == ConnectState.Connect }
                .take(1)
                .collect {
                    launch {
                        val messagesFromHistory =
                            loadLastMessages(COUNT_MESSAGES_FROM_HISTORY).toUi()
                        if (messagesFromHistory.isNotEmpty()) {
                            val preparedHistory = messageTimeline.attachCachedFiles(messagesFromHistory)
                            messageFromCache = messagesFromHistory
                            reduceState {
                                state.value.copy(
                                    chat = state.value.chat.copy(
                                        messages = messageTimeline.decorate(
                                            preparedHistory + state.value.chat.messages
                                        ),
                                    ),
                                    hasNewMessages = true
                                )
                            }
                        }
                    }

                }
        }
    }

    fun setSheetVisibleState(isVisible: Boolean) {
        reduceState { state.value.copy(isOpenBottomSheet = isVisible) }
    }

    fun createNewPhoto(): Uri =
        photoCreate().also {
            reduceState {
                state.value.copy(
                    previewState = state.value.previewState.copy(
                        uriPhoto = it,
                        isFromCamera = true
                    )
                )
            }
        }

    override fun backClick() {
        messageClient.isConsultantScreenActive = false
        super.backClick()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        event.handleSessionState()
        event.handleRatingLifecycle()
    }

    override fun handleFragmentArguments(arguments: Bundle) {
        val user = WebimUser(
            id = arguments.getString(ConsultantFragment.USER_ID_KEY_EXTRA).orEmpty(),
            name = arguments.getString(ConsultantFragment.USER_NAME_KEY_EXTRA).orEmpty()
        )
        reduceState { state.value.copy(user = user) }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun handleUserAction(action: Action) {
        when (action) {
            AppAction.BackPressure -> {
                when {
                    state.value.previewState.isPhotoPreview -> reduceState {
                        state.value.copy(
                            previewState = state.value.previewState.copy(isPhotoPreview = false)
                        )
                    }

                    state.value.contextMenuEntity.isOpenContextMenu -> reduceState {
                        state.value.copy(
                            contextMenuEntity = state.value.contextMenuEntity.copy(isOpenContextMenu = false)
                        )
                    }

                    else -> backClick()
                }
            }

            ConsultantAction.StopRecordVoiceClick -> stopRecordVoice()
            ConsultantAction.DeleteRecordVoiceClick -> abortAudioRecordDialog.dialogOpen()
            is ConsultantAction.VoiceRecordClick -> {
                playback.pauseActive(::updateAudioState)
                startRecordVoice(action.permissionStatus)
            }
            ConsultantAction.CancelPhotoClick -> launch {
                state.value.previewState.uriPhoto?.let { deleteFile.invoke(it) }
            }

            is ConsultantAction.SendVoiceRecordClick -> sendVoiceMessage()

            is ConsultantAction.ChatMessageClick -> {
                reduceState {
                    state.value.copy(
                        previewState = state.value.previewState.copy(
                            uriPhoto = action.message.document?.uri ?: Uri.EMPTY,
                            urlPhoto = action.message.document?.url,
                            isPhotoPreview = true,
                            isFromCamera = false
                        )
                    )
                }
            }


            is ConsultantAction.FileSelected -> {
                postMessage(
                    SendMessageUseCase.Params(
                        uri = action.uri,
                        needCaching = true
                    )
                )
            }

            is ConsultantAction.PictureSelected ->
                postMessage(
                    SendMessageUseCase.Params(
                        uri = action.uri,
                        needCaching = true
                    )
                )

            is ConsultantAction.SendMessageClick -> {
                postMessage(
                    SendMessageUseCase.Params(
                        text = state.value.inputMessage
                    )
                )
                reduceState { state.value.copy(inputMessage = "") }
            }

            is ConsultantAction.SendAutoMessage ->
                postMessage(
                    SendMessageUseCase.Params(
                        text = action.text
                    )
                )

            ConsultantAction.PreviewBackPressure -> {
                if (state.value.previewState.isFromCamera) {
                    launch {
                        state.value.previewState.uriPhoto?.let { deleteFile.invoke(it) }
                    }
                }
                reduceState {
                    state.value.copy(
                        previewState = state.value.previewState.copy(isPhotoPreview = false)
                    )
                }
            }

            ConsultantAction.PictureSendClick -> {
                postMessage(
                    SendMessageUseCase.Params(
                        uri = state.value.previewState.uriPhoto
                    )
                )
                reduceState {
                    state.value.copy(
                        previewState = state.value.previewState.copy(isPhotoPreview = false)
                    )
                }
            }

            is ConsultantAction.VerifyFile -> handleFileState(action.message)
            is ConsultantAction.SelectedOperatorRate -> rateOperator(action.number)
            is ConsultantAction.CopyMessageClick -> handleCopingMessage(action.message.text.orEmpty())
            is ConsultantAction.DeleteMessageClick -> handleDeletingMessage(action.message)
            is ConsultantAction.SaveEditMessageClick -> handleSavingEditable()
            is ConsultantAction.CancelEditMessageClick -> hideEditMessage()
            is ConsultantAction.PlayAudioClick -> {
                if (state.value.recordGraphState.recordState == RecordState.Recording) {
                    abortAudioRecordDialog.dialogOpen()
                } else {
                    playback.play(viewModelScope, currentMessage(action.message), ::updateAudioState)
                }
            }
            is ConsultantAction.PauseAudioClick ->
                playback.pause(action.message.id, ::updateAudioState)
            is ConsultantAction.OnAudioTrackClick ->
                playback.seek(
                    viewModelScope,
                    currentMessage(action.message),
                    action.percentOfTrack,
                    ::updateAudioState
                )

            is ConsultantAction.OnDownIconClick -> sendEvent(ScrollToDown)
            is ConsultantAction.OnSwipeRefresh -> loadNextCachedMessages()
            is ConsultantAction.SelectPhotoClick -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    sendEvent(PhotoSelect)
                } else {
                    verifyPermission(
                        status = action.permissionStatus,
                        onGranted = { sendEvent(PhotoSelect) },
                        onShouldShow = { storageDialog.dialogOpen() },
                        onDenied = { sendEvent(PermissionEvent.Storage()) }
                    )
                }
            }
            is ConsultantAction.MakePhotoClick -> verifyPermission(
                status = action.permissionStatus,
                onGranted = { sendEvent(OpenCamera) },
                onShouldShow = { cameraDialog.dialogOpen() },
                onDenied = { sendEvent(PermissionEvent.Camera) }
            )
            is ConsultantAction.SelectFileClick -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    sendEvent(FileSelect)
                } else {
                    verifyPermission(
                        status = action.permissionStatus,
                        onGranted = { sendEvent(FileSelect) },
                        onShouldShow = { storageDialog.dialogOpen() },
                        onDenied = { sendEvent(PermissionEvent.Storage()) }
                    )
                }
            }
        }
    }

    override fun reduceStateByAction(
        currentState: ConsultantViewState,
        action: Action
    ): ConsultantViewState = ConsultantStateReducer.reduce(currentState, action)

    private fun handleFileState(message: MessageUiEntity) {
        fileController.handle(
            scope = viewModelScope,
            message = currentMessage(message),
            openFile = { uri, type ->
                val screen = if (type == MessageType.Document) {
                    Screens.ViewPdfScreen(uri)
                } else {
                    Screens.ViewVideoScreen(uri)
                }
                router.navigateTo(screen)
            },
            updateCache = ::updateFileCache
        )
    }

    private fun currentMessage(message: MessageUiEntity): MessageUiEntity =
        state.value.chat.messages.firstOrNull { it.id == message.id } ?: message

    private fun updateFileCache(messageId: String, cachingState: CachingState, uri: Uri?) {
        reduceState {
            state.value.copy(
                chat = state.value.chat.reduceChatState(
                    messageId = messageId,
                    cachingState = cachingState,
                    uri = uri
                )
            )
        }
    }

    private fun updateAudioState(messageId: String, isPlaying: Boolean, position: Int?) {
        reduceState {
            state.value.copy(
                chat = state.value.chat.reduceAudioState(
                    messageId = messageId,
                    isPlaying = isPlaying,
                    trackPosition = position
                )
            )
        }
    }

    private fun sendVoiceMessage() {
        val currentState = state.value
        if (currentState.recordGraphState.recordState != RecordState.Complete ||
            currentState.audioFileUri == Uri.EMPTY
        ) return

        postMessage(SendMessageUseCase.Params(uri = currentState.audioFileUri))
        resetRecordingState()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    private fun verifyPermission(
        status: PermissionStatus,
        onGranted: () -> Unit,
        onShouldShow: () -> Unit,
        onDenied: () -> Unit
    ) {
        when {
            status.isGranted -> onGranted()
            status.shouldShowRationale -> onShouldShow()
            else -> onDenied()
        }
    }

    private fun postMessage(params: SendMessageUseCase.Params) {
        launch {
            try {
                sendMessage(params)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                // fixme обработка ошибок
                Timber.d("EltaTag handle error: $e")
            }
        }
        if (state.value.chat.ratingEntity.isRatingMessageShowing) {
            hideRatingMessage()
        }
    }

    private fun deleteRecordVoice() {
        val currentState = state.value
        if (currentState.recordGraphState.recordState == RecordState.Recording) voiceRecorder.stop()
        resetRecordingState()
        val uri = currentState.audioFileUri
        if (uri != Uri.EMPTY) launch { deleteFile(uri) }
    }

    private fun stopRecordVoice() {
        if (state.value.recordGraphState.recordState != RecordState.Recording) return
        val uri = state.value.audioFileUri
        if (voiceRecorder.stop()) {
            reduceState {
                state.value.copy(
                    recordGraphState = state.value.recordGraphState.copy(
                        recordState = RecordState.Complete
                    )
                )
            }
        } else {
            resetRecordingState()
            if (uri != Uri.EMPTY) launch { deleteFile(uri) }
        }
    }

    private fun resetRecordingState() {
        reduceState {
            state.value.copy(
                audioFileUri = Uri.EMPTY,
                recordGraphState = RecordGraphState(
                    recordState = RecordState.Empty,
                    recordGraph = emptyList(),
                    duration = LocalTime.MIN
                )
            )
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    private fun startRecordVoice(permissionStatus: PermissionStatus) {
        verifyPermission(
            status = permissionStatus,
            onGranted = {
                sendEvent(MakeVibration)
                voiceRecorder.start(
                    scope = viewModelScope,
                    onStarted = { audioFile ->
                        reduceState {
                            state.value.copy(
                                recordGraphState = state.value.recordGraphState.copy(recordState = RecordState.Recording),
                                audioFileUri = audioFile
                            )
                        }
                    },
                    onLevel = ::appendRecordLevel
                )
            },
            onShouldShow = { audioDialog.dialogOpen() },
            onDenied = { sendEvent(PermissionEvent.RecordAudio()) }
        )
    }

    private fun loadNextCachedMessages() {
        launch {
            reduceState {
                state.value.copy(isLoadingNextMessagesPage = true)
            }
            val nextMessagesFromCache =
                loadNextCachedMessages(COUNT_MESSAGES_FROM_HISTORY).toUi()
            val preparedHistory = messageTimeline.attachCachedFiles(nextMessagesFromCache)
            // PullRefreshIndicator на версии Compose 1.3.1 имеет баг: индикатор загрузки не исчезает
            // мгновенной загрузки. С помощью делея можно решить его
            delay(1_000)
            reduceState {
                state.value.copy(
                    isLoadingNextMessagesPage = false,
                    chat = state.value.chat.copy(
                        messages = messageTimeline.decorate(
                            preparedHistory + state.value.chat.messages
                        )
                    )
                )
            }
            messageFromCache = messageFromCache + nextMessagesFromCache
        }
    }

    private fun rateOperator(rateNumber: Int) {
        reduceState {
            state.value.copy(
                chat = state.value.chat.updateRatingEntity(
                    state.value.chat.ratingEntity.copy(starsCount = rateNumber)
                )
            )
        }
        ratingScheduler.schedule(
            scope = viewModelScope,
            currentStars = { state.value.chat.ratingEntity.starsCount },
            onFinished = ::hideRatingMessage
        )
    }

    private fun handleCopingMessage(text: String) {
        copyText(text)
        reduceState {
            state.value.copy(contextMenuEntity = state.value.contextMenuEntity.resetMenu())
        }
    }

    private fun handleDeletingMessage(message: MessageUiEntity) {
        launch {
            deleteMessage(message.id)
            if (
                message.type != MessageType.Text
                && message.document != null
                && message.document.cachingState == CachingState.Cached
            ) {
                message.document.uri?.let { deleteFile(it) }
            }
            reduceState {
                state.value.copy(
                    contextMenuEntity = state.value.contextMenuEntity.resetMenu()
                )
            }
        }
    }

    private fun handleSavingEditable() {
        launch {
            editMessage(
                messageId = state.value.messageForEdit?.id.orEmpty(),
                newText = state.value.inputMessage
            )
            reduceState {
                state.value.copy(contextMenuEntity = state.value.contextMenuEntity.resetMenu())
            }
            hideEditMessage()
        }
    }

    private fun hideRatingMessage() {
        reduceState {
            state.value.copy(
                chat = state.value.chat.updateRatingEntity(
                    RatingUiEntity(
                        isRatingMessageShowing = false,
                        starsCount = null
                    )
                )
            )
        }
    }

    private fun hideEditMessage() {
        reduceState {
            state.value.copy(
                inputMessage = "",
                messageForEdit = null,
                isEditMessage = false
            )
        }
    }

    private fun appendRecordLevel(volumeLevel: Float) {
        reduceState {
            state.value.copy(
                recordGraphState = state.value.recordGraphState.copy(
                    recordGraph = state.value.recordGraphState.recordGraph + volumeLevel,
                    duration = state.value.recordGraphState.duration.plus(100L, ChronoUnit.MILLIS)
                )
            )
        }
    }

    private fun Lifecycle.Event.handleSessionState() {
        when (this) {
            Lifecycle.Event.ON_CREATE -> launch { webimSession.create(state.value.user) }
            Lifecycle.Event.ON_START -> {
                launch {
                    try {
                        webimSession.onResume()
                    } catch (e: IllegalStateException) {
                        webimSession.create(state.value.user)
                    }
                }
            }
            Lifecycle.Event.ON_RESUME -> webimSession.onResume()
            Lifecycle.Event.ON_PAUSE -> webimSession.onPause()
            Lifecycle.Event.ON_DESTROY -> webimSession.onDestroy()
            else -> Unit
        }
    }

    private fun Lifecycle.Event.handleRatingLifecycle() {
        when (this) {
            Lifecycle.Event.ON_STOP -> launch {
                ratingScheduler.flush(
                    currentStars = { state.value.chat.ratingEntity.starsCount },
                    onFinished = ::hideRatingMessage
                )
            }

            else -> Unit
        }
    }

    private fun ChatUiEntity.updateRatingEntity(
        rating: RatingUiEntity
    ): ChatUiEntity {
        return this.copy(ratingEntity = rating)
    }

    private fun ContextMenuUiEntity.resetMenu(): ContextMenuUiEntity = this.copy(
        isOpenContextMenu = false,
        selectedMessage = null
    )
}

private const val COUNT_MESSAGES_FROM_HISTORY = 10
