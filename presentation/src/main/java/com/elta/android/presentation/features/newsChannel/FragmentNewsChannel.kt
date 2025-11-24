package com.elta.android.presentation.features.newsChannel

import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
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
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.features.newsChannel.model.MakeVibration
import com.elta.android.presentation.features.newsChannel.model.NewsAction
import com.elta.android.presentation.features.newsChannel.model.OpenDownloadedFile
import com.elta.android.presentation.features.newsChannel.model.ScrollToDown
import com.elta.android.presentation.features.newsChannel.model.ScrollToTop
import com.elta.android.presentation.features.newsChannel.model.ShareImage
import com.elta.android.presentation.features.newsChannel.ui.components.NewsContent
import com.elta.android.presentation.features.newsChannel.viewModel.NewsViewModel
import com.elta.android.presentation.widgets.status.Visibility.Hide.delay
import com.elta.android.presentation.widgets.status.Visibility.HideWithDelay.delay
import com.elta.android.presentation.widgets.status.Visibility.Show.delay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class FragmentNewsChannel : BaseComposeFragment<NewsViewModel>() {

    override val viewModel: NewsViewModel by viewModels { viewModelFactory }

    companion object {
        fun newInstance() = FragmentNewsChannel()
    }

    override fun NewsViewModel.init() {}

    @OptIn(
        ExperimentalMaterialApi::class,
        ExperimentalPermissionsApi::class,
        ExperimentalComposeUiApi::class
    )

    @Composable
    override fun Content(viewModel: NewsViewModel) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val context = LocalContext.current
        val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val state = viewModel.state.collectAsState()
        val listState = rememberLazyListState()

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is ShareImage -> {
                        Log.d("FragmentNewsChannel", "Received ShareImage event, launching intent")
                        context.startActivity(
                            Intent.createChooser(
                                event.shareIntent,
                                "Поделиться изображением"
                            )
                        )
                    }

                    is OpenDownloadedFile -> {
                        Log.d(
                            "FragmentNewsChannel",
                            "Received OpenDownloadedFile event, opening file: ${event.fileUri}"
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(event.fileUri, "image/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Открыть изображение"
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("FragmentNewsChannel", "Error opening file: ${e.message}", e)
                            Toast.makeText(
                                context,
                                "Не удалось открыть файл: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is ScrollToDown -> {
                        Log.d(
                            "FragmentNewsChannel",
                            "Received ScrollToDown event, messages size: ${state.value.chat.messages.size}"
                        )
                        if (state.value.chat.messages.isNotEmpty()) {
                            launch {
                                delay(100)
                                listState.animateScrollToItem(index = state.value.chat.messages.size - 1)
                            }
                        } else {
                            Log.w(
                                "FragmentNewsChannel",
                                "Cannot scroll to down: message list is empty"
                            )
                        }
                    }

                    is ScrollToTop -> {
                        Log.d(
                            "FragmentNewsChannel",
                            "Received ScrollToTop event, messages size: ${state.value.chat.messages.size}"
                        )
                        if (state.value.chat.messages.isNotEmpty()) {
                            launch {
                                delay(100)
                                listState.animateScrollToItem(index = 0)
                            }
                        } else {
                            Log.w(
                                "FragmentNewsChannel",
                                "Cannot scroll to top: message list is empty"
                            )
                        }
                    }

                    is MakeVibration -> {
                        Log.d("FragmentNewsChannel", "Received MakeVibration event")
                        // Обработка вибрации
                    }
                }
            }
        }

        LaunchedEffect(key1 = state.value.isOpenBottomSheet) {
            if (state.value.isOpenBottomSheet) {
                sheetState.show()
            } else {
                sheetState.hide()
            }
        }

        LaunchedEffect(key1 = state.value.chat.messages) {
            Log.d(
                "FragmentNewsChannel",
                "Messages changed, size: ${state.value.chat.messages.size}"
            )
            if (state.value.chat.messages.isEmpty()) {
                delay(100)
                listState.scrollToItem(index = 0)
            }
        }

        NewsContent(
            state = state.value,
            sheetState = sheetState,
            listState = listState,
            onPictureArrowBackClick = {
                Log.d("FragmentNewsChannel", "Picture back clicked")
                viewModel sendAction AppAction.BackPressure
            },
            onTopBarBackClick = {
                Log.d("FragmentNewsChannel", "Top bar back clicked")
                viewModel sendAction AppAction.BackPressure
            },
            onMessageClick = { message ->
                Log.d("FragmentNewsChannel", "Message clicked: ${message.id}")
                viewModel sendAction NewsAction.ChatMessageClick(message)
            },
            onLongMessageClick = { message ->
                keyboardController?.hide()
                focusManager.clearFocus()
                Log.d("FragmentNewsChannel", "Long message clicked: ${message.id}")
                viewModel sendAction NewsAction.ChatMessageLongClick(message)
            },
            onCopyClick = { message ->
                Log.d("FragmentNewsChannel", "Copy clicked: ${message.id}")
                viewModel sendAction NewsAction.CopyMessageClick(message)
            },
            onDismissClick = {
                Log.d("FragmentNewsChannel", "Dismiss clicked")
                viewModel sendAction NewsAction.OnDismissContextMenu
            },
            onDocumentIconClick = { message ->
                Log.d("FragmentNewsChannel", "Document icon clicked: ${message.id}")
                viewModel sendAction NewsAction.VerifyFile(message)
            },
            onDownIconClick = {
                Log.d("FragmentNewsChannel", "Down icon clicked")
                viewModel sendAction NewsAction.OnDownIconClick
            },
            onSwipeRefresh = {
                Log.d("FragmentNewsChannel", "Swipe refresh")
                viewModel sendAction NewsAction.OnSwipeRefresh
            },
            onDownloadClick = { imageData ->
                viewModel sendAction NewsAction.DownloadImage(imageData)
            },
            onShareClick = { imageData ->
                viewModel sendAction NewsAction.ShareImage(imageData)
            },
            onScrollToTop = {
                Log.d("FragmentNewsChannel", "Scroll to top clicked")
                viewModel sendAction NewsAction.OnUpIconClick
            },
            onLoadNextPage = {
                Log.d("FragmentNewsChannel", "Load next page triggered")
                viewModel sendAction NewsAction.LoadNextPage
            }
        )
    }
}