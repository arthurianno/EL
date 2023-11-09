package com.elta.android.presentation.features.calcutator.products.model

import com.elta.android.presentation.core.compose.common.Action

sealed class DishDetailAction : Action {
    object ViewName : DishDetailAction()
    object Retry : DishDetailAction()
}
