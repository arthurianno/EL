package com.elta.android.presentation.features.calcutator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonBack
import com.elta.android.presentation.features.calcutator.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.model.ServingUiEntity
import com.elta.android.presentation.theme.GetLocalProperties


@Composable
fun MainHeader(
    dish: DishUiEntity,
    backClickListener: () -> Unit,
    viewNameClickListener: () -> Unit,
) {
    GetLocalProperties { dimens, brash, colors, _, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = dimens.dishCardHeight - dimens.headerBottomDim)
                .background(brush = brash.dishHeader)
        ) {
            ButtonBack(
                color = colors.paleGray,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .statusBarsPadding(),
                onClick = backClickListener
            )
            Column(
                modifier = Modifier
                    .fillMaxSize(), verticalArrangement = Arrangement.Bottom

            ) {
                HeaderTitle(dish, viewNameClickListener)
                BreadUnitsValue(dish)
            }
        }
    }
}

@Composable
private fun ColumnScope.HeaderTitle(dish: DishUiEntity, viewNameClickListener: () -> Unit) {
    GetLocalProperties { dimens, _, colors, shapes, types ->
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            var didOverflowHeight by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.Bottom
            ) {
                if (dish.brandName.isNotEmpty()) {
                    Text(
                        text = dish.brandName,
                        style = types.body1,
                        color = colors.white,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    if (dish.isVerified) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_verify_dish),
                            contentDescription = null,
                            modifier = Modifier.padding(top = dimens.smallDim)
                        )
                        HSpacerVerySmall()
                    }
                    Text(
                        text = dish.name,
                        style = types.h1,
                        color = colors.white,
                        onTextLayout = { textLayoutResult ->
                            didOverflowHeight = textLayoutResult.didOverflowHeight
                        },
                        modifier = Modifier
                            .fadingEdges(didOverflowHeight)
                    )
                }
            }

            if (didOverflowHeight) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(shape = shapes.textButton)
                        .background(color = colors.blackBlue30)
                ) {
                    Text(
                        text = stringResource(id = R.string.calculator_view_name),
                        style = types.moreTextButton,
                        color = colors.white,
                        modifier = Modifier
                            .clickable { viewNameClickListener.invoke() }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

private fun Modifier.fadingEdges(
    didOverflowHeight: Boolean,
    bottomEdgeHeight: Dp = 72.dp
): Modifier = this.then(
    Modifier
        .graphicsLayer { alpha = 0.99F }
        .drawWithContent {
            drawContent()
            if (didOverflowHeight) {
                val bottomColors = listOf(Color.Black, Color.Transparent)
                val bottomEndY = size.height
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = bottomColors,
                        startY = bottomEndY - bottomEdgeHeight.toPx(),
                        endY = bottomEndY
                    ), blendMode = BlendMode.DstIn
                )
            }
        })

@Composable
fun BreadUnitsValue(dish: DishUiEntity) {
    GetLocalProperties { dimens, _, colors, shapes, types ->

        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 48.dp)
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.calculator_bread_units_count),
                color = colors.white,
            )

            Text(
                text = stringResource(
                    id = R.string.calculator_bread_units_count_label,
                    dish.breadUnits,
                ),
                modifier = Modifier
                    .clip(shapes.dishCard)
                    .background(color = colors.white)
                    .padding(dimens.xeValue),
                color = colors.gOrangeB,
                style = types.title3,
            )
        }

    }
}

@Preview
@Composable
fun ContentPreView() {
    val serving = ServingUiEntity(
        "r1",
        "12",
        "2.0",
        "3.0",
        "40.0",
        "2.0",
        "11.0",
    )
    val manyText = "Темно-Зеленые Листовые в Томатном Соусе"

    val dishUiEntity = DishUiEntity(
        "123",
        "ru",
        manyText,
        DishType.Brand,
        "Агуша",
        true,
        listOf(serving),
        serving,
        "20.1",
        Pair("100", "100.0"),
        "2.0"
    )



    Box(
        modifier = Modifier.fillMaxSize()

    ) {
        MainHeader(
            dish = dishUiEntity,
            backClickListener = { },
            viewNameClickListener = { }
        )
    }
}