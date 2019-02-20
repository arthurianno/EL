package com.elta.android.presentation.features.main.records.pm

import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

@Suppress("MagicNumber")
class MainRecordsPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    override fun onCreate() {
        super.onCreate()

        items.consumer.accept(arrayListOf<ListItem>().apply {
            (0..5).forEach { item ->
                add(
                    RecordItem(
                        id = item,
                        icon = R.drawable.ic_medicine,
                        title = "Title #$item",
                        type = "Type #$item",
                        count = "$item ед",
                        date = "12:00",
                        showLabel = item % 2 == 0
                    )
                )
            }
        })
    }
}