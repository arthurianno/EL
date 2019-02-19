package com.elta.android.presentation.widgets.bottom_sheet

import android.view.View

sealed class BottomSheetEvent(val view: View) {
    class Slide(view: View, val offset: Float) : BottomSheetEvent(view)
    class StateChanged(view: View, val state: Int) : BottomSheetEvent(view)
}