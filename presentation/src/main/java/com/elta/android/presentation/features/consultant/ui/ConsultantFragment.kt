package com.elta.android.presentation.features.consultant.ui

import android.Manifest
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import com.elta.android.domain.common.model.MimeType
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.consultant.model.BottomBarIconState
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.FileSelect
import com.elta.android.presentation.features.consultant.model.MakeVibration
import com.elta.android.presentation.features.consultant.model.OpenCamera
import com.elta.android.presentation.features.consultant.model.OpenSettings
import com.elta.android.presentation.features.consultant.model.PhotoSelect
import com.elta.android.presentation.features.consultant.model.RecordState
import com.elta.android.presentation.features.consultant.model.ScrollToDown
import com.elta.android.presentation.features.consultant.model.SendAutoMessage
import com.elta.android.presentation.features.consultant.ui.components.ConsultantContent
import com.elta.android.presentation.features.consultant.viewmodel.ConsultantViewModel
import com.elta.android.presentation.utils.openSettingsIntent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.collectLatest

class ConsultantFragment : BaseComposeFragment<ConsultantViewModel>() {
    companion object {
        const val USER_NAME_KEY_EXTRA = "user_name"
        const val USER_ID_KEY_EXTRA = "user_id"
        fun newInstance(userId: String, userName: String): ConsultantFragment =
            ConsultantFragment().apply {
                arguments = Bundle().apply {
                    putString(USER_ID_KEY_EXTRA, userId)
                    putString(USER_NAME_KEY_EXTRA, userName)
                }
            }
    }

    override val viewModel: ConsultantViewModel by viewModels { viewModelFactory }

    override fun ConsultantViewModel.init() {
        cameraDialog.initDialog(
            title = getString(R.string.consultant_dialog_camera),
            message = "",
            positiveButtonText = getString(R.string.settings_dialog_positive),
            negativeButtonText = getString(R.string.ok)
        )
        storageDialog.initDialog(
            title = getString(R.string.consultant_dialog_photo),
            message = "",
            positiveButtonText = getString(R.string.settings_dialog_positive),
            negativeButtonText = getString(R.string.ok)
        )

        audioDialog.initDialog(
            title = getString(R.string.consultant_dialog_microphone),
            message = "",
            positiveButtonText = getString(R.string.settings_dialog_positive),
            negativeButtonText = getString(R.string.ok)
        )
        abortAudioRecordDialog.initDialog(
            title = getString(R.string.consultant_dialog_cancel_record),
            message = "",
            positiveButtonText = getString(R.string.ok),
            negativeButtonText = getString(R.string.cancel_text)
        )
        resolutionFeedbackDialog.initDialog(
            message = getString(R.string.consultant_resolution_feedback),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )
    }

    @Composable
    override fun Dialogs(viewModel: ConsultantViewModel) {
        BaseDialog(widgetModel = viewModel.cameraDialog)
        BaseDialog(widgetModel = viewModel.storageDialog)
        BaseDialog(widgetModel = viewModel.audioDialog)
        BaseDialog(widgetModel = viewModel.resolutionFeedbackDialog)
        BaseDialog(widgetModel = viewModel.abortAudioRecordDialog)
    }

    @OptIn(
        ExperimentalMaterialApi::class,
        ExperimentalPermissionsApi::class,
        ExperimentalComposeUiApi::class
    )
    @Composable
    override fun Content(viewModel: ConsultantViewModel) {
        val vibrator = getSystemService(LocalContext.current, Vibrator::class.java)
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val state = viewModel.state.collectAsState()
        val listState = rememberLazyListState()
        val getFile =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let { viewModel sendAction ConsultantAction.FileSelected(it) }
            }

        val getPicture =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let { viewModel sendAction ConsultantAction.PictureSelected(it) }
            }

        val makePhoto =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
                if (pictureTaken) viewModel sendAction ConsultantAction.PhotoTaken
                else viewModel sendAction ConsultantAction.CancelPhotoClick
            }

        val recordAudioPermission =
            rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
        val cameraPermission =
            rememberPermissionState(permission = Manifest.permission.CAMERA)
        // fixme: мне кажется лаунч эффекты здесь использованы не по назначению
        LaunchedEffect(key1 = sheetState.currentValue) {
            viewModel.setSheetVisibleState(sheetState.isVisible)
        }
        LaunchedEffect(key1 = state.value.isOpenBottomSheet) {
            if (state.value.isOpenBottomSheet) {
                sheetState.show()
            } else {
                sheetState.hide()
            }
        }
        LaunchedEffect(key1 = Unit) {
            viewModel.event.collectLatest {
                when (it) {
                    is OpenSettings -> openSettingsIntent(requireContext())
                    is OpenCamera -> makePhoto.launch(viewModel.createNewPhoto())
                    is PhotoSelect -> getPicture.launch(MimeType.Image.mimeName)
                    is FileSelect -> getFile.launch(
                        arrayOf(
                            MimeType.Image.mimeName,
                            MimeType.DocumentPdf.mimeName,
                            MimeType.Video.mimeName
                        )
                    )

                    is ScrollToDown -> listState.animateScrollToItem(state.value.chat.messages.size)

                    is MakeVibration -> vibrator?.let {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            it.vibrate(
                                VibrationEffect.createOneShot(
                                    100,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        } else it.vibrate(100)
                    }

                    is SendAutoMessage -> viewModel sendAction ConsultantAction.SendAutoMessage(
                        getString(R.string.consultant_auto_message_question_not_resolved)
                    )

                    is PermissionEvent.RecordAudio -> recordAudioPermission.launchPermissionRequest()
                    is PermissionEvent.Camera -> cameraPermission.launchPermissionRequest()
                    else -> {}
                }
            }
        }

        ConsultantContent(
            state = state.value,
            sheetState = sheetState,
            listState = listState,
            onPhotoSelectClick = {
                viewModel sendAction ConsultantAction.SelectPhotoClick
            },
            onTakingPhotoClick = {
                viewModel sendAction ConsultantAction.MakePhotoClick(
                    cameraPermission.status
                )
            },
            onFileSelectClick = {
                viewModel sendAction ConsultantAction.SelectFileClick
            },
            onRightBottomBarIconClick = { iconState ->
                val action =
                    when (iconState) {
                        BottomBarIconState.SendMessage -> ConsultantAction.SendMessageClick
                        BottomBarIconState.StartRecord -> ConsultantAction.VoiceRecordClick(
                            recordAudioPermission.status
                        )

                        BottomBarIconState.SaveEdit -> ConsultantAction.SaveEditMessageClick
                    }

                viewModel sendAction action
            },
            onMessageChange = {
                viewModel sendAction ConsultantAction.InputChanged(it)
            },
            onVoiceIconClick = { recordState ->
                val action = when (recordState) {
                    RecordState.Recording -> ConsultantAction.StopRecordVoiceClick
                    else -> ConsultantAction.SendVoiceRecordClick
                }
                viewModel sendAction action
            },
            deleteVoiceTrackClick = { viewModel sendAction ConsultantAction.DeleteRecordVoiceClick },
            onAttachFileClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                viewModel sendAction ConsultantAction.FileClick
            },
            onPictureArrowBackClick = { viewModel sendAction ConsultantAction.PreviewBackPressure },
            onPictureSendClick = { viewModel sendAction ConsultantAction.PictureSendClick },
            onTopBarBackClick = { viewModel sendAction AppAction.BackPressure },
            onMessageClick = { message ->
                viewModel sendAction ConsultantAction.ChatMessageClick(message)
            },
            onLongMessageClick = { message ->
                keyboardController?.hide()
                focusManager.clearFocus()
                viewModel sendAction ConsultantAction.ChatMessageLongClick(message)
            },
            onCloseEditClick = {
                viewModel sendAction ConsultantAction.CancelEditMessageClick
            },
            onRatingStarIconClick = { number ->
                viewModel sendAction ConsultantAction.SelectedOperatorRate(number)
            },
            onCopyClick = { message ->
                viewModel sendAction ConsultantAction.CopyMessageClick(message)
            },
            onEditClick = { message ->
                viewModel sendAction ConsultantAction.EditMessageClick(message)
            },
            onDeleteClick = { message ->
                viewModel sendAction ConsultantAction.DeleteMessageClick(message)
            },
            onDismissClick = {
                viewModel sendAction ConsultantAction.OnDismissContextMenu
            },
            onDocumentIconClick = {
                viewModel sendAction ConsultantAction.VerifyFile(it)
            },
            onVoiceMessageIconClick = { message ->
                val action =
                    if (message.audioState?.isPlaying == true) {
                        ConsultantAction.PauseAudioClick(message)
                    } else ConsultantAction.PlayAudioClick(message)

                viewModel sendAction action
            },
            onAudioTrackClick = { message, percentOfTrackSize ->
                viewModel sendAction ConsultantAction.OnAudioTrackClick(message, percentOfTrackSize)
            },
            onDownIconClick = {
                viewModel sendAction ConsultantAction.OnDownIconClick
            },
            onSwipeRefresh = {
                viewModel sendAction ConsultantAction.OnSwipeRefresh
            }
        )
    }
}
