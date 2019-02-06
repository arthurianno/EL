package com.elta.android.presentation.utils

import android.support.v7.widget.RecyclerView
import android.content.Context
import android.support.v4.view.ViewCompat
import android.view.View
import android.widget.ImageView
import com.elta.android.presentation.R
import com.nullgr.core.adapter.items
import com.nullgr.core.adapter.items.ListItem

fun ImageView.toggleSecureIcon(isSecure: Boolean) {
    setImageResource(when (isSecure) {
        true -> R.drawable.ic_show_password
        else -> R.drawable.ic_password_hide
    })
}

inline fun <reified T : ListItem> RecyclerView.ViewHolder.withAdapterPosition(
    block: (items: List<ListItem>, item: T, position: Int) -> Unit
) {
    with(adapterPosition) {
        if (this != RecyclerView.NO_POSITION) {
            val items = items()
            if (items != null && this >= 0 && this < items.size) {
                block.invoke(items, items[this] as T, this)
            }
        }
    }
}

fun View.applyInsetsToContentView(fitsSystemWindows: Boolean) {
    this.fitsSystemWindows = fitsSystemWindows
    ViewCompat.requestApplyInsets(this)
}

fun View.applySystemWindowPadding() {
    this.y = getStatusBarHeight(this.context).toFloat()
}

private fun getStatusBarHeight(context: Context): Int {
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    return context.resources.getDimensionPixelSize(resourceId)
}