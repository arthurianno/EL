package com.elta.android.presentation.core.pm

import com.nullgr.core.adapter.items.ListItem
import me.dmdev.rxpm.state

abstract class BaseListPm(
    services: ServiceFacade
) : BasePm(services) {

    override val isEmptyScreen: Boolean
        get() = !items.hasValue()

    val items = state<List<ListItem>>()
}
