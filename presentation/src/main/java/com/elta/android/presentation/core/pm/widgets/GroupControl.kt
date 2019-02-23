package com.elta.android.presentation.core.pm.widgets

import com.elta.android.presentation.core.bus.Click
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.ui.adapter.GroupItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import io.reactivex.Observable
import me.dmdev.rxpm.PresentationModel

class GroupControl(pm: BasePm, items: PresentationModel.State<List<ListItem>>) {

    val groupItems: Observable<List<ListItem>> = pm.bus.observable(GroupControl::class.java)
        .filter { it is GroupClicked }
        .map { it as GroupClicked }
        .map { it.item }
        .map { it.toggle(items.value) }
}

private fun GroupItem.toggle(source: List<ListItem>): List<ListItem> {
    val itemIndex = source.indexOf(this)
    val newItems = mutableListOf<ListItem>()
    val childItems = this.items
    when (isExpanded) {
        true -> source.forEachIndexed { index, listItem ->
            newItems.add(listItem)
            if (index == itemIndex && childItems.isNotEmpty()) {
                newItems.addAll(childItems)
            }
        }
        else -> {
            val startIndex = itemIndex + 1
            val endIndex = itemIndex + childItems.size
            source.forEachIndexed { index, listItem ->
                if (index !in startIndex..endIndex) {
                    newItems.add(listItem)
                }
            }
        }
    }
    return newItems
}

fun BasePm.groupControl(items: PresentationModel.State<List<ListItem>>): GroupControl = GroupControl(this, items)

fun RxBus.groupClick(click: GroupClicked) {
    post(GroupControl::class.java, click)
}

data class GroupClicked(val item: GroupItem) : Click