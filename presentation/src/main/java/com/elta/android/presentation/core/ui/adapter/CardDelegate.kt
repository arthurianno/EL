package com.elta.android.presentation.core.ui.adapter

import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewOutlineProvider
import com.elta.android.presentation.R
import com.nullgr.core.adapter.items.ListItem

fun bindElevation(holder: RecyclerView.ViewHolder) {
    with(holder.itemView) {
        ViewCompat.setElevation(this, resources.getDimension(R.dimen.card_elevation))
    }
}

fun bindBackground(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
    with(holder.itemView) {
        when {
            items.size == 1 -> {
                background = resources.getDrawable(R.drawable.bg_corners_top_bottom, null)
                outlineProvider = ViewOutlineProvider.BACKGROUND
            }
            position == 0 -> {
                background = resources.getDrawable(R.drawable.bg_corners_top, null)
                outlineProvider = ViewOutlineProvider.BACKGROUND
            }
            position == items.size - 1 -> {
                background = resources.getDrawable(R.drawable.bg_corners_bottom, null)
                outlineProvider = CustomViewOutlineProvider(R.dimen.card_elevation)
            }
            else -> {
                setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                outlineProvider = CustomViewOutlineProvider(R.dimen.card_elevation)
            }
        }
    }
}