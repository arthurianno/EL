package com.elta.android.presentation.core.ui.adapter

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

class CustomViewOutlineProvider(val dimen: Int) : ViewOutlineProvider() {

    override fun getOutline(view: View, outline: Outline) {
        val resources = view.resources
        outline.setRect(
            0,
            resources.getDimensionPixelSize(dimen),
            view.width,
            view.height
        )
    }
}