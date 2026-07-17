package com.elta.android.presentation.features.consultant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.domain.features.consultant.model.BotOption
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
    onTopBarBackClick: () -> Unit,
    onCopyClick: (String) -> Unit,
    onResetClick: () -> Unit
) {
    GetLocalProperties { _, _, colors, _, _ ->
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.shadeBlack4)
                .navigationBarsPadding(),
            topBar = {
                ConsultantTopBar(
                    canGoBack = state.canGoBack,
                    onBackButtonClick = onTopBarBackClick,
                    onResetClick = onResetClick
                )
            },
            bottomBar = {
                if (state.currentOptions.isNotEmpty()) {
                    OptionsPanel(
                        options = state.currentOptions,
                        onOptionClick = onOptionClick
                    )
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
