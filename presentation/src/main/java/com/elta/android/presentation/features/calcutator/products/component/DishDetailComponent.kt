package com.elta.android.presentation.features.calcutator.products.component

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.VSpacerLarge
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.model.ServingUiEntity
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun DishChars(serving: ServingUiEntity) {
    GetLocalProperties { dimens, _, colors, shapes, _ ->
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
            DishChar(R.string.calculator_dish_carbs, serving.carbohydrate)
        }
    }
}

@Composable
private fun DishChar(@StringRes charName: Int, charCount: String) {
    GetLocalProperties { _, _, colors, _, types ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = charCount, style = types.title3
            )
            Text(
                text = stringResource(id = charName), color = colors.shadeBlack1
            )
        }
    }
}
