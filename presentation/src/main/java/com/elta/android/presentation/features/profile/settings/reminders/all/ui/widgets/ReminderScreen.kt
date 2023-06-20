package com.elta.android.presentation.features.profile.settings.reminders.all.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.features.profile.settings.reminders.all.model.ReminderHeaderItem
import com.elta.android.presentation.features.profile.settings.reminders.all.model.ReminderItem
import com.elta.android.presentation.features.profile.settings.reminders.all.model.RemindersAction
import com.elta.android.presentation.features.profile.settings.reminders.all.viewmodels.RemindersViewModel
import com.elta.android.presentation.theme.GetLocalProperties
import com.nullgr.core.adapter.items.ListItem


@Composable
fun ReminderScreen(viewModel: RemindersViewModel) {
    val state = viewModel.state.collectAsState().value
    GetLocalProperties { dimens, _, _, _, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            TopAppBar(viewModel)
            Column(Modifier.padding(dimens.contentPadding)) {
                Title()
                Spacer(modifier = Modifier.size(8.dp))
                Description()
            }

            if (state.reminders.isEmpty()) {
                RemindersEmpty()
            } else {
                Reminders(state.reminders) { id ->
                    viewModel.sendAction(RemindersAction.OpenReminder(id))
                }
            }
        }
    }
}

@Composable
private fun TopAppBar(viewModel: RemindersViewModel) {
    GetLocalProperties { _, _, colors, _, _ ->
        BaseAppTopBar(
            widgetModel = viewModel.appTopBar,
            backgroundColor = colors.white,
            startIcon = R.drawable.ic_back,
            startIconColor = colors.blackBlue,
            endText = R.string.profile_reminders_create_new
        )
    }
}

@Composable
private fun Title() {
    GetLocalProperties { dimens, _, colors, _, types ->
        Text(
            text = stringResource(id = R.string.profile_notification),
            style = types.h1,
            color = colors.blackBlue
        )
    }
}

@Composable
private fun Description() {
    GetLocalProperties { _, _, colors, _, types ->
        Text(
            text = stringResource(id = R.string.profile_reminders_description),
            style = types.body1,
            color = colors.shadeBlack0
        )
    }
}

@Composable
private fun Header(item: ReminderHeaderItem) {
    GetLocalProperties { _, _, colors, _, types ->
        Text(
            modifier = Modifier.padding(16.dp, 16.dp),
            text = item.title,
            style = types.caption1,
            color = colors.shadeBlack2
        )
    }
}

@Composable
private fun Reminders(items: List<ListItem>, onClick: (String) -> Unit) {
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        items(items = items) { item ->
            when (item) {
                is ReminderHeaderItem -> Header(item)
                is ReminderItem -> Reminder(item, onClick)
            }
        }
    }
}

@Composable
fun Reminder(reminder: ReminderItem, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(reminder.id) }
            .padding(16.dp, 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = reminder.type),
                contentDescription = null
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                GetLocalProperties { _, _, colors, _, types ->
                    Text(
                        text = reminder.title,
                        style = types.body2,
                        color = colors.blackBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = reminder.description,
                        style = types.caption1,
                        color = colors.shadeBlack2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }
        }

        Image(
            painter = painterResource(id = reminder.action),
            contentDescription = null
        )
    }
}

@Composable
private fun RemindersEmpty() {
    GetLocalProperties { _, _, colors, _, types ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = stringResource(id = R.string.profile_reminders_empty_state_title),
                style = types.body1,
                color = colors.shadeBlack2
            )
        }
    }
}
