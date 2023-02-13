package com.elta.android.presentation.features.consultant.widgets

import android.Manifest
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import com.elta.android.domain.features.consultant.model.WebimContentType
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.disableClickable
import com.elta.android.presentation.core.compose.widgets.animation.HorizontallyAnimation
import com.elta.android.presentation.core.compose.widgets.buttons.RoundedButton
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.toUi
import com.elta.android.presentation.theme.GetLocalProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit

private const val MESSAGE_MAX_LINES = 10

internal enum class RecordState {
    Empty,
    Recording,
    Complete,
    Deleted
}

internal data class ConsultantBottomAppBarWidgetState(
    val connectState: ConnectState,
    val messageText: TextFieldValue,
    val messageType: WebimContentType,
    val recordState: RecordState,
    val recordTime: LocalTime,
    val recordGraph: List<Float>
)

internal class ConsultantBottomAppBarWidgetModel :
    BaseWidgetModel<ConsultantBottomAppBarWidgetState>() {

    override fun createInitState(): ConsultantBottomAppBarWidgetState =
        ConsultantBottomAppBarWidgetState(
            connectState = ConnectState.Offline,
            messageText = TextFieldValue(),
            messageType = WebimContentType.Voice,
            recordState = RecordState.Empty,
            recordTime = LocalTime.MIN,
            recordGraph = emptyList()
        )

    fun startRecord() {
        setState {
            state.value.copy(
                recordState = RecordState.Recording,
                recordGraph = emptyList(),
                recordTime = LocalTime.MIN
            )
        }
    }

    fun stopRecord() {
        setState { state.value.copy(recordState = RecordState.Complete) }
    }

    fun deleteRecord() {
        setState { state.value.copy(recordState = RecordState.Deleted) }
    }

    fun sendRecord() {
        setState { state.value.copy(recordState = RecordState.Empty) }
    }

    fun clearRecordState() {
        setState { state.value.copy(recordState = RecordState.Empty) }
    }

    fun addRecordTime(millis: Long) {
        val oldTime = state.value.recordTime
        setState { state.value.copy(recordTime = oldTime.plus(millis, ChronoUnit.MILLIS)) }
    }

    fun addValueToGraph(value: Float) {
        val newValue = when {
            value < 0.1 -> 0.1f
            value > 1 -> 1f
            else -> value
        }
        setState {
            state.value.copy(
                recordGraph = state.value.recordGraph
                    .toMutableList()
                    .apply {
                        add(newValue)
                    }
            )
        }
    }

    fun setText(value: TextFieldValue?) {
        val messageType = if (value?.text?.isNotBlank() == true) {
            WebimContentType.Text
        } else {
            WebimContentType.Voice
        }
        setState {
            state.value.copy(
                messageText = value ?: TextFieldValue(),
                messageType = messageType
            )
        }
    }
}

@Composable
internal fun ConsultantBottomAppBar(widgetModel: ConsultantBottomAppBarWidgetModel) {
    val state = widgetModel.state.collectAsState()
    Box {
        MessageBox(widgetModel)
        HorizontallyAnimation(visualState = state.value.recordState != RecordState.Empty) {
            WaveRecordGraph(widgetModel = widgetModel)
        }
    }
}

@Composable
private fun MessageBox(widgetModel: ConsultantBottomAppBarWidgetModel) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.shadeBlack4)
                .padding(dimens.consultantBottomBarContentPadding)
                .imePadding()
        ) {
            FileButton(widgetModel)
            MessageField(widgetModel)
            SendButton(widgetModel)
        }
    }
}

@Composable
private fun WaveRecordGraph(widgetModel: ConsultantBottomAppBarWidgetModel) {
    GetLocalProperties { dimens, brash, colors, shapes, types ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .disableClickable()
                .background(color = colors.gGreenB)
                .padding(dimens.consultantBottomBarContentPadding)
        ) {
            RecordDeleteButton(widgetModel)
            GraphField(widgetModel)
            RecordControlButton(widgetModel)
        }
    }
}

@Composable
private fun BoxScope.RecordControlButton(widgetModel: ConsultantBottomAppBarWidgetModel) {
    val state = widgetModel.state.collectAsState()
    val recordState = state.value.recordState
    val icon = when (recordState) {
        RecordState.Recording -> R.drawable.ic_record_stop
        else -> R.drawable.ic_send
    }
    val action = when (recordState) {
        RecordState.Recording -> ConsultantAction.StopRecVoiceClick
        else -> ConsultantAction.SendVoiceRecClick
    }
    GetLocalProperties { _, _, colors, _, _ ->
        Box(modifier = Modifier.Companion.align(Alignment.BottomEnd)) {
            RoundedButton(
                icon = icon,
                background = colors.white,
                border = null,
                iconColor = colors.gGreenB,
                onClick = { widgetModel.sendAction(action) }
            )
        }
    }
}

@Composable
private fun BoxScope.RecordDeleteButton(widgetModel: ConsultantBottomAppBarWidgetModel) {
    val recordState = widgetModel.state.collectAsState().value.recordState
    GetLocalProperties { _, _, colors, _, _ ->
        var iconColor = colors.white
        var backgroundColor = colors.blackBlue20
        if (recordState == RecordState.Deleted) {
            iconColor = colors.gOrangeB
            backgroundColor = colors.white
        }
        Box(modifier = Modifier.Companion.align(Alignment.BottomStart)) {
            RoundedButton(
                icon = R.drawable.ic_record_delete,
                background = animateColorAsState(targetValue = backgroundColor).value,
                iconColor = animateColorAsState(targetValue = iconColor).value,
                border = null,
                onClick = { widgetModel.sendAction(ConsultantAction.DeleteRecVoiceClick) }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun BoxScope.SendButton(widgetModel: ConsultantBottomAppBarWidgetModel) {
    val state = widgetModel.state.collectAsState()
    val messageType = state.value.messageType
    val recordPermissionState =
        rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
    val icon = if (messageType == WebimContentType.Voice) {
        R.drawable.ic_voice_message
    } else {
        R.drawable.ic_send
    }
    val action = if (messageType == WebimContentType.Voice) {
        ConsultantAction.StartRecVoiceClick(recordPermissionState.status)
    } else {
        ConsultantAction.SendMessageClick(state.value.messageText.text)
    }
    GetLocalProperties { _, _, colors, _, _ ->
        Box(modifier = Modifier.Companion.align(Alignment.BottomEnd)) {
            RoundedButton(
                icon = icon,
                background = colors.gGreenB,
                border = null,
                iconColor = colors.shadeBlack4,
                onClick = { widgetModel.sendAction(action) }
            )
        }
    }
}

@Composable
private fun BoxScope.FileButton(widgetModel: ConsultantBottomAppBarWidgetModel) {
    Box(modifier = Modifier.Companion.align(Alignment.BottomStart)) {
        RoundedButton(
            icon = R.drawable.ic_file,
            onClick = { widgetModel.sendAction(ConsultantAction.FileClick) }
        )
    }
}

@Composable
private fun BoxScope.GraphField(widgetModel: ConsultantBottomAppBarWidgetModel) {
    val listState = rememberLazyListState()
    val state = widgetModel.state.collectAsState()
    LaunchedEffect(key1 = state.value.recordGraph.size) {
        listState.scrollToItem(state.value.recordGraph.size)
    }
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(dimens.graphFieldPadding)
                .fillMaxWidth()
        ) {
            LazyRow(
                userScrollEnabled = false,
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(dimens.verySmallDim),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(end = dimens.graphTimePadding)
                    .fillMaxWidth()
            ) {
                items(items = state.value.recordGraph) {
                    Box(
                        modifier = Modifier
                            .width(dimens.graphItemHeight)
                            .height(dimens.graphItemMaxWidth * it)
                            .background(color = colors.white, shape = shapes.round)
                    )
                }
            }
            TimeLabel(state.value.recordTime.toUi())
        }
    }
}

@Composable
private fun BoxScope.TimeLabel(time: String) {
    GetLocalProperties { _, _, colors, _, types ->
        Text(
            text = time,
            style = types.title3,
            color = colors.white,
            maxLines = 1,
            modifier = Modifier.Companion.align(Alignment.CenterEnd)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BoxScope.MessageField(widgetModel: ConsultantBottomAppBarWidgetModel) {
    val state = widgetModel.state.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val visualTransformation = remember { VisualTransformation.None }
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(dimens.messageTextFieldPadding)
        ) {
            BasicTextField(
                value = state.value.messageText,
                onValueChange = widgetModel::setText,
                interactionSource = interactionSource,
                maxLines = MESSAGE_MAX_LINES,
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.white, shape = shapes.consultantTextField)
                    .border(
                        color = colors.shadeBlack3,
                        shape = shapes.consultantTextField,
                        width = dimens.borderWidth
                    )
            ) { innerTextField ->
                TextFieldDefaults.TextFieldDecorationBox(
                    value = state.value.messageText.text,
                    innerTextField = innerTextField,
                    enabled = true,
                    interactionSource = interactionSource,
                    visualTransformation = visualTransformation,
                    singleLine = false,
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colors.blackBlue,
                        backgroundColor = colors.white,
                        cursorColor = colors.blackBlue,
                        errorIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        placeholderColor = colors.shadeBlack1
                    ),
                    placeholder = { Text(text = stringResource(id = R.string.consultant_message_placeholder)) },
                    contentPadding = dimens.messageTextPadding
                )
            }
        }
    }
}
