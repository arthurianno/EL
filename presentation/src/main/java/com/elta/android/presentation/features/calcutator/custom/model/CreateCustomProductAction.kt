package com.elta.android.presentation.features.calcutator.custom.model

import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity

internal sealed class CreateCustomProductAction: Action {
    object Retry: CreateCustomProductAction()
    object PortionHelpClick: CreateCustomProductAction()
}
