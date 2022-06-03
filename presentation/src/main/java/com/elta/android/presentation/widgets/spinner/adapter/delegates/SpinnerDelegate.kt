package com.elta.android.presentation.widgets.spinner.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.utils.toString
import com.elta.android.presentation.widgets.spinner.SelectItemListener
import com.elta.android.presentation.widgets.spinner.adapter.items.SpinnerItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.resources.ResourceProvider
import kotlinx.android.synthetic.main.item_spinner.*

@Suppress("MagicNumber")
class SpinnerDelegate(
    private val resources: ResourceProvider,
    private val listener: SelectItemListener
) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_spinner
    override val itemType: Any = SpinnerItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<SpinnerItem> { _, item, _ ->
                        listener.onItemSelected(item, item.type.toString(resources))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as SpinnerItem
        with(holder as ViewHolder) {
            spinnerTitleView.text = item.type.toString(resources)
        }
    }
}
