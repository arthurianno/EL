package com.elta.android.presentation.features.calcutator.products.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.elta.android.presentation.R
import com.elta.android.presentation.features.calcutator.products.model.ServingUiEntity
import com.elta.android.presentation.features.calcutator.products.viewmodel.NOTHING_DASH
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun DishChars(serving: ServingUiEntity) {
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Column {
            Text(
                text = stringResource(id = R.string.calculator_additional_information),
                color = colors.shadeBlack2,
            )
            Row(
                modifier = Modifier
                    .padding(vertical = dimens.contentPadding)
                    .fillMaxWidth()
                    .clip(shape = shapes.dishCard)
                    .background(color = colors.paleGray)
                    .padding(dimens.dishChars),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DishChar(R.string.calculator_dish_calories, serving.calories)
                DishChar(R.string.calculator_dish_proteins, serving.protein)
                DishChar(R.string.calculator_dish_fats, serving.fat)
                DishChar(R.string.calculator_dish_carbs, serving.carbohydrate.orEmpty())
            }
        }
    }
}

@Composable
private fun DishChar(@StringRes charName: Int, charCount: String) {
    val gramValue =
        if (charCount.isEmpty() || charCount == "0") NOTHING_DASH
        else stringResource(id = R.string.calculator_dish_gram_value, charCount)

    GetLocalProperties { _, _, colors, _, types ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = gramValue,
                style = types.title3
            )
            Text(
                text = stringResource(id = charName),
                color = colors.shadeBlack1
            )
        }
    }
}
