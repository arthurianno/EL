package com.elta.android.presentation.features.main.records.ui.adapter

import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.features.main.records.ui.adapter.delegates.RecordDelegate
import com.elta.android.presentation.features.main.records.ui.adapter.delegates.RecordsGroupDelegate
import com.elta.android.presentation.features.main.records.ui.adapter.delegates.RecordsHeaderDelegate
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class MainRecordsDelegatesFactory @Inject constructor(
    private val viewPool: RecyclerView.RecycledViewPool,
    private val calculator: DiffCalculator,
    private val bus: RxBus,
    private val resources: ResourceProvider
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            RecordItem::class.java -> RecordDelegate(bus)
            RecordsGroupItem::class.java -> RecordsGroupDelegate(this, calculator, viewPool)
            RecordsHeaderItem::class.java -> RecordsHeaderDelegate(resources)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}