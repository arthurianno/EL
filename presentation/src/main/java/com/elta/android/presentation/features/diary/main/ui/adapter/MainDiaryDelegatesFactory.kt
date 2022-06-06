package com.elta.android.presentation.features.diary.main.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.features.main.records.ui.adapter.delegates.RecordDelegate
import com.elta.android.presentation.features.main.records.ui.adapter.delegates.RecordsGroupDelegate
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class MainDiaryDelegatesFactory @Inject constructor(
    private val viewPool: RecyclerView.RecycledViewPool,
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            RecordItem::class.java -> RecordDelegate(bus)
            RecordsGroupItem::class.java -> RecordsGroupDelegate(viewPool, this)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}
