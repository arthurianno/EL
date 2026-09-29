package com.elta.android.presentation.core.compose.widgets.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.theme.LocalBrash
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalDimens
import com.elta.android.presentation.theme.LocalTypes

@Composable
fun GradientActionButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    shape: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val cornerShape = RoundedCornerShape(shape.dp)

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = cornerShape,
        elevation = null,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            disabledBackgroundColor = Color.Transparent,
            contentColor = colors.white,
            disabledContentColor = if (isLoading) colors.white else colors.shadeBlack1
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = LocalDimens.current.downButtonHeight)
            .then(
                if (enabled || isLoading) {
                    Modifier.background(LocalBrash.current.downButton, cornerShape)
                } else {
                    Modifier.background(colors.shadeBlack3, cornerShape)
                }
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colors.white,
                strokeWidth = 2.dp
            )
        } else {
            Text(text, style = LocalTypes.current.buttonLargeText)
        }
    }
}
