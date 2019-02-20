package com.elta.android.presentation.features.main.records.ui.adapter

import com.elta.android.presentation.features.main.records.ui.adapter.delegates.RecordDelegate
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class MainRecordsDelegatesFactory @Inject constructor(
    private val calculator: DiffCalculator,
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            RecordItem::class.java -> RecordDelegate(bus)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}