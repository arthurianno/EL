package com.elta.android.presentation.features.consultant.ui

import android.os.Bundle
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.viewModels
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.ScrollToDown
import com.elta.android.presentation.features.consultant.ui.components.ConsultantContent
import com.elta.android.presentation.features.consultant.viewmodel.ConsultantViewModel
import kotlinx.coroutines.flow.collectLatest

class ConsultantFragment : BaseComposeFragment<ConsultantViewModel>() {
    companion object {
        fun newInstance(userId: String = "", userName: String = ""): ConsultantFragment = ConsultantFragment()
    }

    override val viewModel: ConsultantViewModel by viewModels { viewModelFactory }

    override fun ConsultantViewModel.init() {
        // Инициализация диалогов больше не требуется, так как нет запросов пермишенов
    }

    @Composable
    override fun Dialogs(viewModel: ConsultantViewModel) {
        // Диалоги отсутствуют в локальном боте
    }

    @Composable
    override fun Content(viewModel: ConsultantViewModel) {
        val state = viewModel.state.collectAsState()
        val listState = rememberLazyListState()

        LaunchedEffect(key1 = Unit) {
            viewModel.event.collectLatest {
                when (it) {
                    is ScrollToDown -> {
                        val messagesCount = state.value.chatMessages.size
                        if (messagesCount > 0) {
                            listState.animateScrollToItem(messagesCount - 1)
                        }
                    }
                }
            }
        }

        ConsultantContent(
            state = state.value,
            listState = listState,
            onOptionClick = { option ->
                viewModel sendAction ConsultantAction.OptionClick(option)
            },
            onSendTextClick = { text ->
                viewModel sendAction ConsultantAction.SendTextClick(text)
            },
            onTopBarBackClick = {
                viewModel.backClick()
            },
            onCopyClick = { text ->
                viewModel sendAction ConsultantAction.CopyMessageClick(text)
            },
            onResetClick = {
                viewModel sendAction ConsultantAction.ResetClick
            }
        )
    }
}
