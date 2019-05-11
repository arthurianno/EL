package com.elta.android.presentation.widgets.decoration

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.support.annotation.DimenRes
import android.support.v7.widget.RecyclerView
import android.view.View
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSeparatorItem
import com.nullgr.core.adapter.DynamicAdapter

@Suppress("MagicNumber")
class SettingsMarginItemDecoration(
    context: Context,
    @DimenRes
    private val marginTopDimen: Int,
    @DimenRes
    private val marginBottomDimen: Int
) : RecyclerView.ItemDecoration() {

    private var marginTop: Int
    private var marginBottom: Int

    init {
        marginTop = getPixelSize(context.resources, marginTopDimen)
        marginBottom = getPixelSize(context.resources, marginBottomDimen)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        val currentItem = getItemByPosition(parent, position)
        val previousItem = getItemByPosition(parent, if (position > 0) position.minus(1) else 0)
        when {
            currentItem is ProfileSettingsHeaderItem
                && previousItem is ProfileSettingsSeparatorItem -> {
                outRect.top = marginTop / 2
                outRect.bottom = marginBottom
            }
            currentItem is ProfileSettingsHeaderItem -> {
                outRect.top = marginTop
                outRect.bottom = marginBottom
            }
        }
    }

    private fun getItemByPosition(parent: RecyclerView, position: Int) =
        (parent.adapter as DynamicAdapter).items[position]

    private fun getPixelSize(resources: Resources, @DimenRes margin: Int): Int =
        if (margin == 0) 0 else resources.getDimensionPixelSize(margin)
}
