package com.elta.android.presentation.features.consultant.model

import com.elta.android.presentation.core.compose.common.Event

//todo: посмотреть как это в других местах сделано, возможно сделать sealed классом и object
data object OpenCamera : Event
data object PhotoSelect : Event
data object FileSelect : Event
data object OpenSettings : Event
data object SendAutoMessage : Event
data object MakeVibration : Event
data object ScrollToDown : Event
