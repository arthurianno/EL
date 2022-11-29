package com.elta.android.presentation.core.compose.widgets.text

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.elta.android.presentation.R
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun BreadUnitsLabel(breadUnitsCount: Double) {
    GetLocalProperties { _, _, _, _, types ->
        Text(
            text = stringResource(
                id = R.string.calculator_bread_units_count_label,
                breadUnitsCount.toString()
            ),
            style = types.breadUnits
        )
    }
}
