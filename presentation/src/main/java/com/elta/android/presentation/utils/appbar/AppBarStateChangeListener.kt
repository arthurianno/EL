package com.elta.android.presentation.utils.appbar

import android.support.design.widget.AppBarLayout

abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {

    private var currentState = AppBarState.IDLE

    override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
        when {
            i == 0 -> {
                if (currentState !== AppBarState.EXPANDED) {
                    onStateChanged(appBarLayout, AppBarState.EXPANDED)
                }
                currentState = AppBarState.EXPANDED
            }
            Math.abs(i) >= appBarLayout.totalScrollRange -> {
                if (currentState !== AppBarState.COLLAPSED) {
                    onStateChanged(appBarLayout, AppBarState.COLLAPSED)
                }
                currentState = AppBarState.COLLAPSED
            }
            else -> {
                if (currentState !== AppBarState.IDLE) {
                    onStateChanged(appBarLayout, AppBarState.IDLE)
                }
                currentState = AppBarState.IDLE
            }
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout, state: AppBarState)
}