package com.elta.android.presentation.features.calcutator.custom.model

import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity

internal sealed class CustomProductAction: Action {
    data class ProductClicked(val dish: DishUiEntity): CustomProductAction()
    data class DeleteProductClicked(val dish: DishUiEntity): CustomProductAction()
    object CreateProduct : CustomProductAction()
    object ErrorResult : CustomProductAction()
}
