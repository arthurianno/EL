package com.elta.android.presentation.features.observers.all.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.domain.features.observers.interactor.userName
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.domain.features.observers.model.ObserverStatus
import com.elta.android.presentation.R
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverHeaderItem
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class ObserverMapper @Inject constructor(
    private val resources: ResourceProvider
) : Mapper<List<Observer>, List<ListItem>> {

    override fun mapFromObject(source: List<Observer>): List<ListItem> {
        val confirmedObservers = source.filter { it.status == ObserverStatus.CONFIRMED }
        val pendingObservers = source.filter { it.status == ObserverStatus.PENDING }
        return mutableListOf<ListItem>().apply {
            if (confirmedObservers.isNotEmpty()) {
                add(ObserverHeaderItem(resources.getString(R.string.profile_observers_header_active)))
                addAll(confirmedObservers.map { it.toUi() })
            }
            if (pendingObservers.isNotEmpty()) {
                add(ObserverHeaderItem(resources.getString(R.string.profile_observers_header_pending)))
                addAll(pendingObservers.map { it.toUi() })
            }
        }
    }

    private fun Observer.toUi(): ListItem =
        ObserverItem(
            id = id,
            type = status.toIcon(),
            title = userName
                ?: resources.getString(R.string.profile_observers_user_name_placeholder),
            description = email,
            action = R.drawable.ic_arrow_left,
            status = status
        )

    private fun ObserverStatus.toIcon() =
        if (this == ObserverStatus.CONFIRMED) {
            R.drawable.ic_viewer_enabled
        } else {
            R.drawable.ic_viewer_disabled
        }
}
