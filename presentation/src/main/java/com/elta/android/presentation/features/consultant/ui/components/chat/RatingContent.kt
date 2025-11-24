package com.elta.android.presentation.features.consultant.ui.components.chat

import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun RatingContent(
    starsCount: Int?,
    onRatingStarIconClick: (Int) -> Unit
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        Row(
            modifier = Modifier
                .padding(dimens.chatRatingContentPadding)
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Bottom
        ) {
            OperatorImage()
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom
            ) {
                OperatorNameLabel()
                VSpacerVerySmall()
                RatingCard(starsCount, onRatingStarIconClick)
            }
        }
    }
}

@Composable
private fun RatingCard(
    starsCount: Int?,
    onRatingStarIconClick: (Int) -> Unit
) {
    GetLocalProperties { dimens, _, colors, shapes, styles ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = shapes.chatMessage)
                .border(
                    shape = shapes.chatMessage,
                    color = colors.shadeBlack4,
                    width = dimens.borderWidth
                )
                .background(colors.white)
                .padding(dimens.chatRatingCardPadding),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = stringResource(id = R.string.consultant_rate_operator),
                style = styles.body1,
                color = colors.blackBlue
            )
            VSpacerMedium()
            EstimateLine(
                startsCount = 5,
                selectedStarNumber = starsCount,
                onRatingStarIconClick = onRatingStarIconClick
            )
        }
    }
}

@Composable
fun EstimateLine(
    startsCount: Int,
    selectedStarNumber: Int?,
    onRatingStarIconClick: (Int) -> Unit
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        Row(
            modifier = Modifier.width(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(dimens.mediumDim)
        ) {
            repeat(startsCount) { number ->
                val starNumber = number + 1
                StarIcon(
                    rateNumber = starNumber,
                    isSelected = selectedStarNumber?.let { it >= starNumber } ?: false,
                    onRatingStarIconClick = onRatingStarIconClick
                )
            }
        }
    }
}

@Composable
private fun StarIcon(
    rateNumber: Int,
    isSelected: Boolean = false,
    onRatingStarIconClick: (Int) -> Unit,
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        val color = remember { Animatable(colors.shadeBlack3) }
        LaunchedEffect(isSelected) {
            val starColor = if (isSelected) colors.gold
            else colors.shadeBlack3
            color.animateTo(starColor)
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_rating_star),
            tint = color.value,
            contentDescription = null,
            modifier = Modifier
                .padding(dimens.verySmallDim)
                .clickableWithNoRipple { onRatingStarIconClick(rateNumber) }
        )
    }
}

@Preview
@Composable
private fun PreviewRatingMessage() {
    RatingContent(null) {}
}
