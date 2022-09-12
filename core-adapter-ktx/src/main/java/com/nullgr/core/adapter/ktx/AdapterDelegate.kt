package com.nullgr.core.adapter.ktx

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.Inflater
import com.nullgr.core.adapter.items.ListItem

/**
 * Delegate of specific [ListItem] that responses for creating and binds view for item.
 * Uses ViewHolder that supports view caching.
 *
 * @author vchernyshov
 */
abstract class AdapterDelegate<B : ViewBinding>(
    private val bindingInflater: Inflater<B>
) : AdapterDelegate() {

    private var _binding: B? = null
    protected val binding
        get() = checkNotNull(_binding)

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        _binding = bindingInflater(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
}
