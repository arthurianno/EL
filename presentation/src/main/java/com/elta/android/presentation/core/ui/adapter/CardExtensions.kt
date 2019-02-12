package com.elta.android.presentation.core.ui.adapter

import android.support.v7.widget.RecyclerView
import com.github.captain_miao.optroundcardview.OptRoundCardView
import com.nullgr.core.adapter.items.ListItem

fun bindCardCorners(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
    with(holder.itemView) {
        if (this is OptRoundCardView) {
            when {
                items.size == 1 -> showCorner(true, true, true, true)
                position == 0 -> showCorner(true, true, false, false)
                position == items.size - 1 -> showCorner(false, false, true, true)
                else -> showCorner(false, false, false, false)
            }
        }
    }
}

fun getCardType(size: Int, position: Int): CardType =
    when {
        size == 1 -> CardType.SINGLE
        position == 0 -> CardType.TOP
        position == size - 1 -> CardType.BOTTOM
        else -> CardType.MIDDLE
    }
