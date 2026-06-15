package com.elta.android.presentation.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R

@Composable
fun StatusAndDrawerPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1B1F)) // Темный фон экрана блокировки
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Status Bar Preview (Иконка у часов)",
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        // Мок статус-бара
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF323135), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "13:19",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_stat_onesignal_default),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "VoWiFi", color = Color.LightGray, fontSize = 10.sp)
                Text(text = "92%", color = Color.White, fontSize = 12.sp)
            }
        }

        Text(
            text = "Notification Drawer Preview (В шторке)",
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        // Мок карточки уведомления в шторке
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2C30), shape = RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Иконка-градиент напрямую как в файле
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_stat_onesignal_default),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Текстовая часть уведомления
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Сателлит Online",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Сейчас",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "Тест5",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Пример текста пуш-уведомления...",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNotificationStyle() {
    StatusAndDrawerPreview()
}
