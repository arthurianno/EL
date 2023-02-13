package com.elta.android.presentation.features.consultant.viewmodel

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.elta.android.domain.common.fileType
import com.elta.android.domain.common.getFileName
import com.elta.android.domain.common.model.FileType
import com.elta.android.domain.features.consultant.usecase.AudioRecordCreateUseCase
import com.elta.android.domain.features.consultant.usecase.FileCachingInteractor
import com.elta.android.domain.features.consultant.usecase.FileDeleteUseCase
import com.elta.android.domain.features.consultant.usecase.FileSendUseCase
import com.elta.android.domain.features.consultant.usecase.PhotoCreateUseCase
import com.elta.android.domain.features.consultant.usecase.WebimChatStateUseCase
import com.elta.android.domain.features.consultant.usecase.WebimGetMessagesUseCase
import com.elta.android.domain.features.consultant.usecase.WebimNetworkStateUseCase
import com.elta.android.domain.features.consultant.usecase.WebimSendMessageUseCase
import com.elta.android.domain.features.consultant.usecase.WebimSessionUseCase
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.ConsultantViewState
import com.elta.android.presentation.features.consultant.model.FileSelect
import com.elta.android.presentation.features.consultant.model.OpenCamera
import com.elta.android.presentation.features.consultant.model.PhotoSelect
import com.elta.android.presentation.features.consultant.model.toUi
import com.elta.android.presentation.features.consultant.model.toWebimUser
import com.elta.android.presentation.features.consultant.widgets.ConsultantBottomAppBarWidgetModel
import com.elta.android.presentation.features.consultant.widgets.ConsultantTopAppBarWidgetModel
import com.elta.android.presentation.features.consultant.widgets.PhotoPreviewBottomAppBarWidgetModel
import com.elta.android.presentation.features.consultant.widgets.PhotoPreviewTopAppBarWidgetModel
import com.elta.android.presentation.features.consultant.widgets.RecordState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.rx2.asFlow
import java.io.File
import javax.inject.Inject

private const val MAX_FILE_SIZE = 10000000L
private const val VOLUME_TIMER_DELAY = 200L
private const val VOLUME_MAX_LEVEL = 7500F

class ConsultantViewModel @Inject constructor(
    private val webimSession: WebimSessionUseCase,
    private val webimNetworkState: WebimNetworkStateUseCase,
    private val webimChatState: WebimChatStateUseCase,
    private val sendMessage: WebimSendMessageUseCase,
    private val getMessages: WebimGetMessagesUseCase,
    private val getProfile: GetProfileUseCase,
    private val photoCreate: PhotoCreateUseCase,
    private val fileDelete: FileDeleteUseCase,
    private val fileSend: FileSendUseCase,
    private val cache: FileCachingInteractor,
    private val audioFileCreate: AudioRecordCreateUseCase,
    context: Context
) : BaseViewModel<ConsultantViewState, Event, ConsultantAction>(), LifecycleEventObserver {
    private val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        MediaRecorder()
    }

    private var volumeLevelTicker: ReceiveChannel<Unit>? = null

    private var audioFile: File? = null

    override fun createInitState(): ConsultantViewState =
        ConsultantViewState(
            webimConnectState = ConnectState.Connecting,
            chat = emptyList(),
            previewPhoto = Uri.EMPTY,
            hasNewMessages = false,
            isOpenBottomSheet = false,
            isPhotoPreview = false
        )

    internal val consultantTopAppBar = ConsultantTopAppBarWidgetModel()
    internal val consultantBottomAppBar = ConsultantBottomAppBarWidgetModel()
    internal val previewTopAppBar = PhotoPreviewTopAppBarWidgetModel()
    internal val previewBottomAppBar = PhotoPreviewBottomAppBarWidgetModel()

    init {
        launch {
            webimNetworkState()
                .catch { handleError(it) }
                .collectLatest {
                    consultantTopAppBar.setConnectState(it.toUi())
                }
        }
        launch {
            getMessages()
                .catch { handleError(it) }
                .collectLatest {
                    reduceState {
                        state.value.copy(
                            chat = it.messages.toUi(),
                            hasNewMessages = it.hasNewMessage
                        )
                    }
                }
        }
    }

    override val widgets: List<BaseWidgetModel<*>> = listOf(
        consultantTopAppBar,
        consultantBottomAppBar,
        previewTopAppBar,
        previewBottomAppBar
    ).actionObserve()

    fun setSheetVisibleState(isVisible: Boolean) {
        reduceState { state.value.copy(isOpenBottomSheet = isVisible) }
    }

    fun createNewPhoto(): Uri =
        photoCreate().also {
            reduceState { state.value.copy(previewPhoto = it) }
        }

    fun showPhotoPreview() {
        reduceState { state.value.copy(isPhotoPreview = true) }
    }

    fun sendFile(uri: Uri) {
        launch {
            when (uri.fileType()) {
                FileType.Heif -> {}
                FileType.Jpg -> sendJpg(uri)
                FileType.Pdf -> sendPdf(uri)
                FileType.Png -> {}
                FileType.Voice -> {}
                else -> Unit
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    fun verifyPermission(
        status: PermissionStatus,
        onGrantedAction: ConsultantAction,
        onDeniedEvent: Event
    ) {
        if (status != PermissionStatus.Granted) {
            sendEvent(onDeniedEvent)
        } else {
            sendAction(onGrantedAction)
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun reduceStateByAction(
        currentState: ConsultantViewState,
        action: Action
    ): ConsultantViewState =
        when (action) {
            ConsultantAction.SearchClick -> currentState

            is ConsultantAction.SendMessageClick -> sendNewMessage(action.text)
            ConsultantAction.SelectPhotoClick -> {
                sendEvent(PhotoSelect())
                currentState.copy(isOpenBottomSheet = false)
            }

            ConsultantAction.MakePhotoClick -> {
                sendEvent(OpenCamera())
                currentState.copy(isOpenBottomSheet = false)
            }

            ConsultantAction.SelectFileClick -> {
                sendEvent(FileSelect())
                currentState.copy(isOpenBottomSheet = false)
            }

            ConsultantAction.FileClick -> currentState.copy(isOpenBottomSheet = true)
            ConsultantAction.PreviewBackPressure -> {
                launch {
                    fileDelete(currentState.previewPhoto)
                }
                currentState.copy(previewPhoto = Uri.EMPTY, isPhotoPreview = false)
            }

            ConsultantAction.PreviewSendClick -> {
                launch {
                    currentState.previewPhoto.lastPathSegment?.let { photoFileName ->
                        fileSend(photoFileName)
                            .catch { handleError(it) }
                            .collect()
                    }
                }
                currentState.copy(isPhotoPreview = false)
            }

            else -> {
                when (action) {
                    AppAction.BackPressure -> backClick()
                    ConsultantAction.StopRecVoiceClick -> stopRecordVoice()
                    ConsultantAction.DeleteRecVoiceClick -> deleteRecordVoice()
                    is ConsultantAction.StartRecVoiceClick -> startRecordVoice(action.permissionStatus)
                    is ConsultantAction.SendVoiceRecClick -> sendVoiceRecord()
                }
                currentState
            }
        }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                launch {
                    getProfile.execute()
                        .toObservable()
                        .asFlow()
                        .map { it.toWebimUser() }
                        .catch { handleError(it) }
                        .collectLatest { webimSession.create(it) }
                }
            }

            Lifecycle.Event.ON_RESUME -> {
                webimSession.onResume()
            }

            Lifecycle.Event.ON_PAUSE -> webimSession.onPause()
            Lifecycle.Event.ON_DESTROY -> webimSession.onDestroy()
            else -> Unit
        }
    }

    private fun sendVoiceRecord() {
        audioFile = null
        consultantBottomAppBar.sendRecord()
    }

    private fun deleteRecordVoice() {
        if (consultantBottomAppBar.state.value.recordState == RecordState.Recording) {
            stopRecordVoice()
        }
        consultantBottomAppBar.deleteRecord()
        consultantBottomAppBar.clearRecordState()
        launch {
            audioFile?.toUri()?.let { fileDelete(it) }
            audioFile = null
        }
    }


    private fun stopRecordVoice() {
        mediaRecorder.stop()
        mediaRecorder.reset()
        volumeLevelTicker?.cancel()
        volumeLevelTicker = null
        consultantBottomAppBar.stopRecord()
    }

    @OptIn(ExperimentalPermissionsApi::class, ObsoleteCoroutinesApi::class)
    private fun startRecordVoice(permissionStatus: PermissionStatus) {
        if (permissionStatus.isGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFile = audioFileCreate()
                with(mediaRecorder) {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setMaxFileSize(MAX_FILE_SIZE)
                    setOutputFile(audioFile)
                    prepare()
                    start()
                }
                launch {
                    volumeLevelTicker = ticker(
                        delayMillis = VOLUME_TIMER_DELAY,
                        initialDelayMillis = VOLUME_TIMER_DELAY
                    )
                    volumeLevelTicker?.let { ticker ->
                        ticker.receiveAsFlow()
                            .filterNotNull()
                            .catch { handleError(it) }
                            .onEach {
                                with(consultantBottomAppBar) {
                                    addValueToGraph(mediaRecorder.maxAmplitude / VOLUME_MAX_LEVEL)
                                    addRecordTime(VOLUME_TIMER_DELAY)
                                }
                            }
                            .collect()
                    }
                }
                consultantBottomAppBar.startRecord()
            }
        } else {
            sendEvent(PermissionEvent.RecordAudio())
        }
    }

    private suspend fun sendPdf(uri: Uri) {
        uri.getFileName()?.let { selectFileName ->
            cache.savePgf(selectFileName, uri).lastPathSegment?.let { cacheFileName ->
                fileSend(cacheFileName)
                    .catch { handleError(it) }
                    .collect()
            }
        }
    }

    private suspend fun sendJpg(uri: Uri) {
        uri.getFileName()?.let { selectFileName ->
            cache.saveJpg(selectFileName, uri).lastPathSegment?.let { cacheFileName ->
                fileSend(cacheFileName)
                    .catch { handleError(it) }
                    .collect()
            }
        }
    }

    private fun sendNewMessage(text: String): ConsultantViewState {
        launch {
            sendMessage(text)
        }
        consultantBottomAppBar.setText(null)
        return state.value.copy()
    }
}
