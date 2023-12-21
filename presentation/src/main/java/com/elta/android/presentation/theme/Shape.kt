package com.elta.android.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val materialThemeShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(0.dp)
)

data class EltaShapes(
    val round: Shape,
    val sheet: Shape,
    val textField: Shape,
    val consultantTextField: Shape,
    val dishCard: Shape,
    var recentQuery: Shape,
    val dialog: Shape,
    val infoDialog: Shape,
    val infoDialogBottom: Shape,
    val textButton: Shape,
    val chatMessage: Shape,
    val smallButton: Shape
)

internal val eltaShapes = EltaShapes(
    round = RoundedCornerShape(50),
    sheet = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomEnd = 0.dp,
        bottomStart = 0.dp
    ),
    textField = RoundedCornerShape(8.dp),
    consultantTextField = RoundedCornerShape(18.dp),
    dishCard = RoundedCornerShape(8.dp),
    recentQuery = RoundedCornerShape(6.dp),
    dialog = RoundedCornerShape(4.dp),
    infoDialog = RoundedCornerShape(8.dp),
    infoDialogBottom = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomEnd = 8.dp,
        bottomStart = 8.dp
    ),
    textButton = RoundedCornerShape(50.dp),
    chatMessage = RoundedCornerShape(16.dp),
    smallButton = RoundedCornerShape(8.dp)
)
