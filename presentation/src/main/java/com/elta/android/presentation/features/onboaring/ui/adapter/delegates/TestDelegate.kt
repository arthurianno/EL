package com.elta.android.presentation.features.onboaring.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.onboaring.ui.adapter.items.TestItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import kotlinx.android.synthetic.main.item_test.*

class TestDelegate : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_test
    override val itemType: Any = TestItem::class

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as TestItem

        with(holder as ViewHolder) {
            testView.text = item.text
        }
    }
}