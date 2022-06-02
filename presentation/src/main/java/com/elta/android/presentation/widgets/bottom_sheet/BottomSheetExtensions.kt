package com.elta.android.presentation.widgets.bottom_sheet

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

inline fun <T : View> BottomSheetBehavior<T>.expand() {
    this.state = BottomSheetBehavior.STATE_EXPANDED
}

inline fun <T : View> BottomSheetBehavior<T>.collaps() {
    this.state = BottomSheetBehavior.STATE_COLLAPSED
}

inline fun <T : View> BottomSheetBehavior<T>.hide() {
    this.state = BottomSheetBehavior.STATE_HIDDEN
}

fun Int.stateAsString(): String =
    when (this) {
        BottomSheetBehavior.STATE_HIDDEN -> "HIDDEN"
        BottomSheetBehavior.STATE_EXPANDED -> "EXPANDED"
        BottomSheetBehavior.STATE_COLLAPSED -> "COLLAPSED"
        BottomSheetBehavior.STATE_DRAGGING -> "DRAGGING"
        BottomSheetBehavior.STATE_HALF_EXPANDED -> "HALF_EXPANDED"
        BottomSheetBehavior.STATE_SETTLING -> "SETTLING"
        else -> "UNKNOWN"
    }
