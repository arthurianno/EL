package com.elta.android.presentation.features.consultant.viewmodel

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.elta.android.common.utils.EltaMessageClient
import com.elta.android.domain.common.usecase.AudioVolumeUseCase
import com.elta.android.domain.common.usecase.CopyTextUseCase
import com.elta.android.domain.common.usecase.FileDeleteUseCase
import com.elta.android.domain.common.usecase.GetTrackPositionUseCase
import com.elta.android.domain.common.usecase.PauseAudioTrackUseCase
import com.elta.android.domain.common.usecase.PhotoCreateUseCase
import com.elta.android.domain.common.usecase.PlayAudioTrackUseCase
import com.elta.android.domain.common.usecase.StartAudioRecordUseCase
import com.elta.android.domain.common.usecase.StopAudioRecordUseCase
import com.elta.android.domain.features.consultant.model.ChatState
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.usecase.DeleteMessageUseCase
import com.elta.android.domain.features.consultant.usecase.DownloadFileUseCase
import com.elta.android.domain.features.consultant.usecase.EditMessageUseCase
import com.elta.android.domain.features.consultant.usecase.GetAudioDurationUseCase
import com.elta.android.domain.features.consultant.usecase.GetCachedFilesUriUseCase
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
import com.elta.android.presentation.features.consultant.model.AudioState
import com.elta.android.presentation.features.consultant.model.BottomBarIconState
import com.elta.android.presentation.features.consultant.model.CachingState
import com.elta.android.presentation.features.consultant.model.ChatUiEntity
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.ConsultantViewState
import com.elta.android.presentation.features.consultant.model.ContextMenuUiEntity
import com.elta.android.presentation.features.consultant.model.FileSelect
import com.elta.android.presentation.features.consultant.model.MakeVibration
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageType.Companion.toContentType
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
import com.elta.android.presentation.features.consultant.viewmodel.analyzer.ChatMessagesAnalyzer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val getCachedFilesUri: GetCachedFilesUriUseCase,
    private val downloadFile: DownloadFileUseCase,
    private val photoCreate: PhotoCreateUseCase,
    private val startAudioRecord: StartAudioRecordUseCase,
    private val stopAudioRecord: StopAudioRecordUseCase,
    private val playAudioTrack: PlayAudioTrackUseCase,
    private val pauseAudioTrack: PauseAudioTrackUseCase,
    private val trackPositionFlow: GetTrackPositionUseCase,
    private val getVoiceDuration: GetAudioDurationUseCase,
    private val deleteFile: FileDeleteUseCase,
    private val audioFlow: AudioVolumeUseCase,
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
            bottomBarIconState = BottomBarIconState.SendMessage,
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

    private var volumeLevelSubscriber: Job? = null
    private var estimateOperatorSubscriber: Job? = null
    private var downloadFileSubscriber: Job? = null
    private var trackPositionSubscriber: Job? = null

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
                    val chatWithCache = messageFromCache + messages
                    val chatWithFilesFromCache = setupCachedFiles(chatWithCache)
                    val messagesWithCorner =
                        ChatMessagesAnalyzer.defineMessagesInSequence(chatWithFilesFromCache)
                    val messagesWithDate =
                        ChatMessagesAnalyzer.defineChangingDateMessages(messagesWithCorner)
                    reduceState {
                        state.value.copy(
                            chat = ChatUiEntity(
                                messages = messagesWithDate,
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
            state
                .collectLatest {
                    val iconState = when {
                        it.isEditMessage -> BottomBarIconState.SaveEdit
                        it.inputMessage.isEmpty() -> BottomBarIconState.StartRecord
                        else -> BottomBarIconState.SendMessage
                    }
                    reduceState {
                        state.value.copy(bottomBarIconState = iconState)
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
                            messageFromCache = messagesFromHistory
                            reduceState {
                                state.value.copy(
                                    chat = state.value.chat.copy(
                                        messages = messageFromCache + state.value.chat.messages,
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
        event.handleEstimateOperatorSubscriber()
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
                pauseAnyActiveTrack()
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
                    pauseAnyActiveTrack()
                    startPlayingTrack(action.message)
                }
            }
            is ConsultantAction.PauseAudioClick -> pausePlayingTrack(action.message.id)
            is ConsultantAction.OnAudioTrackClick ->
                seekToPositionAndPlay(action.message, action.percentOfTrack)

            is ConsultantAction.OnDownIconClick -> sendEvent(ScrollToDown)
            is ConsultantAction.OnSwipeRefresh -> loadNextCachedMessages()
        }
    }

    private fun pauseAnyActiveTrack() {
        state.value.chat.messages.forEach {
            if (it.audioState?.isPlaying == true) {
                pausePlayingTrack(it.id)
            }
        }
    }

    override fun reduceStateByAction(
        currentState: ConsultantViewState,
        action: Action
    ): ConsultantViewState =
        when (action) {
            is ConsultantAction.InputChanged -> currentState.copy(inputMessage = action.text)
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

                currentState.copy(isOpenBottomSheet = false)
            }

            is ConsultantAction.PhotoTaken -> currentState.copy(
                previewState = currentState.previewState.copy(
                    isPhotoPreview = true,
                    isFromCamera = true
                )
            )

            is ConsultantAction.MakePhotoClick -> {
                verifyPermission(
                    status = action.permissionStatus,
                    onGranted = { sendEvent(OpenCamera) },
                    onShouldShow = { cameraDialog.dialogOpen() },
                    onDenied = { sendEvent(PermissionEvent.Camera) }
                )

                currentState.copy(isOpenBottomSheet = false)
            }

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

                currentState.copy(isOpenBottomSheet = false)
            }

            ConsultantAction.FileClick -> currentState.copy(isOpenBottomSheet = true)
            is ConsultantAction.OnDismissContextMenu -> currentState.copy(
                contextMenuEntity = currentState.contextMenuEntity.copy(isOpenContextMenu = false)
            )

            is ConsultantAction.ChatMessageLongClick -> currentState.copy(
                contextMenuEntity = currentState.contextMenuEntity.copy(
                    isOpenContextMenu = true,
                    selectedMessage = action.message
                )
            )

            is ConsultantAction.EditMessageClick -> currentState.copy(
                inputMessage = action.message.text.orEmpty(),
                messageForEdit = action.message,
                isEditMessage = true,
                contextMenuEntity = state.value.contextMenuEntity.copy(
                    isOpenContextMenu = false,
                    selectedMessage = null
                )
            )

            else -> currentState
        }

    private fun handleFileState(message: MessageUiEntity) {
        val document = message.document
        val currentState = state.value

        when (message.document?.cachingState) {

            CachingState.Cached -> {
                val screen = if (message.type == MessageType.Document) {
                    Screens.ViewPdfScreen(document?.uri ?: Uri.EMPTY)
                } else {
                    Screens.ViewVideoScreen(document?.uri ?: Uri.EMPTY)
                }
                router.navigateTo(screen)
            }

            CachingState.Downloading -> {
                if (downloadFileSubscriber?.isActive == true) {
                    downloadFileSubscriber?.cancel()
                    downloadFileSubscriber = null
                }
                document?.uri?.let { deleteFile(it) }
                reduceState {
                    currentState.copy(
                        chat = currentState.chat.reduceChatState(
                            messageId = message.id,
                            cachingState = CachingState.NotCached
                        )
                    )
                }
            }

            else -> downloadFileSubscriber = launch { startDownloadFile(message) }
        }
    }

    private suspend fun startDownloadFile(message: MessageUiEntity) {
        val document = message.document

        reduceState {
            state.value.copy(
                chat = state.value.chat.reduceChatState(
                    messageId = message.id,
                    cachingState = CachingState.Downloading
                )
            )
        }

        val updatedChat = try {
            val downloadedFileUri = downloadFile(
                url = document?.url.orEmpty(),
                fileName = document?.fileName.orEmpty()
            )

            state.value.chat.reduceChatState(
                messageId = message.id,
                cachingState = CachingState.Cached,
                uri = downloadedFileUri
            )

        } catch (e: Exception) {
            state.value.chat.reduceChatState(
                messageId = message.id,
                cachingState = CachingState.NotCached,
            )
        }
        reduceState { state.value.copy(chat = updatedChat) }
    }

    private fun startPlayingTrack(message: MessageUiEntity) {
        val initialAudioStateMessages = state.value.chat.reduceAudioState(
            messageId = message.id,
            isPlaying = true,
            trackPosition = message.audioState?.trackPosition
        )
        reduceState {
            state.value.copy(chat = state.value.chat.copy(messages = initialAudioStateMessages.messages))
        }
        val position = message.audioState?.trackPosition.takeIf { it != null && it != 0 }
        val voiceMessageUri = message.document?.uri
        voiceMessageUri?.let {
            playAudioTrack(
                uri = it,
                trackPosition = position
            )
        }
        trackPositionSubscriber?.cancel()
        trackPositionSubscriber = null
        trackPositionSubscriber = launch { observeTrackPosition(message.id) }
    }

    private fun seekToPositionAndPlay(message: MessageUiEntity, pressAndTrackRelation: Float) {
        message.audioState?.let {
            if (message.audioState.isPlaying) pausePlayingTrack(message.id)

            val seekToPosition = it.duration * pressAndTrackRelation

            playAudioTrack(
                uri = message.document?.uri ?: Uri.EMPTY,
                trackPosition = seekToPosition.toInt()
            )
            trackPositionSubscriber = launch { observeTrackPosition(message.id) }
        }
    }

    private fun pausePlayingTrack(messageId: String) {
        trackPositionSubscriber?.cancel()
        trackPositionSubscriber = null
        val positionOnPause = pauseAudioTrack.invoke()

        reduceState {
            state.value.copy(
                chat = state.value.chat.reduceAudioState(
                    messageId = messageId,
                    isPlaying = false,
                    trackPosition = positionOnPause
                )
            )
        }
    }

    private fun sendVoiceMessage() {
        postMessage(
            SendMessageUseCase.Params(
                uri = state.value.audioFileUri
            )
        )
        reduceState {
            state.value.copy(
                recordGraphState = RecordGraphState(
                    recordState = RecordState.Empty,
                    recordGraph = emptyList(),
                    duration = LocalTime.MIN
                ),
                audioFileUri = Uri.EMPTY
            )
        }
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
        launch {
            if (state.value.recordGraphState.recordState == RecordState.Recording) stopRecordVoice()
            deleteFile.invoke(state.value.audioFileUri)
            reduceState {
                state.value.copy(
                    audioFileUri = Uri.EMPTY,
                    recordGraphState = RecordGraphState(
                        recordState = RecordState.Empty,
                        recordGraph = emptyList(),
                        duration = LocalTime.MIN
                    ),
                )
            }
        }
    }

    private fun stopRecordVoice() {
        stopAudioRecord()
        volumeLevelSubscriber?.cancel()
        volumeLevelSubscriber = null
        reduceState {
            state.value.copy(
                recordGraphState = state.value.recordGraphState.copy(
                    recordState = RecordState.Complete
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
                val audioFile = startAudioRecord()
                reduceState {
                    state.value.copy(
                        recordGraphState = state.value.recordGraphState.copy(
                            recordState = RecordState.Recording
                        ),
                        audioFileUri = audioFile
                    )
                }

                volumeLevelSubscriber = launch {
                    observeAudioFlow()
                }
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
            // PullRefreshIndicator на версии Compose 1.3.1 имеет баг: индикатор загрузки не исчезает
            // мгновенной загрузки. С помощью делея можно решить его
            delay(1_000)
            reduceState {
                state.value.copy(
                    isLoadingNextMessagesPage = false,
                    chat = state.value.chat.copy(messages = nextMessagesFromCache + state.value.chat.messages)
                )
            }
            messageFromCache = messageFromCache + nextMessagesFromCache
        }
    }

    private fun rateOperator(rateNumber: Int) {
        if (estimateOperatorSubscriber?.isCompleted == true) estimateOperatorSubscriber?.cancel()
        reduceState {
            state.value.copy(
                chat = state.value.chat.updateRatingEntity(
                    state.value.chat.ratingEntity.copy(starsCount = rateNumber)
                )
            )
        }
        if (
            estimateOperatorSubscriber?.isActive == false ||
            estimateOperatorSubscriber == null
        ) {
            estimateOperatorSubscriber = launch {
                delay(DELAY_BEFORE_SENDING_RATE)
                sendOperatorRate()
            }
        }
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

    private suspend fun observeAudioFlow() {
        audioFlow.invoke()
            .collect { volumeLevel ->
                reduceState {
                    state.value.copy(
                        recordGraphState = state.value.recordGraphState.copy(
                            recordGraph = state.value.recordGraphState.recordGraph + volumeLevel,
                            duration = state.value.recordGraphState.duration
                                .plus(100L, ChronoUnit.MILLIS)
                        )
                    )
                }
            }
    }

    private suspend fun observeTrackPosition(messageId: String) {
        trackPositionFlow.invoke()
            .distinctUntilChanged { old, new ->
                val isEndOfTrack = old == new
                if (isEndOfTrack) {
                    reduceState {
                        state.value.copy(
                            chat = state.value.chat.reduceAudioState(
                                messageId = messageId,
                                trackPosition = 0,
                                isPlaying = false
                            )
                        )
                    }
                }
                isEndOfTrack
            }
            .collect { position ->
                reduceState {
                    state.value.copy(
                        chat = state.value.chat.reduceAudioState(
                            messageId = messageId,
                            trackPosition = position,
                            isPlaying = true
                        )
                    )
                }
            }
    }

    private suspend fun sendOperatorRate() {
        state.value.chat.ratingEntity.starsCount?.let { starsCount ->
            sendRate(starsCount)
        }
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

    private fun Lifecycle.Event.handleEstimateOperatorSubscriber() {
        when (this) {
            Lifecycle.Event.ON_STOP -> launch {
                if (estimateOperatorSubscriber?.isActive == true) sendOperatorRate()
                estimateOperatorSubscriber?.cancel()
                estimateOperatorSubscriber = null
            }

            else -> Unit
        }
    }

    private fun setupCachedFiles(messages: List<MessageUiEntity>): List<MessageUiEntity> =
        messages.map { message ->
            if (message.document != null && message.type != MessageType.Text) {
                val uri = getCachedFilesUri(
                    fileName = message.document.fileName,
                    type = message.type.toContentType()
                )

                val voiceDuration = message.type.takeIf { it == MessageType.Voice }
                    ?.run {
                        getVoiceDuration(message.document.fileName)
                    }

                message.copy(
                    document = message.document.copy(
                        uri = uri,
                        cachingState = if (uri == null) CachingState.NotCached else CachingState.Cached
                    ),
                    audioState = if (voiceDuration != null) {
                        AudioState(
                            isPlaying = false,
                            duration = voiceDuration,
                            trackPosition = 0
                        )
                    } else null
                )
            } else message
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

private const val DELETE_FILE_DELAY_MILLIS = 500L
private const val RECORD_STEP_DELAY_MILLIS = 100L
private const val DELAY_BEFORE_SENDING_RATE = 10_000L
private const val COUNT_MESSAGES_FROM_HISTORY = 10
