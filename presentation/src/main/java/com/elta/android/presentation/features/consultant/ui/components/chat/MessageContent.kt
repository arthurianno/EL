package com.elta.android.presentation.features.consultant.ui.components.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.domain.features.consultant.model.ChatMessage
import com.elta.android.domain.features.consultant.model.MessageSender
import com.elta.android.presentation.R
import com.elta.android.presentation.theme.GetLocalProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageContent(
    message: ChatMessage,
    onCopyClick: (String) -> Unit
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        val isBot = message.sender == MessageSender.BOT
        val alignment = if (isBot) Alignment.CenterStart else Alignment.CenterEnd
        val bubbleColor = if (isBot) colors.white else colors.gGreenB.copy(alpha = 0.15f)
        val bubbleShape = if (isBot) {
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        } else {
            RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 16.dp),
            contentAlignment = alignment
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                if (isBot) {
                    // Иконка ассистента для бота
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
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Column(
                    modifier = Modifier
                        .clip(bubbleShape)
                        .background(bubbleColor)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { onCopyClick(message.text) }
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        color = colors.shadeBlack1,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                    
                    val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
                    Text(
                        text = timeString,
                        color = colors.shadeBlack2,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                    )
                }

                if (!isBot) {
                    // Нет аватарки для пользователя, сообщения просто выравниваются вправо
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
