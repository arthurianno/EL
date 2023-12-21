package com.elta.android.presentation.features.calcutator.custom.model

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.user.model.Diabetes

@Immutable
data class CustomProductsViewState(
    val searchInFocus: Boolean,
    val calculatorFlow: CalculatorFlow,
    val isError: Boolean,
)
