package com.elta.android.presentation.features.consultant.ui.components.chat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.domain.features.consultant.model.ChatMessage
import com.elta.android.presentation.R
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    chatMessages: List<ChatMessage>,
    isBotTyping: Boolean,
    listState: LazyListState,
    onCopyClick: (String) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = chatMessages,
            key = { it.id }
        ) { message ->
            MessageContent(
                message = message,
                onCopyClick = onCopyClick
            )
        }

        if (isBotTyping) {
            item(key = "typing_indicator") {
                TypingIndicator()
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.gGreenB.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_support),
                    contentDescription = null,
                    tint = colors.gGreenB,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                    .background(colors.white)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                
                val dot1Offset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -6f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 600
                            0f at 0
                            -6f at 150
                            0f at 300
                        },
                        repeatMode = RepeatMode.Restart
                    )
                )

                val dot2Offset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -6f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 600
                            0f at 75
                            -6f at 225
                            0f at 375
                        },
                        repeatMode = RepeatMode.Restart
                    )
                )

                val dot3Offset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -6f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 600
                            0f at 150
                            -6f at 300
                            0f at 450
                        },
                        repeatMode = RepeatMode.Restart
                    )
                )

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .offset(y = dot1Offset.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.gGreenB)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .offset(y = dot2Offset.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.gGreenB)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .offset(y = dot3Offset.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.gGreenB)
                )
            }
        }
    }
}
