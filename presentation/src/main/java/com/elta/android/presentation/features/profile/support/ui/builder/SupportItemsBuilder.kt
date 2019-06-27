package com.elta.android.presentation.features.profile.support.ui.builder

import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.support.model.SupportAction
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportActionItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class SupportItemsBuilder @Inject constructor(
    private val resourceProvider: ResourceProvider
) {
    fun buildItems(): List<ListItem> = arrayListOf<ListItem>().apply {
        add(
            SupportActionItem(
                icon = R.drawable.ic_support_call,
                title = resourceProvider.getString(R.string.profile_support_phone_number),
                subTitle = resourceProvider.getString(R.string.profile_support_phone_number_description),
                action = SupportAction.CallAction(resourceProvider.getString(R.string.profile_support_phone_number))
            )
        )
        add(
            SupportActionItem(
                icon = R.drawable.ic_support_mail,
                title = resourceProvider.getString(R.string.profile_support_email),
                subTitle = resourceProvider.getString(R.string.profile_support_email_description),
                action = SupportAction.MailAction(resourceProvider.getString(R.string.profile_support_email))
            )
        )
        add(
            SupportActionItem(
                icon = R.drawable.ic_support_center,
                title = resourceProvider.getString(R.string.profile_support_service_centers),
                subTitle = resourceProvider.getString(R.string.profile_support_service_centers_description),
                action = SupportAction.ServiceCentersAction
            )
        )
    }
}