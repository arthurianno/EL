package com.nullgr.core.adapter.ktx

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * ViewHolder that implements [LayoutContainer] to use cached views.
 *
 * @author vchernyshov
 */
open class ViewHolder(
    private val binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
    override val containerView: View?
        get() = binding.root
}
