package com.elta.android.presentation.features.consultant.ui.components

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.features.consultant.model.BottomBarIconState
import com.elta.android.presentation.features.consultant.model.ChatUiEntity
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ConsultantViewState
import com.elta.android.presentation.features.consultant.model.ContextMenuUiEntity
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import com.elta.android.presentation.features.consultant.model.PreviewState
import com.elta.android.presentation.features.consultant.model.RatingUiEntity
import com.elta.android.presentation.features.consultant.model.RecordGraphState
import com.elta.android.presentation.features.consultant.model.RecordState
import com.elta.android.presentation.features.consultant.ui.components.bottom.ConsultantBottomBar
import com.elta.android.presentation.features.consultant.ui.components.chat.ChatContent
import com.elta.android.presentation.features.consultant.ui.components.picture.PictureContent
import com.elta.android.presentation.features.consultant.ui.components.top.ConsultantTopBar
import com.elta.android.presentation.theme.GetLocalProperties
import org.threeten.bp.LocalTime

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConsultantContent(
    state: ConsultantViewState,
    sheetState: ModalBottomSheetState,
    listState: LazyListState,
    onPhotoSelectClick: () -> Unit = {},
    onTakingPhotoClick: () -> Unit = {},
    onFileSelectClick: () -> Unit = {},
    onRightBottomBarIconClick: (BottomBarIconState) -> Unit = {},
    onVoiceIconClick: (RecordState) -> Unit = {},
    onAttachFileClick: () -> Unit = {},
    onMessageChange: (String) -> Unit = {},
    deleteVoiceTrackClick: () -> Unit = {},
    onPictureArrowBackClick: () -> Unit = {},
    onPictureSendClick: () -> Unit = {},
    onTopBarBackClick: () -> Unit = {},
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onLongMessageClick: (MessageUiEntity) -> Unit = {},
    onCloseEditClick: () -> Unit = {},
    onDocumentIconClick: (MessageUiEntity) -> Unit = {},
    onRatingStarIconClick: (Int) -> Unit = {},
    onCopyClick: (MessageUiEntity) -> Unit = {},
    onEditClick: (MessageUiEntity) -> Unit = {},
    onDeleteClick: (MessageUiEntity) -> Unit = {},
    onDismissClick: () -> Unit = {},
    onVoiceMessageIconClick: (MessageUiEntity) -> Unit = {},
    onAudioTrackClick: (MessageUiEntity, Float) -> Unit = { _, _ -> },
    onSwipeRefresh: () -> Unit = {},
    onDownIconClick: () -> Unit = {}
) {

    GetLocalProperties { _, _, colors, shapes, _ ->
        if (state.previewState.isPhotoPreview) {
            PictureContent(
                state = state.previewState,
                onBackClick = onPictureArrowBackClick,
                onSendClick = onPictureSendClick
            )
        } else {
            ModalBottomSheetLayout(
                sheetContent = {
                    BottomSheetDialog(
                        onPhotoSelectClick = onPhotoSelectClick,
                        onTakingPhotoClick = onTakingPhotoClick,
                        onFileSelectClick = onFileSelectClick
                    )
                },
                sheetState = sheetState,
                sheetShape = shapes.sheet
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        ConsultantTopBar(
                            connectState = state.connectState,
                            onBackButtonClick = onTopBarBackClick
                        )
                    },
                    bottomBar = {
                        ConsultantBottomBar(
                            inputValue = state.inputMessage,
                            isEditMessage = state.isEditMessage,
                            messageForEdit = state.messageForEdit?.text.orEmpty(),
                            rightIconState = state.bottomBarIconState,
                            recordGraphState = state.recordGraphState,
                            onMessageChange = onMessageChange,
                            onFileClick = onAttachFileClick,
                            onRightIconClick = onRightBottomBarIconClick,
                            onVoiceIconClick = onVoiceIconClick,
                            deleteVoiceTrackClick = deleteVoiceTrackClick,
                            onCloseEditClick = onCloseEditClick
                        )
                    }
                ) { paddingValues ->
                    ChatContent(
                        modifier = Modifier.padding(paddingValues),
                        chat = state.chat,
                        connectState = state.connectState,
                        contentMenuEntity = state.contextMenuEntity,
                        hasNewMessages = state.hasNewMessages,
                        listState = listState,
                        isLoadingNextMessagesPage = state.isLoadingNextMessagesPage,
                        onRatingStarIconClick = onRatingStarIconClick,
                        onMessageClick = onMessageClick,
                        onLongMessageClick = onLongMessageClick,
                        onDocumentIconClick = onDocumentIconClick,
                        onVoiceMessageIconClick = onVoiceMessageIconClick,
                        onAudioTrackClick = onAudioTrackClick,
                        onCopyClick = onCopyClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick,
                        onDismissClick = onDismissClick,
                        onSwipeRefresh = onSwipeRefresh,
                        onDownIconClick = onDownIconClick
                    )
                }
                AnimatedVisibility(
                    visible = state.contextMenuEntity.isOpenContextMenu,
                    enter = fadeIn(animationSpec = tween(100)),
                    exit = fadeOut(animationSpec = tween(100))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = colors.black.copy(alpha = 0.7f))
                            .clickableWithNoRipple { onDismissClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomSheetDialog(
    onPhotoSelectClick: () -> Unit,
    onTakingPhotoClick: () -> Unit,
    onFileSelectClick: () -> Unit,
) {
    BottomSheetMenuItem(
        image = R.drawable.img_photo_select,
        text = R.string.consultant_bottom_sheet_item_select_photo,
        onItemClick = onPhotoSelectClick
    )
    BottomSheetMenuItem(
        image = R.drawable.img_camera,
        text = R.string.consultant_bottom_sheet_item_make_photo,
        onItemClick = onTakingPhotoClick
    )
    BottomSheetMenuItem(
        image = R.drawable.img_file,
        text = R.string.consultant_bottom_sheet_item_select_file,
        onItemClick = onFileSelectClick
    )
}

@Composable
private fun BottomSheetMenuItem(
    @DrawableRes image: Int,
    @StringRes text: Int,
    onItemClick: () -> Unit
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        Row(
            modifier = Modifier
                .clickable(onClick = onItemClick)
                .fillMaxWidth()
                .padding(dimens.consultantBottomSheetItemPadding)
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = null
            )
            HSpacerMedium()
            Text(text = stringResource(id = text))
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
private fun PreviewConsultantContent() {
    ConsultantContent(
        state = ConsultantViewState(
            inputMessage = "",
            messageForEdit = null,
            recordGraphState = RecordGraphState(
                recordState = RecordState.Empty,
                recordGraph = emptyList(),
                duration = LocalTime.MIN
            ),
            bottomBarIconState = BottomBarIconState.SendMessage,
            connectState = ConnectState.Connecting,
            chat = ChatUiEntity(
                messages = emptyList(),
                ratingEntity = RatingUiEntity(isRatingMessageShowing = false, starsCount = null)
            ),
            user = WebimUser(
                id = "",
                name = ""
            ),
            hasNewMessages = false,
            isOpenBottomSheet = false,
            audioFileUri = Uri.EMPTY,
            contextMenuEntity = ContextMenuUiEntity(
                isOpenContextMenu = false,
                selectedMessage = null
            ),
            isEditMessage = false,
            isLoadingNextMessagesPage = false,
            previewState = PreviewState(
                isPhotoPreview = true,
                isFromCamera = true,
                uriPhoto = null,
                urlPhoto = null
            ),
        ),
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden),
        listState = LazyListState()
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewBottomSheetDialog() {
    Column {
        BottomSheetDialog({}, {}, {})
    }
}
