package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PaletteSelectionSheet(onPaletteSelected: (NewDesignPalette) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlucoseDashboardTheme.LightCardBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Палитра интерфейса",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF353B4B)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NewDesignPalette.entries.forEach { palette ->
                val isSelected = NewDesignPaletteController.activePalette == palette
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isSelected) GlucoseDashboardTheme.NormalChartColor else Color(0xFFF1F3F5)
                        )
                        .clickable { onPaletteSelected(palette) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Палитра ${palette.name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF353B4B)
                    )
                }
            }
        }
    }
}
