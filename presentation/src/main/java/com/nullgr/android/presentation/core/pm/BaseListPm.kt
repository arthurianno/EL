package com.nullgr.android.presentation.core.pm

import com.nullgr.core.adapter.items.ListItem

abstract class BaseListPm(
    services: ServiceFacade
) : BasePm(services) {

    override val isEmptyScreen: Boolean
        get() = !items.hasValue()

    val items = State<List<ListItem>>()
}