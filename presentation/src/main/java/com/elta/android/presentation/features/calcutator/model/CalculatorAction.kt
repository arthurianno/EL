package com.elta.android.presentation.features.calcutator.model

import com.elta.android.presentation.core.compose.common.Action

internal sealed class CalculatorAction : Action {
    data class LastWordClick(val word: String) : CalculatorAction()
    data class DishClick(val dish: DishUiEntity) : CalculatorAction()
    data class DeleteDishClick(val dish: DishUiEntity) : CalculatorAction()
    object PortionHelpClick : CalculatorAction()
}
