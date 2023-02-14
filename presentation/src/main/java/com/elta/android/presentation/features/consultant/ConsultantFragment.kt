package com.elta.android.presentation.features.consultant

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.viewModels
import coil.compose.AsyncImage
import com.elta.android.domain.features.consultant.model.WebimOwner
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.common.ShowToast
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.FileSelect
import com.elta.android.presentation.features.consultant.model.OpenCamera
import com.elta.android.presentation.features.consultant.model.PhotoSelect
import com.elta.android.presentation.features.consultant.viewmodel.ConsultantViewModel
import com.elta.android.presentation.features.consultant.widgets.ChatMessage
import com.elta.android.presentation.features.consultant.widgets.ChatMessageWidgetModel
import com.elta.android.presentation.features.consultant.widgets.ConsultantBottomAppBar
import com.elta.android.presentation.features.consultant.widgets.ConsultantTopAppBar
import com.elta.android.presentation.features.consultant.widgets.PhotoPreviewBottomAppBar
import com.elta.android.presentation.features.consultant.widgets.PhotoPreviewTopAppBar
import com.elta.android.presentation.theme.GetLocalProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

private const val IMAGE = "image/*"
private const val DOCUMENT = "application/pdf"

class ConsultantFragment : BaseComposeFragment<ConsultantViewModel>() {
    companion object {
        fun newInstance(): ConsultantFragment = ConsultantFragment()
    }

    override val viewModel: ConsultantViewModel by viewModels { viewModelFactory }

    private val makePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            viewModel.showPhotoPreview()
        }
    }
    private val getPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> viewModel.sendFile(uri) }
    }

    private val getDocument = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> viewModel.sendFile(uri) }
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
    @Composable
    override fun Content(viewModel: ConsultantViewModel) {
        val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val event = viewModel.event.collectAsState(initial = null)
        val state = viewModel.state.collectAsState()
        val storageAccessPermission =
            rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val recordAudionPermission =
            rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
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
        LaunchedEffect(key1 = event.value) {
            when (event.value) {
                is OpenCamera -> makePhoto.launch(viewModel.createNewPhoto())
                is PhotoSelect -> getPhoto.launch(IMAGE)
                is FileSelect -> getDocument.launch(DOCUMENT)
                is PermissionEvent.Storage -> storageAccessPermission.launchPermissionRequest()
                is PermissionEvent.RecordAudio -> recordAudionPermission.launchPermissionRequest()
                is ShowToast -> Toast.makeText(
                    requireContext(),
                    (event.value as ShowToast).text,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        GetLocalProperties { _, _, colors, shapes, _ ->
            if (state.value.isPhotoPreview) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(color = colors.black)
                        .systemBarsPadding()
                ) {
                    PhotoPreviewTopAppBar(widgetModel = viewModel.previewTopAppBar)
                    PreviewPhoto(state.value.previewPhoto)
                    PhotoPreviewBottomAppBar(widgetModel = viewModel.previewBottomAppBar)
                }
            } else {
                ModalBottomSheetLayout(
                    sheetContent = { BottomSheetDialog() },
                    sheetState = sheetState,
                    sheetShape = shapes.sheet
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ConsultantTopAppBar(widgetModel = viewModel.consultantTopAppBar)
                        ChatContent()
                        ConsultantBottomAppBar(widgetModel = viewModel.consultantBottomAppBar)
                    }
                }
            }
        }
    }

    @Composable
    private fun ColumnScope.PreviewPhoto(uri: Uri) {
        GetLocalProperties { dimens, _, colors, _, _ ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(color = colors.black)
                    .padding(dimens.photoPreviewContentPadding)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    @Composable
    private fun BottomSheetDialog() {
        BottomSheetMenuItem(
            image = R.drawable.img_photo_select,
            text = R.string.consultant_bottom_sheet_item_select_photo,
            action = ConsultantAction.SelectPhotoClick
        )
        BottomSheetMenuItem(
            image = R.drawable.img_camera,
            text = R.string.consultant_bottom_sheet_item_make_photo,
            action = ConsultantAction.MakePhotoClick
        )
        BottomSheetMenuItem(
            image = R.drawable.img_file,
            text = R.string.consultant_bottom_sheet_item_select_file,
            action = ConsultantAction.SelectFileClick
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun BottomSheetMenuItem(
        @DrawableRes image: Int,
        @StringRes text: Int,
        action: ConsultantAction
    ) {
        GetLocalProperties { dimens, _, _, _, _ ->
            val storageAccessPermission =
                rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
            Row(
                modifier = Modifier
                    .clickable {
                        viewModel.verifyPermission(
                            status = storageAccessPermission.status,
                            onGrantedAction = action,
                            onDeniedEvent = PermissionEvent.Storage()
                        )
                    }
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

    @Composable
    private fun ColumnScope.ChatContent() {
        GetLocalProperties { dimens, _, colors, _, _ ->
            val state = viewModel.state.collectAsState()
            val hasNewMessages = state.value.hasNewMessages
            val listState = rememberLazyListState()
            Box(
                modifier = Modifier.Companion
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.value.chat.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.consultant_chat_empty_text),
                        color = colors.shadeBlack2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LaunchedEffect(key1 = hasNewMessages) {
                        if (hasNewMessages) {
                            listState.scrollToItem(state.value.chat.size)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        verticalArrangement = Arrangement.spacedBy(dimens.mediumDim),
                        state = listState,
                        contentPadding = dimens.chatPadding
                    ) {
                        items(items = state.value.chat) { message ->
                            val chatMessageWidgetModel = ChatMessageWidgetModel().apply {
                                attach(viewModel)
                                setMessage(message)
                            }
                            when (message.owner) {
                                WebimOwner.User -> UserMessage(chatMessageWidgetModel)
                                WebimOwner.Operator -> OperatorMessage(chatMessageWidgetModel)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun UserMessage(widgetModel: ChatMessageWidgetModel) {
        GetLocalProperties { _, _, colors, _, _ ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                ChatMessage(widgetModel)
            }
        }
    }

    @Composable
    private fun OperatorMessage(widgetModel: ChatMessageWidgetModel) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Image(
                    painter = painterResource(id = R.drawable.img_round_elta),
                    contentDescription = null
                )
                HSpacerSmall()
                ChatMessage(widgetModel)
            }
        }
    }
}
