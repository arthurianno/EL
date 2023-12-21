package com.elta.android.presentation.core.pm

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.ui.adapter.HideableItem
import com.nullgr.core.adapter.items.ListItem
import me.dmdev.rxpm.state

abstract class ExpandableListPm(
    services: ServiceFacade
) : BaseListPm(services) {

    protected val listItems = state<List<ListItem>>()

    override fun onCreate() {
        super.onCreate()
        observeEvents()
    }

    private fun observeEvents() {
        listItems.observable
            .map { item -> item.filter { if (it is HideableItem) it.isVisible else true } }
            .subscribe(items.consumer)
            .untilDestroy()
    }

    abstract fun onItemExpandCollapse(clickedItem: ListItem, allItems: List<ListItem>): List<ListItem>

    override fun onBind() {
        super.onBind()

        bus.clicks<Clicks.ExpandCollapse>()
            .withLatestFrom(listItems.observable) { busValue, items ->
                onItemExpandCollapse(busValue.item, items)
            }
            .subscribe(listItems.consumer)
            .untilUnbind()
    }
}
