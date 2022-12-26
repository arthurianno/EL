package com.elta.android.presentation.features.consultant

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.viewModels
import com.elta.android.domain.features.consultant.model.WebimOwner
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.features.consultant.model.ChatUiEntity
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.viewmodel.ConsultantViewModel
import com.elta.android.presentation.features.consultant.widgets.ConsultantBottomAppBar
import com.elta.android.presentation.features.consultant.widgets.ConsultantTopAppBar
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalColors

class ConsultantFragment : BaseComposeFragment<ConsultantViewModel>() {

    override val viewModel: ConsultantViewModel by viewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)
    }

    @Composable
    override fun Content(viewModel: ConsultantViewModel) {
        val state = viewModel.state.collectAsState()
        GetLocalProperties { dimens, brash, colors, shapes, types ->
            Column(modifier = Modifier.fillMaxSize()) {
                ConsultantTopAppBar(widgetModel = viewModel.consultantTopAppBar)
                ChatContent(viewModel)
                ConsultantBottomAppBar(widgetModel = viewModel.consultantBottomAppBar)
            }
        }
    }

    @Composable
    private fun ColumnScope.ChatContent(viewModel: ConsultantViewModel) {
        GetLocalProperties { dimens, brash, colors, shapes, types ->
            val state = viewModel.state.collectAsState()
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
                    LazyColumn(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        verticalArrangement = Arrangement.spacedBy(dimens.mediumDim)
                    ) {
                        items(items = state.value.chat) { message ->
                            when (message.owner) {
                                WebimOwner.User -> UserMessage(message)
                                WebimOwner.Operator -> OperatorMessage(message)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun UserMessage(message: ChatUiEntity) {
        GetLocalProperties { dimens, brash, colors, shapes, types ->
            Box(
                modifier = Modifier
                    .padding(horizontal = dimens.halfMediumDim)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                ChatMessage(message = message, color = colors.shadeBlack4)
            }
        }
    }

    @Composable
    private fun OperatorMessage(message: ChatUiEntity) {
        GetLocalProperties { dimens, brash, colors, shapes, types ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.halfMediumDim),
                contentAlignment = Alignment.BottomStart
            ) {
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.img_round_elta),
                        contentDescription = null
                    )
                    HSpacerSmall()
                    ChatMessage(message = message)
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ChatMessage(
        message: ChatUiEntity,
        color: Color = LocalColors.current.white
    ) {
        GetLocalProperties { dimens, brash, colors, shapes, types ->
            Box(
                modifier = Modifier
                    .clip(shape = shapes.chatMessage)
                    .border(
                        shape = shapes.chatMessage,
                        color = colors.shadeBlack4,
                        width = dimens.borderWidth
                    )
                    .background(color = color)
                    .combinedClickable(
                        onClick = {
                            viewModel.sendAction(ConsultantAction.ChatMessageClick(message))
                        },
                        onLongClick = {
                            viewModel.sendAction(ConsultantAction.ChatMessageLongClick(message))
                        }
                    )
                    .padding(dimens.chatMessagePadding)

            ) {
                Column {
                    Text(text = message.text)
                    VSpacerVerySmall()
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "14:17",
                            color = colors.shadeBlack1
                        )
                        HSpacerVerySmall()
                        Image(
                            painter = painterResource(id = R.drawable.ic_message_received),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): ConsultantFragment = ConsultantFragment()
    }
}
