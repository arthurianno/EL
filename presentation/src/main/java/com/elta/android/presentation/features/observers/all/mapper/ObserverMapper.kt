package com.elta.android.presentation.features.observers.all.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.domain.features.observers.model.ObserverStatus
import com.elta.android.presentation.R
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverHeaderItem
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.collections.Predicate
import com.nullgr.core.collections.split
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class ObserverMapper @Inject constructor(
    private val resources: ResourceProvider
) : Mapper<List<Observer>, List<ListItem>> {

    override fun mapFromObject(source: List<Observer>): List<ListItem> {
        val predicates = mutableListOf(
            ConfirmedPredicate,
            PendingPredicate
        )
        val splitted = source.split(predicates)
        val confirmed = splitted[0]
        val pending = splitted[1]

        return arrayListOf<ListItem>().apply {
            if (confirmed.isNotEmpty()) {
                add(ObserverHeaderItem(resources.getString(R.string.profile_observers_header_active)))
                addAll(confirmed.map { mapFromObserver(it) })
            }

            if (pending.isNotEmpty()) {
                add(ObserverHeaderItem(resources.getString(R.string.profile_observers_header_pending)))
                addAll(pending.map { mapFromObserver(it) })
            }
        }
    }

    private fun mapFromObserver(source: Observer): ListItem =
        with(source) {
            ObserverItem(
                id = id,
                type = if (status == ObserverStatus.CONFIRMED) R.drawable.ic_viewer_enabled
                else R.drawable.ic_viewer_disabled,
                title = if (name.isNullOrEmpty()) resources.getString(R.string.profile_observers_user_name_placeholder)
                else checkNotNull(name),
                description = email,
                action = R.drawable.ic_arrow_left,
                status = status
            )
        }

    private object ConfirmedPredicate : Predicate<Observer> {
        override fun test(t: Observer?) = t?.status == ObserverStatus.CONFIRMED
    }

    private object PendingPredicate : Predicate<Observer> {
        override fun test(t: Observer?) = t?.status == ObserverStatus.PENDING
    }
}