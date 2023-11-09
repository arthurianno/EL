package com.elta.android.presentation.features.calcutator.products.model

import com.elta.android.presentation.core.compose.common.Action

internal sealed class CalculatorAction : Action {
    data class LastWordClick(val word: String) : CalculatorAction()
    data class DishClicked(val dish: DishUiEntity) : CalculatorAction()
    object CustomProductClicked : CalculatorAction()
    object CreateCustomProductClicked : CalculatorAction()
    data class DeleteDishClicked(val dish: DishUiEntity) : CalculatorAction()
    object PortionHelpClick : CalculatorAction()
    object ClearList : CalculatorAction()
}
