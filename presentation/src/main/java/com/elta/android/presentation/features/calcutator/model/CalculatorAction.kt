package com.elta.android.presentation.features.calcutator.model

import com.elta.android.presentation.core.compose.common.Action

sealed class CalculatorAction : Action {
    data class LastWordClick(val word: String) : CalculatorAction()
    data class AddDishClick(val dish: DishUiEntity) : CalculatorAction()
    object PortionHelpClick : CalculatorAction()
}
