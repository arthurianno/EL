package com.elta.android.presentation.features.consultant.viewmodel

import android.net.Uri
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.elta.android.domain.common.fileType
import com.elta.android.domain.common.getFileName
import com.elta.android.domain.common.model.FileType
import com.elta.android.domain.common.usecase.CachedJpgUseCase
import com.elta.android.domain.common.usecase.CachedPdfUseCase
import com.elta.android.domain.common.usecase.FileDeleteUseCase
import com.elta.android.domain.common.usecase.PhotoCreateUseCase
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.usecase.FileSendUseCase
import com.elta.android.domain.features.consultant.usecase.WebimGetMessagesUseCase
import com.elta.android.domain.features.consultant.usecase.WebimNetworkStateUseCase
import com.elta.android.domain.features.consultant.usecase.WebimSendMessageUseCase
import com.elta.android.domain.features.consultant.usecase.WebimSessionUseCase
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.common.ShowToast
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.media.AudioRecorder
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.ConsultantViewState
import com.elta.android.presentation.features.consultant.model.FileSelect
import com.elta.android.presentation.features.consultant.model.OpenCamera
import com.elta.android.presentation.features.consultant.model.PhotoSelect
import com.elta.android.presentation.features.consultant.model.toUi
import com.elta.android.presentation.features.consultant.widgets.ConsultantBottomAppBarWidgetModel
import com.elta.android.presentation.features.consultant.widgets.ConsultantTopAppBarWidgetModel
import com.elta.android.presentation.features.consultant.widgets.PhotoPreviewBottomAppBarWidgetModel
import com.elta.android.presentation.features.consultant.widgets.PhotoPreviewTopAppBarWidgetModel
import com.elta.android.presentation.features.consultant.widgets.RecordState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val DELETE_FILE_DELAY_MILLIS = 500L
private const val RECORD_STEP_DELAY_MILLIS = 100L

class ConsultantViewModel @Inject constructor(
    private val webimSession: WebimSessionUseCase,
    private val webimNetworkState: WebimNetworkStateUseCase,
    private val sendMessage: WebimSendMessageUseCase,
    private val getMessages: WebimGetMessagesUseCase,
    private val photoCreate: PhotoCreateUseCase,
    private val fileDelete: FileDeleteUseCase,
    private val fileSend: FileSendUseCase,
    private val savePdf: CachedPdfUseCase,
    private val saveJpg: CachedJpgUseCase,
    private val audioRecorder: AudioRecorder
) : BaseViewModel<ConsultantViewState>(), LifecycleEventObserver {

    override fun createInitState(): ConsultantViewState =
        ConsultantViewState(
            webimConnectState = ConnectState.Connecting,
            chat = emptyList(),
            previewPhoto = Uri.EMPTY,
            user = WebimUser(id = "", name = ""),
            hasNewMessages = false,
            isOpenBottomSheet = false,
            isPhotoPreview = false
        )

    private var recordFileUri: Uri? = null

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

    fun setWebimUser(user: WebimUser) {
        reduceState { state.value.copy(user = user) }
    }

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
    override fun handleUserAction(action: Action) {
        when (action) {
            AppAction.BackPressure -> backClick()
            ConsultantAction.StopRecVoiceClick -> stopRecordVoice()
            ConsultantAction.DeleteRecVoiceClick -> deleteRecordVoice()
            is ConsultantAction.StartRecVoiceClick -> startRecordVoice(action.permissionStatus)
            is ConsultantAction.SendVoiceRecClick -> sendVoiceRecord()
            is ConsultantAction.ChatMessageClick -> sendEvent(ShowToast(action.message.text))
        }
    }
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

            else -> currentState
        }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> webimSession.create(state.value.user)
            Lifecycle.Event.ON_START -> webimSession.onResume()
            Lifecycle.Event.ON_RESUME -> webimSession.onResume()
            Lifecycle.Event.ON_PAUSE -> webimSession.onPause()
            Lifecycle.Event.ON_DESTROY -> webimSession.onDestroy()
            else -> Unit
        }
    }

    private fun sendVoiceRecord() {
        recordFileUri?.lastPathSegment?.let { fileSend(it) }
        consultantBottomAppBar.sendRecord()
    }

    private fun deleteRecordVoice() {
        if (consultantBottomAppBar.state.value.recordState == RecordState.Recording) {
            stopRecordVoice()
        }
        launch {
            consultantBottomAppBar.deleteRecord()
            delay(DELETE_FILE_DELAY_MILLIS)
            consultantBottomAppBar.clearRecordState()
            audioRecorder.deleteFile()
        }
    }

    private fun stopRecordVoice() {
        recordFileUri = audioRecorder.stop()
        consultantBottomAppBar.stopRecord()
    }

    @OptIn(ExperimentalPermissionsApi::class, ObsoleteCoroutinesApi::class)
    private fun startRecordVoice(permissionStatus: PermissionStatus) {
        if (permissionStatus.isGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                launch {
                    audioRecorder.start(stepMillis = RECORD_STEP_DELAY_MILLIS)
                    audioRecorder.volumeFlow
                        .catch { handleError(it) }
                        .onEach {
                            with(consultantBottomAppBar) {
                                addValueToGraph(it)
                                addRecordTime(RECORD_STEP_DELAY_MILLIS)
                            }
                        }
                        .collect()
                }
                consultantBottomAppBar.startRecord()
            }
        } else {
            sendEvent(PermissionEvent.RecordAudio())
        }
    }

    private suspend fun sendPdf(uri: Uri) {
        uri.getFileName()?.let { selectFileName ->
            savePdf(selectFileName, uri).lastPathSegment?.let { cacheFileName ->
                fileSend(cacheFileName)
                    .catch { handleError(it) }
                    .collect()
            }
        }
    }

    private suspend fun sendJpg(uri: Uri) {
        uri.getFileName()?.let { selectFileName ->
            saveJpg(selectFileName, uri).lastPathSegment?.let { cacheFileName ->
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
