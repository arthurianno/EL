package com.elta.android.presentation.features.consultant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.domain.features.consultant.model.BotOption
import com.elta.android.presentation.R
import com.elta.android.presentation.features.consultant.model.ConsultantViewState
import com.elta.android.presentation.features.consultant.ui.components.chat.ChatContent
import com.elta.android.presentation.features.consultant.ui.components.top.ConsultantTopBar
import com.elta.android.presentation.theme.GetLocalProperties
import androidx.compose.foundation.lazy.LazyListState

@Composable
fun ConsultantContent(
    state: ConsultantViewState,
    listState: LazyListState,
    onOptionClick: (BotOption) -> Unit,
    onSendTextClick: (String) -> Unit,
    onTopBarBackClick: () -> Unit,
    onCopyClick: (String) -> Unit,
    onResetClick: () -> Unit
) {
    GetLocalProperties { _, _, colors, _, _ ->
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.shadeBlack4)
                .navigationBarsPadding()
                .imePadding(),
            topBar = {
                ConsultantTopBar(
                    canGoBack = state.canGoBack,
                    onBackButtonClick = onTopBarBackClick,
                    onResetClick = onResetClick
                )
            },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth().background(colors.white)) {
                    if (state.currentOptions.isNotEmpty()) {
                        OptionsPanel(
                            options = state.currentOptions,
                            onOptionClick = onOptionClick
                        )
                    }
                    SendInputPanel(onSendTextClick = onSendTextClick)
                }
            }
        ) { paddingValues ->
            ChatContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colors.shadeBlack4),
                chatMessages = state.chatMessages,
                isBotTyping = state.isBotTyping,
                listState = listState,
                onCopyClick = onCopyClick
            )
        }
    }
}

@Composable
private fun OptionsPanel(
    options: List<BotOption>,
    onOptionClick: (BotOption) -> Unit
) {
    GetLocalProperties { _, _, colors, _, _ ->
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.white)
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = 1.dp,
                            color = colors.gGreenB,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onOptionClick(option) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.text,
                        color = colors.gGreenB,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SendInputPanel(
    onSendTextClick: (String) -> Unit
) {
    GetLocalProperties { _, _, colors, _, _ ->
        var textState by remember { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.white)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textState,
                onValueChange = { textState = it },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.consultant_message_placeholder),
                        color = colors.shadeBlack1,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = colors.shadeBlack4.copy(alpha = 0.5f),
                    focusedBorderColor = colors.gGreenB,
                    unfocusedBorderColor = colors.shadeBlack3
                ),
                singleLine = true
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (textState.isNotBlank()) colors.gGreenB else colors.gGreenB.copy(alpha = 0.5f))
                    .clickable(enabled = textState.isNotBlank()) {
                        onSendTextClick(textState)
                        textState = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = null,
                    tint = colors.white,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
