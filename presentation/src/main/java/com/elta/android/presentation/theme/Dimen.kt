package com.elta.android.presentation.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

data class EltaDimens(
    // Base Dims
    val verySmallDim: Dp,
    val smallDim: Dp,
    val halfMediumDim: Dp,
    val mediumDim: Dp,
    val bigDim: Dp,
    val veryBugDim: Dp,
    val contentPadding: Dp,
    val downButtonHeight: Dp,
    val textFieldPopupOffset: DpOffset,
    val borderWidth: Dp,
    val dialogPaddings: PaddingValues,
    val zero: Dp,
    val progressSmallWidth: Dp,
    val progressRegularWidth: Dp,
    val roundedButton: Dp,
    // Calculator Dims
    val lastWordVertical: Dp,
    val dishCardVerticalSpace: Dp,
    val dishNameSpace: Dp,
    val dishCardTextEndPadding: Dp,
    // Dish Dims
    val headerBottomDim: Dp,
    val dishCardHeight: Dp,
    val dishHeaderTitle: PaddingValues,
    val xeValueCard: PaddingValues,
    val xeValue: PaddingValues,
    val verifyIconSize: Dp,
    val dishChars: PaddingValues,
    val portionCountHelpPadding: PaddingValues,
    // Consultant
    val consultantTopBarContentPadding: PaddingValues,
    val consultantTopBarProgress: Dp,
    val consultantBottomBarContentPadding: PaddingValues,
    val photoPreviewBottomBarContentPadding: PaddingValues,
    val messageTextFieldPadding: PaddingValues,
    val graphFieldPadding: PaddingValues,
    val graphTimePadding: Dp,
    val graphItemHeight: Dp,
    val graphItemMaxWidth: Dp,
    val sendMessageTextFieldPadding: PaddingValues,
    val chatCardTextContentPadding: PaddingValues,
    val chatCardFileContentPadding: PaddingValues,
    val chatMessageLabelPadding: PaddingValues,
    val chatPadding: PaddingValues,
    val consultantBottomSheetItemPadding: PaddingValues,
    val photoPreviewContentPadding: PaddingValues,
    val previewSendButtonSize: Dp,
    val imageMessageSize: DpSize,
    val charCardUserMessagePadding: PaddingValues,
    val charCardOperatorMessagePadding: PaddingValues
)

internal val eltaDimens = EltaDimens(
    verySmallDim = 4.dp,
    smallDim = 8.dp,
    halfMediumDim = 12.dp,
    mediumDim = 16.dp,
    bigDim = 24.dp,
    veryBugDim = 32.dp,
    contentPadding = 16.dp,
    downButtonHeight = 52.dp,
    textFieldPopupOffset = DpOffset(16.dp, 0.dp),
    borderWidth = 1.dp,
    dialogPaddings = PaddingValues(top = 24.dp, start = 24.dp, end = 8.dp, bottom = 8.dp),
    zero = 0.dp,
    progressSmallWidth = 1.dp,
    progressRegularWidth = 2.dp,
    roundedButton = 38.dp,
    lastWordVertical = 12.dp,
    dishCardVerticalSpace = 8.dp,
    dishNameSpace = 12.dp,
    dishCardTextEndPadding = 60.dp,
    headerBottomDim = 36.dp,
    dishCardHeight = 488.dp,
    dishHeaderTitle = PaddingValues(top = 12.dp, bottom = 56.dp, start = 16.dp, end = 86.dp),
    xeValueCard = PaddingValues(bottom = 52.dp, end = 16.dp),
    xeValue = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    verifyIconSize = 20.dp,
    dishChars = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    portionCountHelpPadding = PaddingValues(horizontal = 76.dp),
    consultantTopBarContentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    consultantTopBarProgress = 8.dp,
    consultantBottomBarContentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    photoPreviewBottomBarContentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    messageTextFieldPadding = PaddingValues(horizontal = 46.dp),
    graphFieldPadding = PaddingValues(horizontal = 54.dp),
    graphTimePadding = 42.dp,
    graphItemHeight = 2.dp,
    graphItemMaxWidth = 20.dp,
    sendMessageTextFieldPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    chatCardTextContentPadding = PaddingValues(
        top = 12.dp,
        bottom = 28.dp,
        start = 12.dp,
        end = 12.dp
    ),
    chatCardFileContentPadding = PaddingValues(
        top = 12.dp,
        bottom = 28.dp,
        start = 12.dp,
        end = 12.dp
    ),
    chatMessageLabelPadding = PaddingValues(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
    chatPadding = PaddingValues(top = 12.dp, bottom = 12.dp, start = 12.dp, end = 12.dp),
    consultantBottomSheetItemPadding = PaddingValues(16.dp),
    photoPreviewContentPadding = PaddingValues(top = 26.dp, bottom = 40.dp),
    previewSendButtonSize = 48.dp,
    imageMessageSize = DpSize(width = 174.dp, height = 342.dp),
    charCardUserMessagePadding = PaddingValues(
        top = 0.dp,
        bottom = 0.dp,
        start = 78.dp,
        end = 12.dp
    ),
    charCardOperatorMessagePadding = PaddingValues(
        top = 0.dp,
        bottom = 0.dp,
        start = 12.dp,
        end = 32.dp
    )
)
