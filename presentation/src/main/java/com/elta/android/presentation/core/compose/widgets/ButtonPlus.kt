package com.elta.android.presentation.core.compose.widgets

import androidx.compose.foundation.Image
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.elta.android.presentation.R

@Composable
fun ButtonPlus(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Image(
            painter = painterResource(id = R.drawable.btn_plus),
            contentDescription = null
        )
    }
}
